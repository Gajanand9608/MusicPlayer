package com.example.composemusicplayer.player.Service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class JetAudioServiceHandler @Inject constructor(private val exoPlayer: ExoPlayer) :
    Player.Listener {
    private val _audioState: MutableStateFlow<JetAudioState> =
        MutableStateFlow(JetAudioState.Initial)
    val audioState: StateFlow<JetAudioState> = _audioState.asStateFlow()

    private var job: Job? = null

    init {
        exoPlayer.addListener(this)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun addMediaItemList(mediaItems: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition: Long = 0,
        ) {
        when (playerEvent) {
            PlayerEvent.Backward -> {
                exoPlayer.seekBack()
            }

            PlayerEvent.Forward -> {
                exoPlayer.seekForward()
            }

            PlayerEvent.PlayPause -> {
                checkPlayOrPause()
            }

            PlayerEvent.SeekTo -> {
                exoPlayer.seekTo(seekPosition)
            }

            PlayerEvent.SeekToNext -> {
                exoPlayer.seekToNext()
            }

            PlayerEvent.SelectedAudioChange -> {
                when (selectedAudioIndex) {
                    exoPlayer.currentMediaItemIndex -> {
                        checkPlayOrPause()
                    }

                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                        _audioState.value = JetAudioState.Playing(isPlaying = true)
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }

            }

            PlayerEvent.Stop -> {
                stopProgressUpdate()
            }

            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo((exoPlayer.duration * playerEvent.newProgress).toLong())
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
                _audioState.value = JetAudioState.Buffering(exoPlayer.currentPosition)
            }

            ExoPlayer.STATE_READY -> {
                _audioState.value = JetAudioState.Ready(exoPlayer.duration)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
       _audioState.value = JetAudioState.Playing(isPlaying)
        _audioState.value = JetAudioState.CurrentPlayingAudio(exoPlayer.currentMediaItemIndex)
        if(isPlaying){
            GlobalScope.launch(Dispatchers.Main) {
                startProgressUpdate()
            }
        }else{
            stopProgressUpdate()
        }

    }

    private suspend fun checkPlayOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            stopProgressUpdate()
        } else {
            exoPlayer.play()
            _audioState.value = JetAudioState.Playing(isPlaying = true)
            startProgressUpdate()
        }
    }

    private suspend fun startProgressUpdate() {
        job.run {
            while (true) {
                _audioState.value = JetAudioState.Progress(exoPlayer.currentPosition)
            }
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _audioState.value = JetAudioState.Playing(isPlaying = false)
    }

}

sealed class PlayerEvent {
    data object PlayPause : PlayerEvent()
    data object SelectedAudioChange : PlayerEvent()
    data object Backward : PlayerEvent()
    data object SeekToNext : PlayerEvent()
    data object Forward : PlayerEvent()
    data object SeekTo : PlayerEvent()
    data object Stop : PlayerEvent()
    data class UpdateProgress(val newProgress: Float) : PlayerEvent()
}

sealed class JetAudioState {
    data object Initial : JetAudioState()
    data class Ready(val duration: Long) : JetAudioState()
    data class Progress(val progress: Long) : JetAudioState()
    data class Buffering(val progress: Long) : JetAudioState()
    data class Playing(val isPlaying: Boolean) : JetAudioState()
    data class CurrentPlayingAudio(val mediaItemIndex: Int) : JetAudioState()
}