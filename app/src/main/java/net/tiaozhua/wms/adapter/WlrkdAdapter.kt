package net.tiaozhua.wms.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.BaseAdapter
import net.tiaozhua.wms.R
import net.tiaozhua.wms.bean.Orders
import net.tiaozhua.wms.utils.findViewOften


/**
* Created by ldp on 2017/11/7.
*/
class WlrkdAdapter(private val list: List<Orders>, context: Context) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // 适配器中数据集中数据的个数
    override fun getCount(): Int {
        return list.size
    }

    // 获取数据集中与指定索引对应的数据项
    override fun getItem(position: Int): Any {
        return list[position]
    }

    // 获取指定行对应的ID
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 获取每一个Item显示的内容
    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.listview_rkd_item, null)
        val item: Orders = list[position]
        val no: TextView = view.findViewOften(R.id.textView_no)
        no.text = item.dj_no
        val date: TextView = view.findViewOften(R.id.textView_date)
        date.text = item.date
        val gys: TextView = view.findViewOften(R.id.textView_llr)
        gys.text = item.client_name
        val jsr: TextView = view.findViewOften(R.id.textView_jsr)
        jsr.text = item.handler_name
        val ck: TextView = view.findViewOften(R.id.textView_ck)
        ck.text = item.ck_name
        val bz: TextView = view.findViewOften(R.id.textView_bz)
        bz.text = item.dj_remark
        return view
    }
}