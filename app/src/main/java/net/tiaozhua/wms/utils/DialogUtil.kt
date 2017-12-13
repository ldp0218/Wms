package net.tiaozhua.wms.utils

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog

/**
* Created by ldp on 2017/11/7.
*/
object DialogUtil {
    fun showDialog(context: Context,
                   title: String?,
                   message: String,
                   negativeCallback: DialogInterface.OnClickListener?,
                   positiveCallback: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setNegativeButton("取消", negativeCallback)
        builder.setPositiveButton("确定", positiveCallback)
        builder.show()
    }

    fun showAlert(context: Context,
                   message: String,
                   positiveCallback: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setMessage(message)
        builder.setPositiveButton("确定", positiveCallback)
        builder.show()
    }
}