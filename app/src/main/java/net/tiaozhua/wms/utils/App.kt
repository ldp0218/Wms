package net.tiaozhua.wms.utils

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Vibrator
import net.tiaozhua.wms.bean.User

@Suppress("DEPRECATION")
class App : Application() {
    var user: User? = null
    var gzId: Int = 0

    private lateinit var mVibrator: Vibrator

//    val soundpool = SoundPool.Builder()
//            .setMaxStreams(1)
//            .setAudioAttributes(
//                    AudioAttributes.Builder()
//                            .setUsage(AudioAttributes.USAGE_MEDIA)
//                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                            .build()).build()
    private val soundpool = SoundPool(10, AudioManager.STREAM_NOTIFICATION, 0)
    private val soundid = soundpool.load("/etc/Scan_new.ogg", 1)

    /**
     * 播放声音并震动
     */
    fun playAndVibrate(context: Context) {
        soundpool.play(soundid, 1f, 1f, 0, 0, 1f)
        mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        mVibrator.vibrate(100)
//        mVibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}