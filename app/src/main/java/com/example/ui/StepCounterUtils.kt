package com.example.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Calendar

object StepCounterUtils {
    fun getStepCountFlow(context: Context): Flow<Int> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        val prefs = context.getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    val currentSteps = event.values[0].toInt()
                    
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    val savedDay = prefs.getLong("saved_day", 0L)
                    var initialSteps = prefs.getInt("initial_steps", -1)

                    if (savedDay != today || initialSteps == -1) {
                        initialSteps = currentSteps
                        prefs.edit()
                            .putLong("saved_day", today)
                            .putInt("initial_steps", initialSteps)
                            .apply()
                    }

                    val dailySteps = (currentSteps - initialSteps).coerceAtLeast(0)
                    trySend(dailySteps)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (stepSensor != null) {
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            trySend(0) // Emitting 0 if no sensor
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
