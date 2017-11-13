package net.tiaozhua.wms.utils

import okhttp3.*
import java.util.concurrent.TimeUnit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
* Created by ldp on 2017/11/3.
*/

class RetrofitManager private constructor() {
    private var mRetrofit: Retrofit? = null

    init {
        val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .cookieJar(object : CookieJar {
                    private var cookieStore: List<Cookie>? = null
                    override fun saveFromResponse(httpUrl: HttpUrl, list: List<Cookie>) {
                        cookieStore = list
                    }
                    override fun loadForRequest(httpUrl: HttpUrl): List<Cookie> {
                        return cookieStore ?: listOf()
                    }
                })
                .build()

        mRetrofit = Retrofit.Builder()
                .baseUrl("http://192.168.1.165:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
    }

    companion object {
        private var mRetrofitManager: RetrofitManager? = null

        val instance: RequestInterface
            @Synchronized get() {
                if (mRetrofitManager == null) {
                    mRetrofitManager = RetrofitManager()
                }
                return mRetrofitManager!!.mRetrofit!!.create(RequestInterface::class.java)
            }
    }
}
