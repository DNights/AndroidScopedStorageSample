package dev.dnights.scopedstoragesample.mediastore

import dev.dnights.scopedstoragesample.BaseActivity
import dev.dnights.scopedstoragesample.R

import android.Manifest
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dev.dnights.scopedstoragesample.mediastore.adepter.FileClickListeners
import dev.dnights.scopedstoragesample.mediastore.adepter.MediaFileAdepter
import dev.dnights.scopedstoragesample.mediastore.data.MediaFileData
import kotlinx.android.synthetic.main.activity_media_store_file.*
import java.io.FileOutputStream
import java.util.*

class MediaStoreFileActivity : BaseActivity() {

    val DELETE_PERMISSION_REQUEST = 9901

    var curType = MediaStoreFileType.IMAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_store_file)

        /**
         * 읽기 권환 확인
         */
        if (grantExternalStoragePermission()) {
            init()
        } else {
            Toast.makeText(this, "권환이 없습니다.", Toast.LENGTH_LONG).show()
        }

    }

    private fun init() {
        /**
         * 이전 저장소 방식을 사용하는지 확인
         */

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            Toast.makeText(this, "min sdk is Q. Build.VERSION.SDK_INT : ${Build.VERSION.SDK_INT}", Toast.LENGTH_LONG).show()
            return
        }

        if (Environment.isExternalStorageLegacy()) {
            Toast.makeText(this, "isExternalStorageLegacy true.", Toast.LENGTH_LONG).show()
            return
        }

        initMediaFileAdepter()
        initLayout()
    }

    private fun initMediaFileAdepter() {
        val adepter = MediaFileAdepter(object : FileClickListeners {
            override fun onClick(mediaFileData: MediaFileData) {

            }

            override fun onLongClick(mediaFileData: MediaFileData) {
                removeMediaFile(this@MediaStoreFileActivity, mediaFileData.uri)
            }
        })
        rv_file_list.adapter = adepter
        rv_file_list.layoutManager = LinearLayoutManager(rv_file_list.context)
        (rv_file_list.adapter as MediaFileAdepter).setFileList(
            getFileList(
                this,
                MediaStoreFileType.IMAGE
            )
        )
    }

    private fun initLayout() {
        button_read_image.setOnClickListener {
            setAdepterList(MediaStoreFileType.IMAGE)
        }

        button_read_audio.setOnClickListener {
            setAdepterList(MediaStoreFileType.AUDIO)
        }

        button_read_video.setOnClickListener {
            setAdepterList(MediaStoreFileType.VIDEO)
        }

        button_create_image.setOnClickListener {
            createFile(
                this,
                "temp_image",
                MediaStoreFileType.IMAGE,
                "it is temp image".toByteArray()
            )
            (rv_file_list.adapter as MediaFileAdepter).setFileList(getFileList(this, curType))
        }

        button_create_audio.setOnClickListener {
            createFile(
                this,
                "temp_audio",
                MediaStoreFileType.AUDIO,
                "it is temp audio".toByteArray()
            )
            (rv_file_list.adapter as MediaFileAdepter).setFileList(getFileList(this, curType))
        }

        button_create_video.setOnClickListener {
            createFile(
                this,
                "temp_video",
                MediaStoreFileType.VIDEO,
                "it is temp video".toByteArray()
            )
            (rv_file_list.adapter as MediaFileAdepter).setFileList(getFileList(this, curType))
        }

    }

    private fun setAdepterList(type: MediaStoreFileType) {
        val adepter = (rv_file_list.adapter as MediaFileAdepter)
        adepter.setFileList(getFileList(this, type))
        curType = type
    }

    private fun grantExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("test", "Permission is granted")
                true
            } else {
                Log.d("test", "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    1
                )
                false
            }
        } else {
            Toast.makeText(this, "External Storage Permission is Grant", Toast.LENGTH_SHORT).show()
            Log.d("test", "External Storage Permission is Grant ")
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("test", "Permission: ${permissions[0]} was ${grantResults[0]}")
            init()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_PERMISSION_REQUEST) {
            removeMediaFile(this, removeUri)
        }
    }

    fun createFile(
        context: Context,
        fileName: String,
        fileType: MediaStoreFileType,
        fileContents: ByteArray
    ) {
        val contentValues = ContentValues()
        /**
         * allowed directories are [DCIM, Pictures]
         */
        contentValues.put(
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            Environment.DIRECTORY_DCIM + fileType.pathByDCIM
        )
        contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
        contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, fileType.mimeType)
        contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 1)

        val uri = context.contentResolver.insert(
            fileType.externalContentUri,
            contentValues
        )

        val parcelFileDescriptor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            context.contentResolver.openFileDescriptor(uri!!, "w", null)
        } else {
            TODO("VERSION.SDK_INT < KITKAT")
        }

        val fileOutputStream = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)
        fileOutputStream.write(fileContents)
        fileOutputStream.close()

        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(uri, contentValues, null, null)
    }

    fun getFileList(context: Context, type: MediaStoreFileType): List<MediaFileData> {

        val fileList = mutableListOf<MediaFileData>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_TAKEN
        )

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_TAKEN} DESC"

        val cursor = context.contentResolver.query(
            type.externalContentUri,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val dateTakenColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateTaken = Date(cursor.getLong(dateTakenColumn))
                val displayName = cursor.getString(displayNameColumn)
                val contentUri = Uri.withAppendedPath(
                    type.externalContentUri,
                    id.toString()
                )

                Log.d(
                    "test",
                    "id: $id, display_name: $displayName, date_taken: $dateTaken, content_uri: $contentUri\n"
                )

                fileList.add(MediaFileData(id, dateTaken, displayName, contentUri))
            }
        }

        return fileList
    }

    private var removeUri = Uri.EMPTY

    fun removeMediaFile(context: Context, uri: Uri) {
        try {
            uri.let {
                context.contentResolver.delete(uri, null, null)
                Log.d("test", "Removed MediaStore: $it")
            }

            (rv_file_list.adapter as MediaFileAdepter).setFileList(getFileList(this, curType))

        } catch (e: RecoverableSecurityException) {
            val intentSender = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                e.userAction.actionIntent.intentSender
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            intentSender?.let {
                ActivityCompat.startIntentSenderForResult(
                    context as Activity,
                    intentSender,
                    DELETE_PERMISSION_REQUEST,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            }
        }
        removeUri = uri
    }

    enum class MediaStoreFileType(
        val externalContentUri: Uri,
        val mimeType: String,
        val pathByDCIM: String
    ) {
        IMAGE(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*", "/image"),
        AUDIO(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "audio/*", "/audio"),
        VIDEO(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*", "/video");
    }

}