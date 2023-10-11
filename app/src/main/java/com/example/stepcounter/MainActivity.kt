package com.example.stepcounter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.stepcounter.ui.theme.StepCounterTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var hasPermissions = false

        var requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                hasPermissions = isGranted
            }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasPermissions = true
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) -> {
        }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        setContent {
            StepCounterTheme {
                // A surface container using the 'background' color from the theme
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        if (hasPermissions) {
                            StepCounter()
                        } else {
                            Text(text = "Permissions disabled")
                        }
                    }
            }
        }
    }
}

@Composable
fun StepCounter(modifier: Modifier = Modifier) {
    val tag = "StepCounter"

    val ctx = LocalContext.current

    var sensorManager: SensorManager =
        ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    var stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    var stepCount by remember {
        mutableStateOf(0f)
    }

    val stepCounterSensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            Log.d(tag, "sensor changed")
            if(event?.sensor?.type !== Sensor.TYPE_STEP_COUNTER) return
            Log.d(tag, event.values[0].toString())
            stepCount = event.values[0]
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d(tag, "Accuracy changed")
        }
    }

    if (stepCounterSensor == null) {
        Toast.makeText(ctx, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
    } else {
        try {
            sensorManager.registerListener(stepCounterSensorEventListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } catch (e : Error) {
            Log.e(tag, e.toString())
        }
    }

    Box(modifier = Modifier
        .size(200.dp)
        .border(width = 3.dp, color = Color.Green, shape = CircleShape)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text="Total Steps", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stepCount.toString(),
                modifier = modifier
            )
        }

    }
}