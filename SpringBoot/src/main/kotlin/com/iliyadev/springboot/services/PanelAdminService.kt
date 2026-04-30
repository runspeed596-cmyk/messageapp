package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.PanelAdmin
import com.iliyadev.springboot.repositories.PanelAdminRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PanelAdminService(
    private val panelAdminRepository: PanelAdminRepository
) {
    private val logger = LoggerFactory.getLogger(PanelAdminService::class.java)
    private val passwordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder()

    @PostConstruct
    fun seedDefaultSuperAdmin() {
        if (!panelAdminRepository.existsByUsername("admin")) {
            val superAdmin = PanelAdmin(
                username = "admin",
                passwordHash = passwordEncoder.encode("Admin@123"),
                displayName = "مدیر اصلی",
                isSuperAdmin = true
            )
            panelAdminRepository.save(superAdmin)
            logger.info("Default super admin seeded: admin / Admin@123")
        }
    }

    fun authenticateAdmin(username: String, password: String): PanelAdmin? {
        val admin = panelAdminRepository.findByUsername(username) ?: return null
        if (!passwordEncoder.matches(password, admin.passwordHash)) return null
        return admin
    }

    fun fetchAllAdmins(): List<PanelAdmin> {
        return panelAdminRepository.findAll()
    }

    fun createAdmin(username: String, password: String, displayName: String, isSuperAdmin: Boolean): PanelAdmin {
        if (panelAdminRepository.existsByUsername(username)) {
            throw IllegalArgumentException("نام کاربری '$username' قبلاً استفاده شده است")
        }
        val admin = PanelAdmin(
            username = username,
            passwordHash = passwordEncoder.encode(password),
            displayName = displayName,
            isSuperAdmin = isSuperAdmin
        )
        return panelAdminRepository.save(admin)
    }

    fun deleteAdmin(id: UUID) {
        val admin = panelAdminRepository.findById(id).orElseThrow {
            IllegalArgumentException("ادمین با این شناسه یافت نشد")
        }
        if (admin.isSuperAdmin) {
            val superAdminCount = panelAdminRepository.findAll().count { it.isSuperAdmin }
            if (superAdminCount <= 1) {
                throw IllegalArgumentException("حذف آخرین سوپر ادمین امکان‌پذیر نیست")
            }
        }
        panelAdminRepository.deleteById(id)
    }

    fun updateAdmin(id: UUID, displayName: String?, isSuperAdmin: Boolean?, newPassword: String?): PanelAdmin {
        val admin = panelAdminRepository.findById(id).orElseThrow {
            IllegalArgumentException("ادمین با این شناسه یافت نشد")
        }
        if (displayName != null) admin.displayName = displayName
        if (isSuperAdmin != null) admin.isSuperAdmin = isSuperAdmin
        if (!newPassword.isNullOrBlank()) admin.passwordHash = passwordEncoder.encode(newPassword)
        return panelAdminRepository.save(admin)
    }
}
