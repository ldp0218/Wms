package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.Toast

import razerdp.basepopup.BasePopupWindow

/**
* Created by ldp on 2017/10/27.
*/
class DialogPopup(context: Activity) : BasePopupWindow(context), View.OnClickListener {

    lateinit var popupView: View
    private var receiverTag: Boolean = false

    init {
        bindEvent()
    }

    override fun initShowAnimation(): Animation {
        return getTranslateAnimation(500, 0, 200)
    }

    override fun initExitAnimation(): Animation {
        return getTranslateAnimation(0, 500, 200)
    }

    override fun dismiss() {
        super.dismiss()
        if (receiverTag) {   //判断广播是否注册
            receiverTag = false
            context.unregisterReceiver(WlrkActivity.wlrkActivity.mScanReceiver)
        }
    }

    override fun getClickToDismissView(): View {
        return popupView.findViewById(R.id.click_to_dismiss)
    }

    @SuppressLint("InflateParams")
    override fun onCreatePopupView(): View? {
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_scan_material, null)
        return popupView
    }

    override fun initAnimaView(): View {
        return popupView.findViewById(R.id.popup_anima)
    }

    private fun bindEvent() {
        val filter = IntentFilter()
        filter.addAction(SCAN_ACTION)
        if (!receiverTag) {     //在注册广播接受者的时候 判断是否已被注册,避免重复多次注册广播
            receiverTag = true
            context.registerReceiver(WlrkActivity.wlrkActivity.mScanReceiver, filter)
        }
        popupView.findViewById(R.id.button_close).setOnClickListener(this)
        popupView.findViewById(R.id.button_commit).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_close -> this.dismiss()
            R.id.button_commit -> Toast.makeText(context, "点击了确认", Toast.LENGTH_SHORT).show()
            else -> {
            }
        }
    }

    companion object {
        private val SCAN_ACTION = "urovo.rcv.message"//扫描结束action
    }
}