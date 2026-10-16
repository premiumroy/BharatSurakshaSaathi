package com.bharatsuraksha.saathi.ui.session

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds an image shared into the app from another app (share-sheet with a photo),
 * until the image-check screen picks it up.
 */
object IncomingImageStore {
    private val _uri = MutableStateFlow<Uri?>(null)
    val uri: StateFlow<Uri?> = _uri.asStateFlow()

    fun post(value: Uri) { _uri.value = value }

    fun consume() { _uri.value = null }
}
