package com.iliyadev.springboot.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import com.fasterxml.jackson.databind.ObjectMapper

@Service
class NajvaSmsService(
    @Value("\${najva.sms.api-key}") private val apiKey: String,
    @Value("\${najva.sms.template-name}") private val templateName: String,
    @Value("\${najva.sms.base-url}") private val baseUrl: String
) {
    private val logger = LoggerFactory.getLogger(NajvaSmsService::class.java)
    private val restTemplate: RestTemplate = RestTemplate()
    private val objectMapper: ObjectMapper = ObjectMapper()
    /**
     * Send OTP code via Najva SMS template.
     * Uses the verify/lookup endpoint with the pre-defined template.
     *
     * @param phoneNumber The recipient phone number (e.g. "09123456789")
     * @param otpCode The OTP code to send as the token value
     * @return true if SMS was sent successfully, false otherwise
     */
    fun sendOtpViaSms(phoneNumber: String, otpCode: String): Boolean {
        val url: String = UriComponentsBuilder
            .fromHttpUrl("$baseUrl/$apiKey/verify/lookup.json")
            .queryParam("receptor", phoneNumber)
            .queryParam("template", templateName)
            .queryParam("token", otpCode)
            .toUriString()
        return try {
            logger.info("📱 Sending OTP SMS to $phoneNumber via Najva...")
            val responseBody: String? = restTemplate.getForObject(url, String::class.java)
            if (responseBody == null) {
                logger.error("❌ Najva SMS response was null for $phoneNumber")
                return false
            }
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
