package dev.dnights.scopedstoragesample.SAF.data

import android.net.Uri

data class SAFFileData(
    val name: String,
    val type: String,
    val uri: Uri,
    val isDirectory: Boolean
)
