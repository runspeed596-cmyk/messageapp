package com.iliyadev.springboot.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URLEncoder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

@Service
class NajvaSmsService(
    @Value("\${najva.sms.api-key}") private val apiKey: String,
    @Value("\${najva.sms.sender}") private val sender: String,
    @Value("\${najva.sms.template-name}") private val templateName: String,
    @Value("\${najva.sms.base-url}") private val baseUrl: String,
    @Value("\${najva.sms.enabled:false}") private val isEnabled: Boolean
) {
    private val logger = LoggerFactory.getLogger(NajvaSmsService::class.java)
    private val objectMapper: ObjectMapper = ObjectMapper()

    private val httpClient: HttpClient = run {
        val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        HttpClient.newBuilder()
            .sslContext(sslContext)
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }

    /**
     * Send OTP code via Najva SMS template.
     */
    fun sendOtpViaSms(phoneNumber: String, otpCode: String): Boolean {
        if (!isEnabled) {
            logger.info("=====================================================")
            logger.info("📱 [TEST MODE] Najva SMS is Disabled.")
            logger.info("📱 MOCK SMS TO: $phoneNumber")
            logger.info("📱 MOCK OTP CODE: $otpCode")
            logger.info("=====================================================")
            return true
        }

        val encodedReceptor: String = URLEncoder.encode(phoneNumber, "UTF-8")
        val encodedTemplate: String = URLEncoder.encode(templateName, "UTF-8")
        val encodedToken: String = URLEncoder.encode(otpCode, "UTF-8")
        val encodedSender: String = URLEncoder.encode(sender, "UTF-8")

        // Resolve hostname to IP to bypass Docker DNS issues
        val resolvedHost: String = try {
            val addr = java.net.InetAddress.getByName(java.net.URI(baseUrl).host)
            addr.hostAddress
        } catch (e: Exception) {
            logger.warn("⚠️ DNS resolution failed for ${java.net.URI(baseUrl).host}, using fallback IP")
            "185.166.104.6" // Fallback IP for sms.najva.com
        }
        val originalHost: String = java.net.URI(baseUrl).host
        val ipBaseUrl: String = baseUrl.replace(originalHost, resolvedHost)

        val url: String = "$ipBaseUrl/$apiKey/verify/lookup.json?receptor=$encodedReceptor&sender=$encodedSender&template=$encodedTemplate&token=$encodedToken"

        return try {
            logger.info("📱 Sending OTP SMS to $phoneNumber via Najva...")
            logger.info("📱 URL (IP-based): $url")

            val request: HttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Host", originalHost)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "fa-IR,fa;q=0.9,en-US;q=0.8,en;q=0.7")
                .GET()
                .build()

            val response: HttpResponse<String> = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val responseBody: String = response.body() ?: ""
            val statusCode: Int = response.statusCode()

            logger.info("📱 Najva HTTP status: $statusCode")

            if (responseBody.isBlank()) {
                logger.error("❌ Najva SMS response was empty for $phoneNumber (HTTP $statusCode)")
                return false
            }

            // Check if CDN is returning an HTML challenge page
            if (responseBody.trim().startsWith("<")) {
                logger.error("❌ Najva CDN returned HTML instead of JSON (HTTP $statusCode). Snippet: ${responseBody.take(200)}")
                return false
            }

            logger.info("📱 Najva response: $responseBody")

            val responseJson = objectMapper.readTree(responseBody)
            val returnStatus: Int = responseJson.path("return").path("status").asInt(0)
            val returnMessage: String = responseJson.path("return").path("message").asText("")

            if (returnStatus == 200) {
                logger.info("✅ OTP SMS sent successfully to $phoneNumber (status=$returnStatus, message=$returnMessage)")
                true
            } else {
                logger.error("❌ Najva SMS failed for $phoneNumber: status=$returnStatus, message=$returnMessage")
                false
            }
        } catch (e: Exception) {
            logger.error("❌ Exception sending OTP SMS to $phoneNumber: ${e.javaClass.simpleName}: ${e.message}", e)
            false
        }
    }
}
