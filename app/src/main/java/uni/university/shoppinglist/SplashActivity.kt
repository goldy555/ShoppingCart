package uni.university.shoppinglist

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private var mHandler: Handler? = null
    private var mAnimation: Animation? = null
    private val mDuration: Long = 3000
    private val mRunnable = Runnable {
        if (!isFinishing) {
            val intent = Intent(applicationContext, ShoppingListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash)


        mAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        findViewById<ImageView>(R.id.load_icon).startAnimation(mAnimation)

        mHandler = Handler()
        mHandler!!.postDelayed(mRunnable, mDuration)
    }

    override fun onDestroy() {
        mHandler!!.removeCallbacks(mRunnable)
        mAnimation!!.cancel()
        mAnimation!!.reset()
        super.onDestroy()
    }
}
