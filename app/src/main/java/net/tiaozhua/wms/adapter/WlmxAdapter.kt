package net.tiaozhua.wms.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_wlmx.*
import net.tiaozhua.wms.R
import net.tiaozhua.wms.WlckActivity
import net.tiaozhua.wms.WlmxActivity
import net.tiaozhua.wms.bean.Scdpl
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.findViewOften

/**
* Created by ldp on 2017/11/8.
*/
class WlmxAdapter(private val list: MutableList<Scdpl>, private val context: Context) : BaseAdapter() {
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
        val view: View = convertView ?: inflater.inflate(R.layout.listview_wlmx_item, parent, false)
        val item = list[position]
        val no: TextView = view.findViewOften(R.id.textView_no)
        no.text = item.scd_no
        val name: TextView = view.findViewOften(R.id.textView_name)
        name.text = item.pro_name
        val model: TextView = view.findViewOften(R.id.textView_model)
        model.text = item.pro_model
        val klNum: TextView = view.findViewOften(R.id.textView_klnum)
        klNum.text = (item.mx_num - item.mx_wcnum).toString()
        val ckNum: TextView = view.findViewOften(R.id.textView_cknum)
        ckNum.text = item.num.toString()
        val remark: TextView = view.findViewOften(R.id.textView_remark)
        remark.text = item.mx_remark
        val btnDelete: Button = view.findViewOften(R.id.btnDelete)
        btnDelete.setOnClickListener {
            DialogUtil.showDialog(context, null, "是否删除?",
                    null,
                    DialogInterface.OnClickListener { _, _ ->
                        // 取消流程卡中物料的选择
                        for (scdmx in WlckActivity.self.scdmxList) {
                            if (scdmx.scd_no == item.scd_no) {
                                for (pl in scdmx.plList) {
                                    if (pl.ma_id == item.ma_id) {
                                        pl.checked = false
                                        break
                                    }
                                }
                                break
                            }
                        }
                        // 移除物料明细和物料出库列表中相关数据
                        for (wlck in WlckActivity.self.wlckList) {
                            if (wlck.ma_id == item.ma_id) {
                                wlck.num -= item.num
                                WlmxActivity.self.textView_manum.text = wlck.num.toString()
                                if (wlck.num > 0) {
                                    wlck.mxList!!.removeAt(position)
                                } else {
                                    WlckActivity.self.wlckList.remove(wlck)
                                }
                                WlckActivity.self.adapter.notifyDataSetChanged()
                                list.removeAt(position)
                                notifyDataSetChanged()
                                break
                            }
                        }
                    }
            )
        }
        return view
    }
}