package com.example.data.responses

data class BasicApiResponse<T>(
    val successful: Boolean,
    val message: String? = null,
    val data: T? = null
)
