// app/src/main/java/com/muriithi/dekutcallforhelp/interfaces/OneSignalApi.kt:
package com.muriithi.dekutcallforhelp.interfaces

import com.muriithi.dekutcallforhelp.beans.OneSignalNotification
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OneSignalApi {

    @Headers(
        "Content-Type: application/json; charset=utf-8",
        "Authorization: Basic OTk3YjFkOTQtYWI0ZS00Zjk2LThhODItM2Q1ZjA5YWYyNGVk" // Use your OneSignal REST API key
    )
    @POST("notifications")
    suspend fun sendNotification(@Body notification: OneSignalNotification): retrofit2.Response<Void>

    companion object {
        private const val BASE_URL = "https://onesignal.com/api/v1/"

        fun create(): OneSignalApi {
            val logging = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OneSignalApi::class.java)
        }
    }
}