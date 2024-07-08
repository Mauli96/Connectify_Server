package example.com.service

import example.com.data.models.Post
import example.com.data.repository.post.PostRepository
import example.com.data.requests.CreatePostRequest
import example.com.data.responses.PostResponse
import example.com.util.Constants

class PostService(
    private val repository: PostRepository
) {

    suspend fun createPost(
        request: CreatePostRequest,
        userId: String,
        imageUrl: String
    ): Boolean {
        return repository.createPost(
            Post(
                imageUrl = imageUrl,
                userId = userId,
                timestamp = System.currentTimeMillis(),
                description = request.description
            )
        )
    }

    suspend fun getPostsForFollows(
        ownUserId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<PostResponse> {
        return repository.getPostsByFollows(ownUserId, page, pageSize)
    }

    suspend fun getPostsForProfile(
        ownUserId: String,
        userId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<PostResponse> {
        return repository.getPostsForProfile(ownUserId, userId, page, pageSize)
    }

    suspend fun getPost(
        postId: String
    ): Post? {
        return repository.getPost(postId)
    }

    suspend fun getPostDetails(
        ownUserId: String,
        postId: String
    ): PostResponse? {
        return repository.getPostDetails(ownUserId, postId)
    }

    suspend fun deletePost(postId: String) {
        repository.deletePost(postId)
    }
}