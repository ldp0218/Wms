package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.popup_nonscanning.view.*
import razerdp.basepopup.BasePopupWindow


/**
* Created by ldp on 2017/10/27.
*/
class NonScanningPopup(private val activity: Activity) : BasePopupWindow(activity), View.OnClickListener {

    lateinit var popupView: View
    lateinit var adapter: ArrayAdapter<String>

    init {
        if (activity is CpckActivity) {
            adapter = ArrayAdapter<String>(activity,
                    android.R.layout.simple_list_item_1, activity.nonScanningList.values.toMutableList())
            popupView.listView.adapter = adapter
        }
        popupView.button_close.setOnClickListener(this)
    }

    fun update(list: HashMap<Int, String>) {
        adapter = ArrayAdapter<String>(activity,
                android.R.layout.simple_list_item_1, list.values.toMutableList())
        popupView.listView.adapter = adapter
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
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_nonscanning, null)
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