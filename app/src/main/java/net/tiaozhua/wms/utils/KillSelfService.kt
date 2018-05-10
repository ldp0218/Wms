package net.tiaozhua.wms.utils

/**
 * Created by ldp on 2018/5/10.
 */

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder

/***
 * 该服务只用来让APP重启，生命周期也仅仅是只是重启APP。重启完即自我杀死
 */
class KillSelfService : Service() {
    private val handler: Handler = Handler()
    private var pkgName: String? = null


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        stopDelayed = intent.getLongExtra("Delayed", 2000)
        pkgName = intent.getStringExtra("PackageName")
        handler.postDelayed({
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName!!)
            startActivity(launchIntent)
            this@KillSelfService.stopSelf()
        }, stopDelayed)
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        /**关闭应用后多久重新启动 */
        private var stopDelayed: Long = 2000
    }

}
