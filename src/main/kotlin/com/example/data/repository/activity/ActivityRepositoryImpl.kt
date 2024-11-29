package com.example.data.repository.activity

import com.example.data.models.Activity
import com.example.data.models.Skill
import com.example.data.responses.ActivityResponse
import com.example.util.Constants
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.eq

class ActivityRepositoryImpl(
    db: CoroutineDatabase
) : ActivityRepository {

    private val activities = db.getCollection<Activity>()

    override suspend fun getActivitiesForUser(
        userId: String,
        page: Int,
        pageSize: Int
    ): List<ActivityResponse> {
        val activities = activities.find(Activity::toUserId eq userId)
            .skip(page * pageSize)
            .limit(pageSize)
            .descendingSort(Activity::timestamp)
            .toList()
        return activities.map { activity ->
            ActivityResponse(
                timestamp = activity.timestamp,
                userId = activity.byUserId,
                parentId = activity.parentId,
                type = activity.type,
                username = activity.username,
                profilePictureUrl = activity.profilePictureUrl,
                id = activity.id
            )
        }
    }

    override suspend fun createActivity(activity: Activity) {
        activities.insertOne(activity)
    }

    override suspend fun deleteActivity(activityId: String): Boolean {
        return activities.deleteOneById(activityId).wasAcknowledged()
    }
}