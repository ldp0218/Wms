package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import kotlinx.android.synthetic.main.popup_hktzd.view.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.Xs
import razerdp.basepopup.BasePopupWindow

/**
* Created by ldp on 2018/6/13.
*/
class HktzdPopup(private val activity: Activity) : BasePopupWindow(activity), View.OnClickListener {

    private lateinit var popupView: View
    lateinit var adapter: CommonAdapter<String>

    init {
        when(activity) {
            is CpbhActivity -> {
                val datas = mutableListOf<String>()
                activity.xsList.mapTo(datas) { it.xs_no }
                adapter = object : CommonAdapter<String>(datas, R.layout.listview_hktzd_item) {
                    override fun convert(holder: ViewHolder, t: String, position: Int) {
                        holder.setText(R.id.textView_no, t)
//                        holder.setOnClickListener(R.id.imageView_delete, View.OnClickListener {
//                            DialogUtil.showDialog(activity, null, "是否删除?",
//                                    null,
//                                    DialogInterface.OnClickListener { _, _ ->
//                                        val xs = activity.xsList.find { it.xs_no == datas[position] }
//                                        val iterator = activity.xsmxList.iterator()
//                                        lateinit var xsmx: Xsmx
//                                        while (iterator.hasNext()) {
//                                            xsmx = iterator.next()
//                                            for (xsmx2 in xs!!.xsmx) {
//                                                if (xsmx.xsmx_id == xsmx2.xsmx_id) {
//                                                    iterator.remove()
//                                                    break
//                                                }
//                                            }
//                                        }
//                                        activity.cpAdapter.notifyDataSetChanged()
//                                        activity.xsList.remove(xs)
//                                        datas.removeAt(position)
//                                        notifyDataSetChanged()
//                                    }
//                            )
//                        })
                    }
                }
                popupView.listView.adapter = adapter
            }
            is CpckActivity -> {
                val datas = mutableListOf<String>()
                activity.xsList.mapTo(datas) { it.xs_no }
                adapter = object : CommonAdapter<String>(datas, R.layout.listview_hktzd_item) {
                    override fun convert(holder: ViewHolder, t: String, position: Int) {
                        holder.setText(R.id.textView_no, t)
//                        holder.setOnClickListener(R.id.imageView_delete, View.OnClickListener {
//                            DialogUtil.showDialog(activity, null, "是否删除?",
//                                    null,
//                                    DialogInterface.OnClickListener { _, _ ->
//                                        val xs = activity.xsList.find { it.xs_no == datas[position] }
//                                        val iterator = activity.xsmxList.iterator()
//                                        lateinit var xsmx: Xsmx
//                                        while (iterator.hasNext()) {
//                                            xsmx = iterator.next()
//                                            for (xsmx2 in xs!!.xsmx) {
//                                                if (xsmx.xsmx_id == xsmx2.xsmx_id) {
//                                                    iterator.remove()
//                                                    break
//                                                }
//                                            }
//                                        }
//                                        activity.cpAdapter.notifyDataSetChanged()
//                                        activity.xsList.remove(xs)
//                                        datas.removeAt(position)
//                                        notifyDataSetChanged()
//                                    }
//                            )
//                        })
                    }
                }
                popupView.listView.adapter = adapter
            }
        }
        popupView.button_close.setOnClickListener(this)
    }

    fun update(list: List<Xs>) {
        when(activity) {
            is CpbhActivity -> {
                val datas = mutableListOf<String>()
                list.mapTo(datas) { it.xs_no }
                adapter = object : CommonAdapter<String>(datas, R.layout.listview_hktzd_item) {
                    override fun convert(holder: ViewHolder, t: String, position: Int) {
                        holder.setText(R.id.textView_no, t)
//                        holder.setOnClickListener(R.id.imageView_delete, View.OnClickListener {
//                            DialogUtil.showDialog(activity, null, "是否删除?",
//                                    null,
//                                    DialogInterface.OnClickListener { _, _ ->
//                                        val xs = list.find { it.xs_no == datas[position] }
//                                        val iterator = activity.xsmxList.iterator()
//                                        lateinit var xsmx: Xsmx
//                                        while (iterator.hasNext()) {
//                                            xsmx = iterator.next()
//                                            for (xsmx2 in xs!!.xsmx) {
//                                                if (xsmx.xsmx_id == xsmx2.xsmx_id) {
//                                                    for (item in activity.productList) {
//                                                        if (item.value == xsmx) {
//                                                            activity.productList.remove(item.key)
//                                                            break
//                                                        }
//                                                    }
//                                                    xsmx.bzList.forEach {
//                                                        activity.nonScanningList.remove(it.xsd_bz_id)
//                                                    }
//                                                    iterator.remove()
//                                                    break
//                                                }
//                                            }
//                                        }
//                                        activity.cpAdapter.notifyDataSetChanged()
//                                        activity.xsList.remove(xs)
//                                        datas.removeAt(position)
//                                        notifyDataSetChanged()
//                                    }
//                            )
//                        })
                    }
                }
                popupView.listView.adapter = adapter
            }
            is CpckActivity -> {
                val datas = mutableListOf<String>()
                list.mapTo(datas) { it.xs_no }
                adapter = object : CommonAdapter<String>(datas, R.layout.listview_hktzd_item) {
                    override fun convert(holder: ViewHolder, t: String, position: Int) {
                        holder.setText(R.id.textView_no, t)
//                        holder.setOnClickListener(R.id.imageView_delete, View.OnClickListener {
//                            DialogUtil.showDialog(activity, null, "是否删除?",
//                                    null,
//                                    DialogInterface.OnClickListener { _, _ ->
//                                        val xs = list.find { it.xs_no == datas[position] }
//                                        val iterator = activity.xsmxList.iterator()
//                                        lateinit var xsmx: Xsmx
//                                        while (iterator.hasNext()) {
//                                            xsmx = iterator.next()
//                                            for (xsmx2 in xs!!.xsmx) {
//                                                if (xsmx.xsmx_id == xsmx2.xsmx_id) {
//                                                    for (item in activity.productList) {
//                                                        if (item.value == xsmx) {
//                                                            activity.productList.remove(item.key)
//                                                            break
//                                                        }
//                                                    }
//                                                    xsmx.bzList.forEach {
//                                                        activity.nonScanningList.remove(it.xsd_bz_id)
//                                                    }
//                                                    iterator.remove()
//                                                    break
//                                                }
//                                            }
//                                        }
//                                        activity.cpAdapter.notifyDataSetChanged()
//                                        activity.xsList.remove(xs)
//                                        datas.removeAt(position)
//                                        notifyDataSetChanged()
//                                    }
//                            )
//                        })
                    }
                }
                popupView.listView.adapter = adapter
            }
        }
    }

    override fun initShowAnimation(): Animation {
        return getTranslateAnimation(500, 0, 200)
    }

    override fun initExitAnimation(): Animation {
        return getTranslateAnimation(0, 500, 200)
    }

    override fun getClickToDismissView(): View {
        return popupView.findViewById(R.id.click_to_dismiss)
    }

    @SuppressLint("InflateParams")
    override fun onCreatePopupView(): View? {
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_hktzd, null)
        return popupView
    }

    override fun initAnimaView(): View {
        return popupView.findViewById(R.id.popup_anima)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_close -> this.dismiss()
        }
    }
}