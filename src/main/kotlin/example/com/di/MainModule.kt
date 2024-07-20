package example.com.di

import com.google.gson.Gson
import example.com.data.models.Skill
import example.com.data.repository.activity.ActivityRepository
import example.com.data.repository.activity.ActivityRepositoryImpl
import example.com.data.repository.chat.ChatRepository
import example.com.data.repository.chat.ChatRepositoryImpl
import example.com.data.repository.comment.CommentRepository
import example.com.data.repository.comment.CommentRepositoryImpl
import example.com.data.repository.follow.FollowRepository
import example.com.data.repository.follow.FollowRepositoryImpl
import example.com.data.repository.likes.LikeRepository
import example.com.data.repository.likes.LikeRepositoryImpl
import example.com.data.repository.post.PostRepository
import example.com.data.repository.post.PostRepositoryImpl
import example.com.data.repository.skill.SkillRepository
import example.com.data.repository.skill.SkillRepositoryImpl
import example.com.data.repository.user.UserRepository
import example.com.data.repository.user.UserRepositoryImpl
import example.com.service.*
import example.com.service.chat.ChatController
import example.com.service.chat.ChatService
import example.com.util.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mainModule = module {
    single {
        val client = KMongo.createClient().coroutine
        client.getDatabase(Constants.DATABASE_NAME)
    }
    single<UserRepository> {
        UserRepositoryImpl(get())
    }
    single<FollowRepository> {
        FollowRepositoryImpl(get())
    }
    single<PostRepository> {
        PostRepositoryImpl(get())
    }
    single<LikeRepository> {
        LikeRepositoryImpl(get())
    }
    single<CommentRepository> {
        CommentRepositoryImpl(get())
    }
    single<ActivityRepository> {
        ActivityRepositoryImpl(get())
    }
    single<SkillRepository> {
        SkillRepositoryImpl(get())
    }
    single<ChatRepository> {
        ChatRepositoryImpl(get())
    }
    single { UserService(get(), get()) }
    single { FollowService(get()) }
    single { PostService(get()) }
    single { LikeService(get(), get(), get()) }
    single { CommentService(get(), get()) }
    single { ActivityService(get(), get(), get()) }
    single { SkillService(get()) }
    single { ChatService(get()) }

    single { Gson() }

    single { ChatController(get()) }
}