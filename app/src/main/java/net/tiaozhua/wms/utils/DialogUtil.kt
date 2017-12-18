package net.tiaozhua.wms.utils

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.util.*

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

    /**
     * 弹起输入法
     * @param edit
     * @param delay
     * @param delayTime
     */
    fun showInputMethod(context: Context, edit: EditText, delay: Boolean, delayTime: Long) {
        if (delay) {
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    val imm = context.getSystemService(
                            Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(edit, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                }
            }, delayTime)
        } else {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(edit, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        }
    }
}