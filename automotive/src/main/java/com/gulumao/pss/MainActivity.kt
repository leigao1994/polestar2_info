package com.gulumao.pss

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.car.Car
import android.car.CarInfoManager
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.property.CarPropertyManager.CarPropertyEventCallback
import android.content.pm.PackageManager
import android.util.Log
import android.widget.TextView


class MainActivity : AppCompatActivity() {
    private lateinit var car: Car
    private lateinit var carPropertyManager: CarPropertyManager
    private val callback = object : CarPropertyEventCallback {
        private var speedRaw: CarPropertyValue<Float>? = null
        private var speedDisplay: CarPropertyValue<Float>? = null
        private var speedUnit: CarPropertyValue<Int>? = null

        override fun onChangeEvent(value: CarPropertyValue<*>?) {
            when (value?.propertyId) {
                VehiclePropertyIds.PERF_VEHICLE_SPEED -> speedRaw =
                    value as CarPropertyValue<Float>?

                VehiclePropertyIds.PERF_VEHICLE_SPEED_DISPLAY -> speedDisplay =
                    value as CarPropertyValue<Float>?

                VehiclePropertyIds.VEHICLE_SPEED_DISPLAY_UNITS -> speedUnit =
                    value as CarPropertyValue<Int>?
            }
            updateText(speedRaw.toString())
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initCar()
        carPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

        carPropertyManager.registerCallback(
            callback,
            VehiclePropertyIds.PERF_VEHICLE_SPEED,
            CarPropertyManager.SENSOR_RATE_NORMAL
        )
    }

    private fun updateText(s: String) {
        val v = findViewById<TextView>(R.id.t1)
        v.text = s
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up
        carPropertyManager.unregisterCallback(callback)
        car.disconnect()
    }

    override fun onPause() {
        if(car.isConnected) {
            car.disconnect()
        }

        super.onPause()
    }

    override fun onResume() {
        if (!car.isConnected) {
            car.connect()
        }
        super.onResume()
    }

    private fun initCar() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE))
            return

        if (::car.isInitialized)
            return

        car = Car.createCar(this)
    }
}