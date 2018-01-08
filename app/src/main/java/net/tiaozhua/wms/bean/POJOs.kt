package net.tiaozhua.wms.bean

import java.io.Serializable

/**
* Created by ldp on 2017/11/2.
*/
const val SCAN_ACTION = "urovo.rcv.message"  // PDA扫描结束action

data class ApiBean<out T>(val code: Int, val data: T, val info: String?)

data class ResponseList<out T>(val items: List<T>, val limit: Int, var page: Int, val totalCount: Int, var totalPages: Int) : Serializable

data class Orders(val ck_id: Int, val ck_name: String, val client_id: Int, val client_name: String, val date: String,
      val dj_id: Int, val dj_money: Double, val dj_name: String, val dj_no: String, val dj_remark: String,
      val dj_type: Int, val handler_id: Int, val handler_name: String, val type: Int, val user_id: Int,
      val user_name: String, val wid: Int) : Serializable

data class Jhrk(val pdacode: Int, val ck_id: Int, val ck_name: String, val client_id: Int, val client_name: String, var delJhmx: MutableList<Jhmx>?,
    var dj_id: Int, val handler_id: Int, val handler_name: String, val isBill: Int, val jh_id: Int,
    val jh_ldrq: String, val jh_money: Double, val jh_no: String, var jhmx: MutableList<Jhmx>, val newJhmx: MutableList<Jhmx>?,
    var oldJhmx: MutableList<Jhmx>?, val remark: String, val sys_date: String, val user_id: Int)

data class Jhmx(val ck_id: Int, val hj_name: String?, val jh_id: Int, val jhdmx_id: Int, val jhdmx_num: Int,
    val jhmx_id: Int, val ma_code: String, val ma_id: Int, val ma_model: String, val ma_name: String,
    val ma_spec: String, val ma_txm: String, var mx_num: Int, val mx_price: Double, val mx_remark: String, val mx_unit: String, val kcnum: Int)

data class Material(val begin_num: Double, val charcode: String, val ck_name: String, val comment: String, val kc_flag: Boolean,
    val kc_hw_name: String, val kc_id: Int, val kc_num: Int, val ma_address: String,val ma_code: String,
    val ma_color: String, val ma_id: Int, val ma_inprice: Double, val ma_kind: String,val ma_max: Double,
    val ma_min: Double, val ma_model: String, val ma_name: String, val ma_pym: String,
    val ma_spec: String, val ma_txm: String, val ma_unit: String, val used_flag: Boolean) : Serializable

data class Ckd(val check_date: String, val check_flag: Int, val check_name: String, val check_remark: String, val ck_flag: Int,
               val ck_id: Int, val ck_name: String, val ckd_id: Int, val ckd_ldrq: String, val ckd_no: String, val llr_name: String, val llr_id: Int,
               val remark: String, val bm_id: Int, val bm_name: String, val jx: Int, var ckdmx: MutableList<Ckdmx>, val pdacode: Int) : Serializable

data class Ckdmx(val kc_hw_name: String, val kc_num: Int, val ma_code: String, val ma_id: Int, val ma_model: String, val ma_name: String, val hj_name: String?,
                 val ma_spec: String, val ma_txm: String, val ma_unit: String, val mckmx_id: Int, var mx_num: Int, val mx_remark: String, val wc_num: Int, var ck_num: Int)

data class CkdFailureData(val clldList: List<Ckdmx>?, val ckcList: List<Ckdmx>?)

data class Ck(val ck_id: Int, val ck_name: String)

data class Pd(val id: Int, val pd_hao: String, val pd_date: String, val ck_id: Int, val ck_no: String,
              val ck_name: String, val pd_state: Int, val handler: String, val pd_remark: String)

data class Pdmx(var type: Int, var pd_id: Int?, var iid: Int?, var kc_num: Int, var pd_num: Int, var id: Int?, var ck_id: Int,
                var hao: String, var txm: String, var name: String, var spec: String, var model: String,
                var hw: String?, var comment: String, var bz_code: String = "", var scd_no: String = "", var xsd_bz_id: Int = 0)

data class PdResult(val code: Int, val info: String?, val id: Int, val pd_id: Int)

data class Rkd(var rk_no: String, var ck_id: Int, var ck_name: String, var rk_ldrq: String, var remark: String, val rkmx: MutableList<Rkmx>, val isBill: Int)

data class Rkmx(val xsd_bz_id: Int, var mx_num: Int, var mx_remark: String?, val scd_no: String, val pro_name: String, val bz_code: String)

data class Product(val pro_id: Int, val pro_name: String, val pro_code: String, val pro_unit: String,
                   val pro_model: String, val pro_spec: String, var bz_id: Int, val bz_code: String,
                   val scd_no: String, val kc_num: Int, val kc_hw_name: String)
