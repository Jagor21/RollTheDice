package com.insolitum.rollthedice

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.IOException
import kotlin.math.abs
import kotlin.random.Random


class RollDice : Activity(), SensorEventListener {
    private val rollAnimations = 50
    private val delayTime = 15
    private lateinit var res: Resources
    private val diceImages = intArrayOf(
        R.drawable.dice_1,
        R.drawable.dice_2,
        R.drawable.dice_3,
        R.drawable.dice_4,
        R.drawable.dice_5,
        R.drawable.dice_6
    )
    private val dice: Array<Drawable?> = arrayOfNulls<Drawable>(6)
    private val randomGen: Random = Random
    private var diceSum = 0
    private val roll = intArrayOf(6, 6)
    private lateinit var dice1: ImageView
    private lateinit var dice2: ImageView
    private var diceContainer: LinearLayout? = null
    private lateinit var vView: View
    private lateinit var sensorMgr: SensorManager
    private var animationHandler: Handler? = null
    private var lastUpdate: Long = -1
    private var x = 0f
    private var y = 0f
    private var z = 0f
    private var last_x = 0f
    private var last_y = 0f
    private var last_z = 0f
    private var paused = false

    /** Called when the activity is first created.  */
    override fun onCreate(savedInstanceState: Bundle?) {
        paused = false
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game)
        title = getString(R.string.app_name)
        res = resources
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Hide the status bar
            hide(WindowInsetsCompat.Type.statusBars())
            // Allow showing the status bar with swiping from top to bottom
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        for (i in 0..5) {
            dice[i] = ContextCompat.getDrawable(this, diceImages[i])
        }
        diceContainer = findViewById(R.id.diceContainer)
        diceContainer!!.setOnClickListener {
            try {
                rollDice()
            } catch (e: Exception) {
            }
        }
        vView = findViewById(R.id.v_view)
        var clickTimes = 0
        vView.setOnClickListener {
            clickTimes++
            if(clickTimes == 13) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, this@RollDice.getSharedPref().getString(INSTALL_ATTRIBUTION_SP_KEY, "No install attribution data"))
                    type = "text/plain"
                }
                startActivity(intent)
                clickTimes = 0
            }
        }
        dice1 = findViewById(R.id.dice1)
        dice2 = findViewById(R.id.dice2)
        animationHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                dice1.setImageDrawable(dice[roll[0]])
                dice2.setImageDrawable(dice[roll[1]])
            }
        }
        sensorMgr = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelSupported: Boolean = sensorMgr.registerListener(
            this,
            sensorMgr!!.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
        if (!accelSupported) sensorMgr?.unregisterListener(this) //no accelerometer on the device
        rollDice()
    }

    private fun rollDice() {
        if (paused) return
        Thread {
            for (i in 0 until rollAnimations) {
                doRoll()
            }
        }.start()
        val mp: MediaPlayer = MediaPlayer.create(this, R.raw.roll)
        try {
            mp.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mp.start()
    }

    private fun doRoll() { // only does a single roll
        roll[0] = randomGen.nextInt(6)
        roll[1] = randomGen.nextInt(6)
        diceSum =
            roll[0] + roll[1] + 2 // 2 is added because the values of the rolls start with 0 not 1
        synchronized(getLayoutInflater()) { animationHandler?.sendEmptyMessage(0) }
        try { // delay to alloy for smooth animation
            Thread.sleep(delayTime.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        paused = false
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onSensorChanged(event: SensorEvent) {
        val mySensor: Sensor = event.sensor
        if (mySensor.getType() === SensorManager.SENSOR_ACCELEROMETER) {
            val curTime = System.currentTimeMillis()
            if (curTime - lastUpdate > UPDATE_DELAY) {
                val diffTime = curTime - lastUpdate
                lastUpdate = curTime
                x = event.values[SensorManager.DATA_X]
                y = event.values[SensorManager.DATA_Y]
                z = event.values[SensorManager.DATA_Z]
                val speed = abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000
                if (speed > SHAKE_THRESHOLD) { //the screen was shaked
                    rollDice()
                }
                last_x = x
                last_y = y
                last_z = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return  //this method isn't used
    }

    companion object {
        private const val UPDATE_DELAY = 50
        private const val SHAKE_THRESHOLD = 5000
    }
}