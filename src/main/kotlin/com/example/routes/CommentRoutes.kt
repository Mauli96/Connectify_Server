package com.example.routes

import com.example.data.requests.CreateCommentRequest
import com.example.data.responses.BasicApiResponse
import com.example.service.ActivityService
import com.example.service.CommentService
import com.example.service.LikeService
import com.example.util.ApiResponseMessages
import com.example.util.CommentFilter
import com.example.util.Constants
import com.example.util.QueryParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createComment(
    commentService: CommentService,
    activityService: ActivityService
) {
    authenticate {
        post("/api/comment/create") {
            val request = call.receiveNullable<CreateCommentRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val userId = call.userId
            when(commentService.createComment(request, userId)) {
                is CommentService.ValidationEvent.ErrorFieldEmpty -> {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            successful = false,
                            message = ApiResponseMessages.FIELDS_BLANK
                        )
                    )
                }
                is CommentService.ValidationEvent.ErrorCommentTooLong -> {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            successful = false,
                            message = ApiResponseMessages.COMMENT_TOO_LONG
                        )
                    )
                }
                is CommentService.ValidationEvent.Success -> {
                    activityService.addCommentActivity(
                        byUserId = userId,
                        postId = request.postId,
                    )
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            successful = true,
                        )
                    )
                }
                is CommentService.ValidationEvent.UserNotFound -> {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            successful = false,
                            message = ApiResponseMessages.USER_NOT_FOUND
                        )
                    )
                }
            }
        }
    }
}

fun Route.getCommentsForPost(
    commentService: CommentService,
) {
    authenticate {
        get("/api/comment/get") {
            val postId = call.parameters[QueryParams.PARAM_POST_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize = call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?:
            Constants.DEFAULT_PAGE_SIZE
            val filterTypeString = call.parameters[QueryParams.FILTER_TYPE]
            val filterType = CommentFilter.fromValue(filterTypeString ?: "")
                ?: CommentFilter.MOST_RECENT
            val comments = commentService.getCommentsForPost(postId, call.userId, filterType, page, pageSize)
            call.respond(
                HttpStatusCode.OK,
                comments
            )
        }
    }
}

fun Route.deleteComment(
    commentService: CommentService,
    likeService: LikeService
) {
    authenticate {
        delete("/api/comment/delete") {
            val commentId = call.parameters[QueryParams.PARAM_COMMENT_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            val comment = commentService.getCommentById(commentId)
            if(comment?.userId != call.userId) {
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }
            val deleted = commentService.deleteComment(commentId)
            if(deleted) {
                likeService.deleteLikesForParent(commentId)
                call.respond(HttpStatusCode.OK,)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}