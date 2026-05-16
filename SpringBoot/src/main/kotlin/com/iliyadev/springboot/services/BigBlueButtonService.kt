package com.iliyadev.springboot.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

@Service
class BigBlueButtonService {

    private val log = LoggerFactory.getLogger(BigBlueButtonService::class.java)

    // Using Blindside Networks test server for local development
    // For production, this should be replaced with the actual Ubuntu BBB server URL and Secret
    @Value("\${bbb.server.url:https://test-install.blindsidenetworks.com/bigbluebutton/api}")
    lateinit var serverUrl: String

    @Value("\${bbb.server.secret:8cd8ef52e8e101574e400365b55e11a6}")
    lateinit var sharedSecret: String

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    fun createMeeting(
        meetingId: String,
        name: String,
        attendeePw: String,
        moderatorPw: String,
        record: Boolean = true
    ): Boolean {
        try {
            val welcomeMsg = urlEncode("به کلاس آنلاین «$name» خوش آمدید! 🎓")
            val params = "name=${urlEncode(name)}" +
                "&meetingID=${urlEncode(meetingId)}" +
                "&attendeePW=${urlEncode(attendeePw)}" +
                "&moderatorPW=${urlEncode(moderatorPw)}" +
                "&record=$record" +
                "&autoStartRecording=$record" +
                "&allowStartStopRecording=true" +
                "&welcome=$welcomeMsg" +
                "&logoutURL=${urlEncode("https://mosbatelm.ir")}" +
                "&meta_bbb-origin=MosbatElm"
            val checksum = calculateChecksum("create", params)
            val requestUrl = "$serverUrl/create?$params&checksum=$checksum"
            val request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val responseBody = response.body()
            log.info("BBB Create Meeting Response for $meetingId: $responseBody")
            val doc = parseXml(responseBody)
            val returncode = doc.getElementsByTagName("returncode").item(0)?.textContent
            return returncode == "SUCCESS"
        } catch (e: Exception) {
            log.error("Failed to create BBB meeting $meetingId", e)
            return false
        }
    }

    /**
     * Checks if a meeting is currently running (moderator has joined and started it).
     * This enables SkyRoom-like behavior: students can only join after the organizer starts.
     */
    fun isMeetingRunning(meetingId: String): Boolean {
        try {
            val params = "meetingID=${urlEncode(meetingId)}"
            val checksum = calculateChecksum("isMeetingRunning", params)
            val requestUrl = "$serverUrl/isMeetingRunning?$params&checksum=$checksum"
            val request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val doc = parseXml(response.body())
            val running = doc.getElementsByTagName("running").item(0)?.textContent
            log.info("BBB isMeetingRunning for $meetingId: $running")
            return running == "true"
        } catch (e: Exception) {
            log.error("Failed to check meeting status for $meetingId", e)
            return false
        }
    }

    /**
     * Gets recording URLs for a given meeting ID.
     * Returns a list of maps with playback URLs and metadata.
     */
    fun getRecordings(meetingId: String): List<Map<String, String>> {
        try {
            val params = "meetingID=${urlEncode(meetingId)}"
            val checksum = calculateChecksum("getRecordings", params)
            val requestUrl = "$serverUrl/getRecordings?$params&checksum=$checksum"
            val request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val doc = parseXml(response.body())
            val recordings = mutableListOf<Map<String, String>>()
            val recordingNodes = doc.getElementsByTagName("recording")
            for (i in 0 until recordingNodes.length) {
                val node = recordingNodes.item(i)
                val recordId = node.childNodes.let { children ->
                    (0 until children.length).mapNotNull { j ->
                        if (children.item(j).nodeName == "recordID") children.item(j).textContent else null
                    }.firstOrNull() ?: ""
                }
                val playbackNode = node.childNodes.let { children ->
                    (0 until children.length).mapNotNull { j ->
                        if (children.item(j).nodeName == "playback") children.item(j) else null
                    }.firstOrNull()
                }
                val playbackUrl = playbackNode?.childNodes?.let { children ->
                    (0 until children.length).mapNotNull { j ->
                        val child = children.item(j)
                        if (child.nodeName == "format") {
                            child.childNodes.let { formatChildren ->
                                (0 until formatChildren.length).mapNotNull { k ->
                                    if (formatChildren.item(k).nodeName == "url") formatChildren.item(k).textContent else null
                                }.firstOrNull()
                            }
                        } else null
                    }.firstOrNull()
                } ?: ""
                recordings.add(mapOf("recordId" to recordId, "playbackUrl" to playbackUrl))
            }
            log.info("BBB Recordings for $meetingId: ${recordings.size} found")
            return recordings
        } catch (e: Exception) {
            log.error("Failed to get recordings for $meetingId", e)
            return emptyList()
        }
    }

    fun getJoinUrl(meetingId: String, fullName: String, password: String, isFarsi: Boolean = true): String {
        var params = "fullName=${urlEncode(fullName)}&meetingID=${urlEncode(meetingId)}&password=${urlEncode(password)}&joinViaHtml5=true"
        // Force Persian (fa) locale in the frontend
        if (isFarsi) {
            params += "&userdata-bbb_override_default_locale=fa_IR"
        }
        val checksum = calculateChecksum("join", params)
        return "$serverUrl/join?$params&checksum=$checksum"
    }

    private fun calculateChecksum(apiCall: String, params: String): String {
        val input = apiCall + params + sharedSecret
        val digest = MessageDigest.getInstance("SHA-1")
        val bytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
    }

    private fun parseXml(xmlString: String): Document {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val inputSource = InputSource(StringReader(xmlString))
        return builder.parse(inputSource)
    }
}
