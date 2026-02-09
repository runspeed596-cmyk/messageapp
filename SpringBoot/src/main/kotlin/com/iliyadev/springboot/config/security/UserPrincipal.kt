package com.iliyadev.springboot.config.security

import java.security.Principal
import java.util.UUID

class UserPrincipal(
    val id: UUID,
    private val name: String
) : Principal {
    override fun getName(): String = name
}
