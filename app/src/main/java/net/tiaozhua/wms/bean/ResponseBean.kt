package net.tiaozhua.wms.bean

import java.io.Serializable

/**
* Created by ldp on 2017/11/2.
*/
data class ApiBean<out I>(val code: Int, val data: I, val info: String)

data class ResponseList<out T>(val items: List<T>, val limit: Int, var page: Int, val totalCount: Int, var totalPages: Int) : Serializable

data class Orders(val ck_id: Int, val ck_name: String, val client_id: Int, val client_name: String, val date: String,
                  val dj_id: Int, val dj_money: Double, val dj_name: String, val dj_no: String, val dj_remark: String,
                  val dj_type: Int, val handler_id: Int, val handler_name: String, val type: Int, val user_id: Int,
                  val user_name: String, val wid: Int) : Serializable

data class Jhrk(val ck_id: Int, val ck_name: String, val client_id: Int, val client_name: String, val delJhmx: List<Jhmx>?,
                val dj_id: Int, val handler_id: Int, val handler_name: String, val isBill: Int, val jh_id: Int,
                val jh_ldrq: String, val jh_money: Double, val jh_no: String, val jhmx: List<Jhmx>, val newJhmx: List<Jhmx>?,
                val oldJhmx: List<Jhmx>?, val remark: String, val sys_date: String, val user_id: Int)

data class Jhmx(val ck_id: Int, val hj_name: String?, val jh_id: Int, val jhdmx_id: Int, val jhdmx_num: Double,
                val jhmx_id: Int, val ma_code: String, val ma_id: Int, val ma_model: String, val ma_name: String,
                val ma_spec: String, val ma_txm: String, var mx_num: Double, val mx_price: Double, val mx_remark: String, val mx_unit: String)

data class Material(val begin_num: Double, val charcode: String, val ck_name: String, val comment: String, val kc_flag: Boolean,
                    val kc_hw_name: String, val kc_id: Int, val kc_num: Double, val ma_address: String,val ma_code: String,
                    val ma_color: String, val ma_id: Int, val ma_inprice: Double, val ma_kind: String,val ma_max: Double,
                    val ma_min: Double, val ma_model: String, val ma_name: String, val ma_pym: String,
                    val ma_spec: String, val ma_txm: String, val ma_unit: String, val used_flag: Boolean) : Serializable