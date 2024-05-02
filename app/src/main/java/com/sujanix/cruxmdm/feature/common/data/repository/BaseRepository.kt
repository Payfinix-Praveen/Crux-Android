package com.sujanix.cruxmdm.feature.common.data.repository

import android.util.Log
import com.sujanix.cruxmdm.feature.common.utlis.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

abstract class BaseRepository {

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                Log.d("ApiCheck", throwable.message.toString())
                when (throwable) {
                    is HttpException -> {
                        Resource.Failure(
                            false,
                            throwable.code(),
                            throwable.message(),
                            throwable.response()?.errorBody()
                        )
                    }

                    is IOException -> {
                        Resource.Failure(
                            true,
                            null,
                            "No Internet please check your internet connection and try again",
                            null
                        )
                    }

                    else -> {
                        Resource.Failure(true, null, throwable.localizedMessage, null)
                    }
                }
            }
        }
    }

    fun String.makeRequestBody(): RequestBody =
        this.toRequestBody("multipart/form-data".toMediaTypeOrNull())
}