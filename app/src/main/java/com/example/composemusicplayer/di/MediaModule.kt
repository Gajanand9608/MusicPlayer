package com.example.composemusicplayer.di

import android.app.NotificationManager
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.example.composemusicplayer.player.Service.JetAudioServiceHandler
import com.example.composemusicplayer.player.notification.JetNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun providesAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .setUsage(C.USAGE_MEDIA).build()

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun providesExoplayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            .setAudioAttributes(audioAttributes, true)
            .setTrackSelector(DefaultTrackSelector(context)).build()
    }

    @Provides
    @Singleton
    fun providesMediaSession(@ApplicationContext context: Context, player: ExoPlayer) : MediaSession{
        return MediaSession.Builder(context, player).build()
    }

    @Provides
    @Singleton
    fun providesNotificationManager(@ApplicationContext context: Context, exoPlayer: ExoPlayer)  : JetNotificationManager {
        return JetNotificationManager(context, exoPlayer)
    }

    @Provides
    @Singleton
    fun providesServiceHandler(exoPlayer: ExoPlayer) : JetAudioServiceHandler {
        return JetAudioServiceHandler(exoPlayer)
    }
}