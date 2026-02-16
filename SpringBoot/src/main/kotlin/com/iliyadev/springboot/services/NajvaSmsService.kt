package com.iliyadev.springboot.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.http.client.ClientHttpResponse
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URLEncoder

@Service
class NajvaSmsService(
    @Value("\${najva.sms.api-key}") private val apiKey: String,
    @Value("\${najva.sms.sender}") private val sender: String,
    @Value("\${najva.sms.template-name}") private val templateName: String,
    @Value("\${najva.sms.base-url}") private val baseUrl: String
) {
    private val logger = LoggerFactory.getLogger(NajvaSmsService::class.java)
    private val objectMapper: ObjectMapper = ObjectMapper()
    private val restTemplate: RestTemplate = RestTemplate().apply {
        errorHandler = object : DefaultResponseErrorHandler() {
            override fun hasError(response: ClientHttpResponse): Boolean = false
        }
    }
    /**
     * Send OTP code via Najva SMS template.
     * Uses the verify/lookup endpoint with the pre-defined template.
     *
     * @param phoneNumber The recipient phone number (e.g. "09123456789")
     * @param otpCode The OTP code to send as the token value
     * @return true if SMS was sent successfully, false otherwise
     */
    fun sendOtpViaSms(phoneNumber: String, otpCode: String): Boolean {
        val encodedReceptor: String = URLEncoder.encode(phoneNumber, "UTF-8")
        val encodedTemplate: String = URLEncoder.encode(templateName, "UTF-8")
        val encodedToken: String = URLEncoder.encode(otpCode, "UTF-8")
        val encodedSender: String = URLEncoder.encode(sender, "UTF-8")
        val url: String = "$baseUrl/$apiKey/verify/lookup.json?receptor=$encodedReceptor&sender=$encodedSender&template=$encodedTemplate&token=$encodedToken"
        return try {
            logger.info("📱 Sending OTP SMS to $phoneNumber via Najva...")
            logger.info("📱 Request URL: $url")
            val responseBody: String? = restTemplate.getForObject(url, String::class.java)
            if (responseBody == null) {
                logger.error("❌ Najva SMS response was null for $phoneNumber")
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
            logger.error("❌ Exception sending OTP SMS to $phoneNumber: ${e.message}", e)
            false
        }
    }
}

