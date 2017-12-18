package net.tiaozhua.wms.utils

import net.tiaozhua.wms.bean.*
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
* Created by ldp on 2017/11/1.
*/
interface RequestInterface {
    @GET("action/admin/login?type=1")
    fun login(@Query("a_name") name: String, @Query("a_pwd") password: String): Call<ApiBean<String>>

    @GET("action/djcg/djCgPage?limit=100&orderByProp=dj_id&descOrAsc=desc&dj_type=1&type=0")
    fun wlrkd(): Call<ApiBean<ResponseList<Orders>>>

    @GET("action/ckd/ckdPage?limit=100&orderByProp=ckd_id&descOrAsc=desc&check_flag=1&ck_flag=0")
    fun wlckd(): Call<ApiBean<ResponseList<Ckd>>>

    @GET("action/jh/jhrkInfo")
    fun jhrkInfo(@Query("id") id: Int): Call<ApiBean<Jhrk>>

    @GET("action/material/materialList?selectValue=4")
    fun materialList(@Query("searchValue") txm: String, @Query("ckid") ckid: Int, @Query("page") page: Int = 1): Call<ApiBean<ResponseList<Material>>>

    @POST("action/jh/updateJh")
    fun updateJh(@Body route: RequestBody): Call<ApiBean<List<Jhmx>>>

    @GET("action/ckd/ckdmxList")
    fun ckdmxList(@Query("ckd_id") ckd_id: Int, @Query("ck_id") ck_id: Int): Call<ApiBean<List<Ckdmx>>>
}