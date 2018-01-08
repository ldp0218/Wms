package net.tiaozhua.wms.utils

import android.app.Dialog
import android.content.Context
import net.tiaozhua.wms.R

/**
* Created by ldp on 2017/11/2.
*/

class LoadingDialog : Dialog {

    constructor(context: Context) : super(context)

    constructor(context: Context, theme: Int) : super(context, theme)

    companion object {
        private var mLoadingProgress: LoadingDialog? = null

        fun show(context: Context) {
            mLoadingProgress = LoadingDialog(context, R.style.progress_dialog)
            mLoadingProgress?.setCanceledOnTouchOutside(false)
            mLoadingProgress?.setContentView(R.layout.dialog)
            //按返回键响应是否取消等待框的显示
            mLoadingProgress?.setCancelable(false)
            mLoadingProgress?.show()
        }

        fun dismiss() {
            if (mLoadingProgress != null) {
                mLoadingProgress!!.dismiss()
            }
        }
    }
}
