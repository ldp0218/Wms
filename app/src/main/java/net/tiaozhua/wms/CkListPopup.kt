package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_wlck.*
import kotlinx.android.synthetic.main.activity_wlrk.*
import kotlinx.android.synthetic.main.popup_ck_list.view.*
import net.tiaozhua.wms.bean.Ck
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import net.tiaozhua.wms.view.ChoiceView
import razerdp.basepopup.BasePopupWindow


/**
* Created by ldp on 2017/10/27.
*/
class CkListPopup(private val activity: Activity) : BasePopupWindow(activity), View.OnClickListener {

    private lateinit var popupView: View
    private lateinit var list: List<Ck>
    lateinit var adapter: ArrayAdapter<Ck>

    init {
        LoadingDialog.show(activity)
        RetrofitManager.instance.ckList()
                .enqueue(object : BaseCallback<ResponseList<Ck>>(activity) {
                    override fun successData(data: ResponseList<Ck>) {
                        list = data.items
                        adapter = object : ArrayAdapter<Ck>(activity, R.layout.listview_material, list) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = (convertView as ChoiceView?) ?: ChoiceView(context)
                                view.setTextView(getItem(position).ck_name)
                                return view
                            }
                        }
                        popupView.listView_ck.adapter = adapter
                    }
                })

        popupView.button_close.setOnClickListener(this)
        popupView.button_ok.setOnClickListener(this)
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
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_ck_list, null)
        return popupView
    }

    override fun initAnimaView(): View {
        return popupView.findViewById(R.id.popup_anima)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_close -> dismiss()
            R.id.button_ok -> {
                val position = popupView.listView_ck.checkedItemPosition
                if (position == ListView.INVALID_POSITION) {
                    Toast.makeText(context, "请选择仓库", Toast.LENGTH_SHORT).show()
                } else {
                    when(activity) {
                        is WlrkActivity -> {
                            activity.jhd.ck_id = list[position].ck_id
                            activity.jhd.ck_name = list[position].ck_name
                            activity.editText_ck.setText(list[position].ck_name)
                        }
                        is WlckActivity -> {
                            activity.ckd.ck_id = list[position].ck_id
                            activity.ckd.ck_name = list[position].ck_name
                            activity.editText_wlck_ck.setText(list[position].ck_name)
                        }
                    }
                    dismiss()
                }
            }
        }
    }
}