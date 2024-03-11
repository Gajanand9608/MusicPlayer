package com.example.composemusicplayer.data.local.repository

import com.example.composemusicplayer.data.local.model.Audio
import com.example.composemusicplayer.data.local.model.ContentResolverHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val contentResolver: ContentResolverHelper,
) {

    suspend fun getAudioData() : List<Audio>  = withContext(Dispatchers.IO){
        contentResolver.getAudioData()
    }
}