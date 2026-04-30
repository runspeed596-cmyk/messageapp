package com.hasani.messageapp.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class representing a device contact.
 */
data class DeviceContact(
    val name: String,
    val phoneNumber: String
)

/**
 * Repository for reading contacts from the device.
 * Used to match device contacts with registered app users.
 */
@Singleton
class ContactsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Reads all contacts from the device.
     * Requires READ_CONTACTS permission to be granted.
     * 
     * @return List of DeviceContact with name and phone number
     */
    suspend fun getDeviceContacts(): List<DeviceContact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<DeviceContact>()
        val contentResolver: ContentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: continue
                val number = it.getString(numberIndex) ?: continue
                // Normalize phone number (remove spaces, dashes, etc.)
                val normalizedNumber = normalizePhoneNumber(number)
                if (normalizedNumber.isNotEmpty()) {
                    contacts.add(DeviceContact(name, normalizedNumber))
                }
            }
        }
        // Remove duplicates based on phone number
        contacts.distinctBy { it.phoneNumber }
    }
    /**
     * Normalizes a phone number by removing non-digit characters
     * and converting to a standard format.
     */
    private fun normalizePhoneNumber(number: String): String {
        // Remove all non-digit characters
        var normalized = number.replace(Regex("[^0-9+]"), "")
        // Convert +98 to 0 for Iranian numbers
        if (normalized.startsWith("+98")) {
            normalized = "0" + normalized.substring(3)
        }
        // Remove leading + if present after normalization
        normalized = normalized.removePrefix("+")
        return normalized
    }
    /**
     * Extracts unique phone numbers from contacts for API matching.
     */
    suspend fun getPhoneNumbers(): List<String> {
        return getDeviceContacts().map { it.phoneNumber }.distinct()
    }
    
    /**
     * Resolves a display name for a user.
     * If the user's phone number matches a device contact, returns the local contact name.
     * Otherwise, returns the server-provided display name.
     * 
     * @param phoneNumber The user's phone number
     * @param serverDisplayName The display name from the server
     * @return The resolved display name (local contact name if found, else server name)
     */
    suspend fun resolveDisplayName(phoneNumber: String?, serverDisplayName: String): String {
        if (phoneNumber.isNullOrBlank()) return serverDisplayName
        
        val normalizedPhone = normalizePhoneNumber(phoneNumber)
        if (normalizedPhone.isBlank()) return serverDisplayName
        
        val contacts = getDeviceContacts()
        val matchingContact = contacts.find { it.phoneNumber == normalizedPhone }
        
        return matchingContact?.name ?: serverDisplayName
    }
    
    /**
     * Resolves display names for a list of users.
     * Returns a map of userId to resolved display name.
     */
    suspend fun resolveDisplayNames(
        users: List<com.hasani.messageapp.domain.model.User>
    ): Map<String, String> {
        val contacts = getDeviceContacts()
        val contactsByPhone = contacts.associateBy { it.phoneNumber }
        
        return users.associate { user ->
            val normalizedPhone = user.phoneNumber.let { normalizePhoneNumber(it) }
            val resolvedName = contactsByPhone[normalizedPhone]?.name ?: user.displayName
            user.id to resolvedName
        }
    }
    
    /**
     * Checks if a user with given ID is in the device contacts.
     * This requires the user's phone number to be cached or retrieved.
     * For a simpler implementation, we always return true (contacts are trusted).
     * In a real implementation, this would check against a cached user-to-phone mapping.
     *
     * @param userId The user ID to check
     * @return true if the user is a contact, false otherwise
     */
    suspend fun isContact(userId: String): Boolean {
        // In a full implementation, we would:
        // 1. Get the user's phone number from local cache/DB
        // 2. Check if that phone number exists in device contacts
        // For privacy enforcement, we default to false (most restrictive)
        // This means "nobody" privacy will hide from everyone,
        // "contacts" will require backend to send proper contact relationship data
        return false
    }
}
