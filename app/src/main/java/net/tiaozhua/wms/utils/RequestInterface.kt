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

    @GET("action/jhd/jhdPage?check_flag=1&finish_flag=0&orderByProp=jhd_id&descOrAsc=desc")
    fun wljhd(@Query("page") page: Int = 1): Call<ApiBean<ResponseList<Jhd>>>

    @GET("action/pda/jhdInfo")
    fun jhdInfo(@Query("id") id: Int): Call<ApiBean<Jhd>>

    @GET("action/material/materialList?selectValue=4")
    fun materialList(@Query("searchValue") txm: String, @Query("ckid") ckid: Int, @Query("page") page: Int = 1): Call<ApiBean<ResponseList<Material>>>

    @GET("action/jh/getJh_no")
    fun getJhNo(): Call<ApiBean<String>>

    @POST("action/jh/insert_jh")
    fun insertJh(@Body jhd: RequestBody): Call<ApiBean<List<Jhdmx>>>

    @GET("action/ckd/ckdPage?limit=100&orderByProp=ckd_id&descOrAsc=desc&check_flag=1&ck_flag=0")
    fun wlckd(): Call<ApiBean<ResponseList<Ckd>>>

    @GET("action/ckd/ckdmxList")
    fun ckdmxList(@Query("ckd_id") ckd_id: Int, @Query("ck_id") ck_id: Int): Call<ApiBean<List<Ckdmx>>>

    @POST("action/pda/lldCk")
    fun lldCk(@Body lld: RequestBody): Call<ApiBean<CkdFailureData>>

    @GET("action/ckd/lldInfo")
    fun lldInfo(@Query("id") id: Int): Call<ApiBean<Ckd>>

    @GET("action/ck/ckList")
    fun ckList(): Call<ApiBean<ResponseList<Ck>>>

    @GET("action/kc/pdList")
    fun pdList(@Query("type") type: Int, @Query("page") page: Int = 1, @Query("limit") limit: Int = 10): Call<ApiBean<ResponseList<Pd>>>

    @POST("action/kc/selPdmx")
    fun selPdmx(@Body pdmx: RequestBody): Call<ApiBean<Pdmx>>

    @POST("action/kc/updatePdmx")
    fun updatePdmx(@Body pdmx: RequestBody): Call<PdResult>

    @GET("action/kc/pdmxList?showUnused=true&pdStatus=1")
    fun pdmxList(@Query("type") type: Int, @Query("ck_id") ck_id: Int,
                 @Query("pd_id") pd_id: Int, @Query("page") page: Int = 1): Call<ApiBean<ResponseList<Pdmx>>>

    @POST("action/kc/finishPd")
    fun finishPd(@Query("pdId") pdId: Int): Call<ApiBean<String>>

    @POST("action/kc/delPd")
    fun delPd(@Query("type") type: Int, @Query("pdId") pdId: Int): Call<ApiBean<String>>

    @GET("action/rk/getRk_no")
    fun getRkNo(): Call<ApiBean<String>>

    @GET("action/pda/getProductInfo")
    fun getProductInfo(@Query("id") id: Int): Call<ApiBean<Bzmx>>

    @POST("action/rk/insert_rk")
    fun insertRk(@Body rkd: RequestBody): Call<ApiBean<List<Bzmx>>>

    @GET("action/pda/productList")
    fun productList(@Query("id") id: Int, @Query("ckid") ckid: Int): Call<ApiBean<ResponseList<Product>>>

    @GET("action/pda/hktzd")
    fun hktzd(@Query("id") id: Int): Call<ApiBean<Xs>>

    @POST("action/pda/cpck")
    fun cpck(@Body xs: RequestBody): Call<ApiBean<String>>
}