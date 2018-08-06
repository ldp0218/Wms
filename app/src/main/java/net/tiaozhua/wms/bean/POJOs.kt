package net.tiaozhua.wms.bean

import android.databinding.BaseObservable
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

/**
* Created by ldp on 2017/11/2.
*/
const val SCAN_ACTION = "urovo.rcv.message"  // PDA扫描结束action

//annotation class Poko

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiBean<out T>(val code: Int, val data: T, val info: String?)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class ResponseList<out T>(val items: List<T>, val limit: Int, var page: Int, val totalCount: Int, var totalPages: Int) : Serializable

@Parcelize
//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class User(val a_id: Int, val a_no: String, val a_name: String, val bm_id: Int?, val bm_name: String?, val gz_id: Int?, val gz_name: String?, val ck_id: Int?, val ck_name: String?) : Parcelable

@Parcelize
//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Jhd(var ck_id: Int, var ck_name: String, var client_id: Int, var client_name: String, val date: String,
               var pdacode: Int, var jhd_id: Int, var jhd_no: String, var jh_no: String, val remark: String?, var handler_id: Int,
               var handler_name: String, val jhd_wcnum: Int, var jhd_wwcnum: Int, var jhd_ldrq: String, var jh_ldrq: String,
               var jhmx: MutableList<Jhdmx>?, var isBill: Int = 1) : Parcelable

@Parcelize
//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Jhdmx(val ck_id: Int, val hj_name: String?, val jhdmx_id: Int, val ma_code: String,
                 val ma_id: Int, val ma_name: String, val ma_kind: String?, val ma_spec: String?, val ma_txm: String,
                 var mx_num: Double, val mx_wcnum: Double, val mx_wwcnum: Double, var mx_price: Double,
                 val mx_remark: String?, val mx_unit: String, val kcnum: Double) : Parcelable

@Parcelize
//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Material(val charcode: String, val ck_name: String, val comment: String?, val kc_flag: Boolean,
                    val kc_hw_name: String?, val kc_id: Int, val kc_num: Double, val ck_num: Double, val ma_address: String?,
                    val ma_code: String, val ma_id: Int, val ma_inprice: Double, val ma_kind: Int?, val ma_kind_name: String?,
                    val ma_name: String, val ma_spec: String?, val ma_txm: String, val ma_unit: String) : Parcelable, Serializable

@Parcelize
//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Scdmx(val scdmx_id: Int, val pro_id: Int, val bz_id: Int?, val bz_code: String?, val scd_id: Int?,
                 val mx_num: Double, val mx_wcnum: Double, val mx_remark: String?, val xsdmx_id: Int?,
                 val pro_name: String, val mx_unit: String, val pro_model: String?, val xsdmx_spec: String,
                 val xsdmx_color: String, val pro_type: Int, val scd_no: String, var plList: MutableList<Scdpl>?, val pdacode: Int = 0) : Parcelable

@Parcelize
//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Scdpl(val scdpl_id: Int?, val ma_id: Int, val scdmx_id: Int, var kc_num: Double, var mx_num: Double, val kc_hw_name: String?,
                 var mx_wcnum: Double, var num: Double, var mx_remark: String?, val ma_name: String, val ma_code: String,
                 val ma_spec: String?, val ma_kind: String?, val ma_unit: String, val gz_id: Int, val isAdd: Int,
                 var scd_no: String?, var pro_name: String?, var pro_model: String?, var checked: Boolean = true) : Parcelable, BaseObservable()

@Parcelize
//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Wlck(val ma_id: Int, val kc_num: Double, val kc_hw_name: String?, var num: Double, var scanNum: Double, val mx_remark: String?, val ma_name: String, val ma_code: String,
                val ma_spec: String?, val ma_kind: String?, val ma_unit: String, val gz_id: Int, val isAdd: Int, var mxList: MutableList<Scdpl>?) : Parcelable

@Parcelize
//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Ckd(var ck_id: Int, var ck_name: String, val ckd_id: Int, var ckd_ldrq: String, var ckd_no: String, var llr_name: String, var llr_id: Int,
               val remark: String?, val bm_id: Int, val bm_name: String, var ckdmx: MutableList<Ckdmx>) : Parcelable

@Parcelize
//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Ckdmx(val scdpl_id: Int?, val scdmx_id: Int?, val ma_code: String = "", val ma_id: Int, val ma_kind: String? = "",
                 val ma_name: String, val ma_spec: String? = "", val ma_unit: String, val mckmx_id: Int, val mx_remark: String? = "",
                 var mx_num: Double, val wc_num: Double, val kc_num: Double, var ck_num: Double, val kl_num: Double?, val gz_id: Int, val isAdd: Int, var scd_no: String?) : Parcelable

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class CkdFailureData(val kcData: List<Ckdmx>?, val clData: List<Ckdmx>?)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Ck(val ck_id: Int, val ck_name: String)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Pd(val id: Int, val pd_hao: String, val pd_date: String, val ck_id: Int, val ck_no: String,
              val ck_name: String, val pd_state: Int, val handler: String, val pd_remark: String)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Pdmx(var type: Int, var pd_id: Int?, var iid: Int?, var kc_num: Double, var pd_num: Double, var id: Int?, var ck_id: Int,
                var hao: String, var txm: String, var name: String, var spec: String?, var kind: String?,
                var hw: String?, var comment: String?, var model: String?, var bz_hao: String?, var scd_no: String?, var xsd_bz_id: Int)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class PdResult(val code: Int, val info: String?, val id: Int, val pd_id: Int)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Rkd(var rk_no: String, var ck_id: Int?, var ck_name: String, var rk_ldrq: String,
               var remark: String?, val rkmx: MutableList<Rkmx>, val isBill: Int)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Rkmx(val xsd_bz_id: Int?, var mx_num: Int, val scd_no: String?,
                val pro_name: String, val bz_id: Int, val bz_code: String,
                val bz_hao: String, val pro_id: Int, val kc_num: Int, val wrk_num: Int)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Bzmx(val xsd_bz_id: Int, val bz_id: Int, var check_num: Int)

//data class Product(val pro_id: Int, val pro_name: String, val pro_code: String, val pro_unit: String,
//                   val pro_model: String?, val pro_spec: String?, var bz_id: Int, val bz_code: String,
//                   val scd_no: String?, val kc_num: Int, val kc_hw_name: String)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Xs(val xs_id: Int = 0, val ck_id: Int = 0, val ck_name: String = "", val xs_ldrq: String = "", val xs_no: String = "",
                 val remark: String? = "", val xsmx: MutableList<Xsmx> = mutableListOf(), val pdacode: Int = 0)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Xsmx(val xsmx_id: Int, val pro_id: Int, val xsdmx_id: Int, val mx_num: Int, var package_num: Int?, val scd_no: String?,
                var check_num: Int = 0, val pro_spec: String?, val pro_model: String?, val pro_color: String?, val mx_remark: String?, val xsdmx_instruction: String?,
                val pro_type: Int, val yx: Int, val bz_id: Int?, val bz_code: String?, val bzList: List<Bz>, var xsdbzList: MutableList<Bzmx>)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductXs(val xsList: List<Xs>, val bzmxList: List<Bzmx>)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Bz(val bz_id: Int, val bz_code: String?, val bz_hao: String, val bz_ratio: Int,
              var total: Int, var num: Int = 0, val xsd_bz_id: Int, val xsdmx_id: Int?, var scd_no: String?)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class Baozhuang(val xsd_bz_id: Int, val bz_id: Int, val pro_id: Int, val pro_name: String,
                     val pro_model: String, val pro_type: Int, val scd_no: String?, val xsdmx_id: Int,
                     val bz_code: String, val bz_hao: String, val pro_bz_num: Int, var check_num: Int,
                     val xsdmx_instruction: String?, val yx: Int, val kc_num: Int, val wrk_num: Int)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class SemiProduct(val ma_id: Int, val ma_name: String, val ma_code: String, val scdmxs: List<Scdmx>)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class BcpRkd(var ck_id: Int, var ck_name: String, val bcpRkmxs: MutableList<BcpRkmx>)

//@Poko
//@JsonIgnoreProperties(ignoreUnknown = true)
data class BcpRkmx(val ma_id: Int, val ma_name: String, val ma_code: String, val scdmx_id: Int,
                   var num: Int, var wrk_num: Int, val hw: String?, val remark: String?)
