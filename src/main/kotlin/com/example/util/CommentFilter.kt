package com.example.util

enum class CommentFilter(val value: String) {
    MOST_RECENT("mostRecent"),
    MOST_OLD("mostOld"),
    MOST_POPULAR("mostPopular");

    companion object {
        fun fromValue(value: String): CommentFilter? {
            return entries.find {
                it.value.equals(value, ignoreCase = true)
            }
        }
    }
}