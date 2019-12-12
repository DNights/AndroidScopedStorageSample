package dev.dnights.scopedstoragesample.SAF

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import dev.dnights.scopedstoragesample.BaseActivity
import dev.dnights.scopedstoragesample.R
import dev.dnights.scopedstoragesample.SAF.adepter.FileClickListeners
import dev.dnights.scopedstoragesample.SAF.adepter.SAFFileAdepter
import dev.dnights.scopedstoragesample.SAF.data.SAFFileData
import kotlinx.android.synthetic.main.activity_saf.*
import java.io.FileOutputStream

class StorageAccessFrameworkActivity : BaseActivity() {

    private val OPEN_DIRECTORY_REQUEST_CODE = 1000
    private val WRITE_REQUEST_CODE: Int = 1100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saf)

        button_create_file.setOnClickListener {
            createFile("temp_file", "image/*")
        }

        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            }
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }

        startActivityForResult(intent, OPEN_DIRECTORY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val directoryUri = data?.data ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                contentResolver.takePersistableUriPermission(
                    directoryUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            getFileList(directoryUri)

        }

        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                Log.i("test", "Uri: $uri")
                writeFile(uri, "temp".toByteArray())
            }
        }
    }

    private fun getFileList(directoryUri: Uri) {
        val documentsTree = DocumentFile.fromTreeUri(application, directoryUri) ?: return
        val childDocuments = documentsTree.listFiles()

        val adepter = SAFFileAdepter(object : FileClickListeners {
            override fun onClick(sAFFileData: SAFFileData) {
                if(sAFFileData.isDirectory){
                    getFileList(sAFFileData.uri)
                }
            }

            override fun onLongClick(sAFFileData: SAFFileData) {
                removeFile(sAFFileData.uri)
                getFileList(sAFFileData.parentFileUri)
            }

        })

        val fileList = childDocuments.map {
            SAFFileData(
                it.name ?: "unknown name",
                it.type ?: "unknown type",
                it.uri,
                it.isDirectory,
                it.parentFile?.uri?: Uri.EMPTY
            )
        }
        adepter.setFileList(fileList)

        rv_saf_file_list.layoutManager = LinearLayoutManager(rv_saf_file_list.context)
        rv_saf_file_list.adapter = adepter


    }

    private fun createFile(fileName: String, mimeType: String){
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mimeType
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
        } else {
            TODO("VERSION.SDK_INT < KITKAT")
        }

        startActivityForResult(intent, WRITE_REQUEST_CODE)
    }

    private fun writeFile(uri: Uri, data: ByteArray) {
        contentResolver.openFileDescriptor(uri, "w").use {
            FileOutputStream(it!!.fileDescriptor).use {fos->
                fos.write(data)
                fos.close()
            }
        }
    }

    private fun removeFile(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            DocumentsContract.deleteDocument(contentResolver, uri)
        }
        Toast.makeText(applicationContext, "Remove File : $uri", Toast.LENGTH_SHORT).show()
    }

}