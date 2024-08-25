package example.com.data.repository.activity

import example.com.data.models.Activity
import example.com.data.models.User
import example.com.data.responses.ActivityResponse
import org.litote.kmongo.`in`
import org.litote.kmongo.coroutine.CoroutineDatabase
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