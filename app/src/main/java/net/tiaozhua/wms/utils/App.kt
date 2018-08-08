package net.tiaozhua.wms.utils

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Vibrator
import net.tiaozhua.wms.bean.User
import android.app.Activity



@Suppress("DEPRECATION")
class App : Application() {
    private lateinit var activityList: MutableList<Activity>//用于存放所有启动的Activity的集合
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

    override fun onCreate() {
        super.onCreate()
        activityList = arrayListOf()
    }

    /**
     * 添加Activity
     */
    fun addActivity(activity: Activity) {
        // 判断当前集合中不存在该Activity
        if (!activityList.contains(activity)) {
            activityList.add(activity)//把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    fun removeActivity(activity: Activity) {
        //判断当前集合中存在该Activity
        if (activityList.contains(activity)) {
            activityList.remove(activity)//从集合中移除
            activity.finish()//销毁当前Activity
        }
    }

    /**
     * 销毁所有的Activity
     */
    fun removeALLActivity() {
        //通过循环，把集合中的所有Activity销毁
        for (activity in activityList) {
            activity.finish()
        }
    }
}