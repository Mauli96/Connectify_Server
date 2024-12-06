package com.example.di

import com.example.data.repository.activity.ActivityRepository
import com.example.data.repository.activity.ActivityRepositoryImpl
import com.example.data.repository.chat.ChatRepository
import com.example.data.repository.chat.ChatRepositoryImpl
import com.example.data.repository.comment.CommentRepository
import com.example.data.repository.comment.CommentRepositoryImpl
import com.example.data.repository.follow.FollowRepository
import com.example.data.repository.likes.LikeRepositoryImpl
import com.example.data.repository.post.PostRepository
import com.example.data.repository.post.PostRepositoryImpl
import com.example.data.repository.skill.SkillRepository
import com.example.data.repository.skill.SkillRepositoryImpl
import com.example.data.repository.user.UserRepository
import com.example.data.repository.user.UserRepositoryImpl
import com.example.service.*
import com.example.service.chat.ChatController
import com.example.service.chat.ChatService
import com.example.util.Constants
import com.google.gson.Gson
import example.com.data.repository.follow.FollowRepositoryImpl
import example.com.data.repository.likes.LikeRepository
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mainModule = module {

    val mongoPw = System.getenv("MONGO_PW")
    single {
        val client = KMongo.createClient(
            connectionString = "mongodb+srv://mauli_waghmore:$mongoPw@cluster.zpdoe.mongodb.net/?retryWrites=true&w=majority&appName=Cluster"
        ).coroutine
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

    single {
        UserService(get(), get())
    }
    single {
        FollowService(get(), get())
    }
    single {
        PostService(get())
    }
    single {
        LikeService(get(), get(), get())
    }
    single {
        CommentService(get(), get())
    }
    single {
        ActivityService(get(), get(), get(), get())
    }
    single {
        SkillService(get())
    }
    single {
        ChatService(get())
    }
    single {
        Gson()
    }
    single {
        ChatController(get())
    }
}