package com.example.data.repository.activity

import com.example.data.models.Activity
import com.example.data.responses.ActivityResponse
import com.example.util.Constants

interface ActivityRepository {

    suspend fun getActivitiesForUser(
        userId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<ActivityResponse>

    suspend fun createActivity(activity: Activity)

    suspend fun deleteActivity(activityId: String): Boolean
}