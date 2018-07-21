package net.tiaozhua.wms

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import net.tiaozhua.wms.utils.DialogUtil

/**
* Created by ldp on 2017/11/7.
*/
abstract class BaseActivity(private val layoutId: Int) : AppCompatActivity() {
    /**
     * 获取头部标题的TextView
     * @return
     */
    lateinit var toolbarTitle: TextView

    /**
     * this Activity of tool bar.
     * 获取头部.
     * @return support.v7.widget.Toolbar.
     */
    private lateinit var toolbar: Toolbar

    /**
     * 是否显示后退按钮,默认显示,可在子类重写该方法.
     * @return
     */
    protected var isShowBacking: Boolean = true

    /**
     * this activity layout res
     * 设置layout布局,在子类重写该方法.
     * @return res layout xml id
     */
    //protected abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = findViewById(R.id.toolbar_title)
        toolbar.overflowIcon = ContextCompat.getDrawable(this, R.mipmap.more)
        //将Toolbar显示到界面
        setSupportActionBar(toolbar)

        //getTitle()的值是activity的android:lable属性值
        toolbarTitle.text = title
        //设置默认的标题不显示
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onStart() {
        super.onStart()
        /**
         * 判断是否有Toolbar,并默认显示返回按钮
         */
        if (isShowBacking) {
            showBack()
        }
    }

    protected open fun isExit(): Boolean {
        return false
    }

    /**
     * 版本号小于21的后退按钮图片
     */
    private fun showBack() {
        //setNavigationIcon必须在setSupportActionBar(toolbar);方法后面加入
        toolbar.setNavigationIcon(R.mipmap.arrow_left)
        toolbar.setNavigationOnClickListener {
            if (isExit()) {
                DialogUtil.showDialog(this, null, "数据未保存,是否离开?",
                        null,
                        DialogInterface.OnClickListener { _, _ ->
                            this.finish()
                            onBackPressed()
                        }
                )
            } else {
                this.finish()
                onBackPressed()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home -> {
                if (isExit()) {
                    DialogUtil.showDialog(this, null, "数据未保存,是否离开?",
                            null,
                            DialogInterface.OnClickListener { _, _ ->
                                this.finish()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            }
                    )
                } else {
                    this.finish()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
            R.id.menu_logout -> {
                if (isExit()) {
                    DialogUtil.showDialog(this, null, "数据未保存,是否离开?",
                            null,
                            DialogInterface.OnClickListener { _, _ ->
                                this.finish()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            }
                    )
                } else {
                    this.finish()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        return false
    }
}