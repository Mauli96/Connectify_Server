package example.com.data.repository.likes

import example.com.data.models.Like
import example.com.util.Constants

interface LikeRepository {

    suspend fun likeParent(
        userId: String,
        parentId: String,
        parentType: Int
    ): Boolean

    suspend fun unlikeParent(
        userId: String,
        parentId: String,
        parentType: Int
    ): Boolean

    suspend fun deleteLikesForParent(parentId: String)

    suspend fun getLikesForParent(
        parentId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<Like>
}