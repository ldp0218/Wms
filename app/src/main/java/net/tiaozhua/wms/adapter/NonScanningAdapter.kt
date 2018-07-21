package net.tiaozhua.wms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import net.tiaozhua.wms.R
import net.tiaozhua.wms.bean.Bz
import net.tiaozhua.wms.utils.findViewOften

/**
* Created by ldp on 2017/11/7.
*/
class NonScanningAdapter(private val list: List<Bz>, context: Context) : BaseAdapter() {

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
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.listview_nonscanning_item, parent, false)
        val item = list[position]
        val no: TextView = view.findViewOften(R.id.textView_no)
        no.text = item.scd_no
        val bz: TextView = view.findViewOften(R.id.textView_bz)
        bz.text = item.bz_hao
        val num: TextView = view.findViewOften(R.id.textView_num)
        num.text = (item.total - item.num).toString()
        return view
    }
}