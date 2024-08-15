package example.com.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(val id: Int, val name: String, val email: String)
