package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.popup_over_use.view.*
import net.tiaozhua.wms.adapter.OverUseAdapter
import net.tiaozhua.wms.bean.Scdpl
import razerdp.basepopup.BasePopupWindow


/**
* Created by ldp on 2017/10/27.
*/
class OverUsePopup(private val activity: Activity) : BasePopupWindow(activity), View.OnClickListener {

    private lateinit var popupView: View
    lateinit var adapter: OverUseAdapter

    init {
        if (activity is LldInfoActivity) {
            adapter = OverUseAdapter(activity.overList, activity)
            popupView.listView_overUse.adapter = adapter
        }
        popupView.button_close.setOnClickListener(this)
        popupView.button_cancel.setOnClickListener(this)
        popupView.button_ok.setOnClickListener(this)
    }

    fun update(list: List<Scdpl>) {
        adapter = OverUseAdapter(list, activity)
        popupView.listView_overUse.adapter = adapter
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
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_over_use, null)
        return popupView
    }

    override fun initAnimaView(): View {
        return popupView.findViewById(R.id.popup_anima)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_close -> this.dismiss()
            R.id.button_cancel -> this.dismiss()
            R.id.button_ok -> {
                val listview = popupView.listView_overUse
                lateinit var layout: RelativeLayout
                lateinit var editText: EditText
                when (activity) {
                    is LldInfoActivity -> {
                        for (i in 0 until listview.adapter.count) {
                            layout = listview.getChildAt(i) as RelativeLayout
                            editText = layout.findViewById(R.id.editText_remark)
                            activity.overList[i].mx_remark = editText.text.toString()
                        }
                        if ((activity.overList.any {it.mx_remark!!.trim() == ""})) {
                            Toast.makeText(activity, "请填写完说明", Toast.LENGTH_SHORT).show()
                            return
                        }
                        this.dismiss()
                        val intent = Intent()
                        intent.putExtra("scdmx", activity.scdmx)
                        activity.setResult(Activity.RESULT_OK, intent)
                        activity.finish()
                    }
                    is WlckActivity -> {
                        for (i in 0 until listview.adapter.count) {
                            layout = listview.getChildAt(i) as RelativeLayout
                            editText = layout.findViewById(R.id.editText_remark)
                            activity.overList[i].mx_remark = editText.text.toString()
                        }
                        if ((activity.overList.any {it.mx_remark!!.trim() == ""})) {
                            Toast.makeText(activity, "请填写完说明", Toast.LENGTH_SHORT).show()
                            return
                        }
                        for (item in activity.scdmxList) {
                            for (it in item.plList) {
                                for (i in activity.overList) {
                                    if (it.scdpl_id == i.scdpl_id) {
                                        it.mx_remark = i.mx_remark
                                        break
                                    }
                                }
                            }
                        }
                        this.dismiss()
                    }
                }
            }
        }
    }
}