package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ProCameraScreen
import com.example.ui.components.PermissionScreen
import com.example.ui.theme.CameraDarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ProCameraViewModel

class MainActivity : ComponentActivity() {

    private val cameraViewModel: ProCameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val context = LocalContext.current
                val uiState by cameraViewModel.uiState.collectAsStateWithLifecycle()

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
                    cameraViewModel.setCameraPermissionGranted(cameraGranted)
                }

                LaunchedEffect(Unit) {
                    val hasCam = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    cameraViewModel.setCameraPermissionGranted(hasCam)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CameraDarkBackground
                ) {
                    if (uiState.hasCameraPermission) {
                        ProCameraScreen(viewModel = cameraViewModel)
                    } else {
                        PermissionScreen(
                            onRequestPermission = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
