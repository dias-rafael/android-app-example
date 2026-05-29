package com.example.android.di

import android.content.Context
import androidx.room.Room
import com.example.android.data.local.AppDatabase
import com.example.android.data.local.UserDao
import com.example.android.data.remote.ApiService
import com.example.android.data.repository.UserRepositoryImpl
import com.example.android.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(okHttpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "example_db"
        ).build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService, userDao: UserDao): UserRepository {
        return UserRepositoryImpl(apiService, userDao)
    }

    @Provides
    @Singleton
    fun provideGetUsersUseCase(userRepository: UserRepository): com.example.android.domain.usecase.GetUsersUseCase {
        return com.example.android.domain.usecase.GetUsersUseCaseImpl(userRepository)
    }
}