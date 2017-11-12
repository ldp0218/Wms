package net.tiaozhua.wms.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import net.tiaozhua.wms.R
import net.tiaozhua.wms.bean.Jhmx
import net.tiaozhua.wms.utils.findViewOften

/**
* Created by ldp on 2017/11/8.
*/
class WlAdapter(private val list: List<Jhmx>, context: Context) : BaseAdapter() {
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
        val view: View = convertView ?: inflater.inflate(R.layout.listview_wl_item, null)
        val item: Jhmx = list[position]
        val name: TextView = view.findViewOften(R.id.textView_name)
        name.text = item.ma_name
        val wrknum: TextView = view.findViewOften(R.id.textView_wrknum)
        wrknum.text = item.jhdmx_num.toString()
        val scannum: TextView = view.findViewOften(R.id.textView_scannum)
        scannum.text = "0"
        val hw: TextView = view.findViewOften(R.id.textView_hw)
        hw.text = item.hj_name
        return view
    }
}