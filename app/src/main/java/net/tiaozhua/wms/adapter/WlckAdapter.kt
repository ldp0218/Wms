package net.tiaozhua.wms.adapter

import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import net.tiaozhua.wms.R
import net.tiaozhua.wms.WlckActivity
import net.tiaozhua.wms.WlmxActivity
import net.tiaozhua.wms.bean.Wlck
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.findViewOften

/**
* Created by ldp on 2017/11/7.
*/
class WlckAdapter(private val wlckList: List<Wlck>, private val context: WlckActivity) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // 适配器中数据集中数据的个数
    override fun getCount(): Int {
        return wlckList.size
    }

    // 获取数据集中与指定索引对应的数据项
    override fun getItem(position: Int): Any {
        return wlckList[position]
    }

    // 获取指定行对应的ID
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 获取每一个Item显示的内容
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.listview_wlck_item, parent, false)
        val item = wlckList[position]
        val name: TextView = view.findViewOften(R.id.textView_name)
        name.text = item.ma_name
        val kcNum: TextView = view.findViewOften(R.id.textView_kcnum)
        kcNum.text = item.kc_num.toString()
        val ckNum: TextView = view.findViewOften(R.id.textView_cknum)
        ckNum.text = item.num.toString()
        val scanNum: TextView = view.findViewOften(R.id.textView_scanNum)
        scanNum.text = item.scanNum.toString()
        val unit: TextView = view.findViewOften(R.id.textView_unit)
        unit.text = item.ma_unit
        val hw: TextView = view.findViewOften(R.id.textView_hw)
        hw.text = item.kc_hw_name
        val btnDelete: Button = view.findViewOften(R.id.btnDelete)
        btnDelete.setOnClickListener {
            DialogUtil.showDialog(context, null, "是否删除?",
                    null,
                    DialogInterface.OnClickListener { _, _ ->
                        // 取消流程卡中物料的选择
                        for (scdmx in context.scdmxList) {
                            for (pl in scdmx.plList) {
                                if (pl.ma_id == item.ma_id) {
                                    pl.checked = false
                                }
                            }
                        }
                        context.wlckList.removeAt(position)
                        notifyDataSetChanged()
                    }
            )
        }
        val layoutWl: RelativeLayout = view.findViewOften(R.id.layout_wl)
        layoutWl.setOnClickListener {
            val wl = context.wlckList[position]
            if (wl.mxList == null) {
                context.wlPopup!!.setUpdate()
                context.wlPopup!!.showPopupWindow()
            } else {
                val mIntent = Intent(context, WlmxActivity::class.java)
                mIntent.putExtra("wl", wl)
                context.startActivity(mIntent)
            }
        }
        return view
    }
}