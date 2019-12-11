package dev.dnights.scopedstoragesample.SAF

import android.os.Bundle
import dev.dnights.scopedstoragesample.BaseActivity
import dev.dnights.scopedstoragesample.R

class StorageAccessFrameworkActivity : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saf)
    }
}