package net.tiaozhua.wms.utils

import android.content.Context
import android.content.Intent

/**
 * Created by ldp on 2018/5/10.
 */

object RestartAPPTool {
    /**
     * 重启整个APP
     * @param context
     * @param Delayed 延迟多少毫秒
     */
    @JvmOverloads
    fun restartAPP(context: Context, Delayed: Long = 500) {

        /**开启一个新的服务，用来重启本APP */
        val intent1 = Intent(context, KillSelfService::class.java)
        intent1.putExtra("PackageName", context.packageName)
        intent1.putExtra("Delayed", Delayed)
        context.startService(intent1)

        /**杀死整个进程 */
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
/***重启整个APP *///我们传入500毫秒
