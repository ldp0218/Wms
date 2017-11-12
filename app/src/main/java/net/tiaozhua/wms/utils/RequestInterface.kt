package net.tiaozhua.wms.utils

import net.tiaozhua.wms.bean.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
* Created by ldp on 2017/11/1.
*/
interface RequestInterface {
    @GET("action/admin/login?type=1")
    fun login(@Query("a_name") name: String, @Query("a_pwd") password: String): Call<ApiBean<String>>

    @GET("action/djcg/djCgPage?limit=100&orderByProp=dj_id&descOrAsc=desc&dj_type=1&type=0")
    fun wlrkd(): Call<ApiBean<ResponseList<Orders>>>

    @GET("action/jh/jhrkInfo")
    fun jhrkInfo(@Query("id") id: Int): Call<ApiBean<Jhrk>>

    @GET("action/material/materialList?selectValue=4")
    fun materialList(@Query("searchValue") txm: String, @Query("ckid") ckid: Int, @Query("page") page: Int = 1): Call<ApiBean<ResponseList<Material>>>
}