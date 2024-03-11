package com.example.composemusicplayer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import com.example.composemusicplayer.player.Service.JetAudioService
import com.example.composemusicplayer.ui.AudioScreen.HomeScreen
import com.example.composemusicplayer.ui.theme.ComposeMusicPlayerTheme
import com.example.composemusicplayer.viewModel.AudioViewModel
import com.example.composemusicplayer.viewModel.UIEvents
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val audioViewModel: AudioViewModel by viewModels()
    private var isServiecRunning = false

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeMusicPlayerTheme {
                val permissionState =
                    rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
                val lifeCycleOwner = LocalLifecycleOwner.current
                DisposableEffect(key1 = lifeCycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            permissionState.launchPermissionRequest()
                        }
                    }
                    lifeCycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifeCycleOwner.lifecycle.removeObserver(observer) }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        progress = audioViewModel.progress,
                        onProgress = {
                            audioViewModel.onUIEvents(UIEvents.SeekTo(it))
                        },
                        isAudioPlaying = audioViewModel.isPlaying,
                        currentPlayingAudio = audioViewModel.currentSelectedAudio,
                        audioList = audioViewModel.audioList,
                        onStart = { audioViewModel.onUIEvents(UIEvents.PlayPause) },
                        onItemClick = {
                            audioViewModel.onUIEvents(UIEvents.SelectedAudioChange(it))
                            startService()
                        }
                    ) {
                        audioViewModel.onUIEvents(UIEvents.SeekToNext)
                    }
                }
            }
        }
    }

    private fun startService() {
        if (!isServiecRunning) {
            val intent = Intent(this, JetAudioService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isServiecRunning = true
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeMusicPlayerTheme {
        Greeting("Android")
    }
}