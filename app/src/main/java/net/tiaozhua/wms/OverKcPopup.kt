package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import kotlinx.android.synthetic.main.popup_over_kc.view.*
import net.tiaozhua.wms.adapter.OverKcAdapter
import net.tiaozhua.wms.bean.Ckdmx
import razerdp.basepopup.BasePopupWindow


/**
* Created by ldp on 2017/10/27.
*/
class OverKcPopup(private val activity: Activity) : BasePopupWindow(activity), View.OnClickListener {

    private lateinit var popupView: View
    lateinit var adapter: OverKcAdapter

    init {
        if (activity is WlckActivity) {
            adapter = OverKcAdapter(activity.overKcList, activity)
            popupView.listView_overKc.adapter = adapter
        }
        popupView.button_close.setOnClickListener(this)
        popupView.button_ok.setOnClickListener(this)
    }

    fun update(list: List<Ckdmx>) {
        adapter = OverKcAdapter(list, activity)
        popupView.listView_overKc.adapter = adapter
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
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_over_kc, null)
        return popupView
    }

    override fun initAnimaView(): View {
        return popupView.findViewById(R.id.popup_anima)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_close -> dismiss()
            R.id.button_ok -> dismiss()
        }
    }
}