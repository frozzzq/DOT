package com.dot.app.domain.model

/**
 * Modelo de dominio para la cuenta local de la usuaria.
 */
data class UserAccount(
    val id: Long = 0L,
    val name: String,
    val email: String
)
