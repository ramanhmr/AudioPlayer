package com.ramanhmr.audioplayer.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ramanhmr.audioplayer.R
import com.ramanhmr.audioplayer.utils.PermissionUtils
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class PermissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PermissionUtils.hasPermission(applicationContext)) toMain()

        setContentView(R.layout.activity_permission)
        findViewById<Button>(R.id.btn_ask_permission).setOnClickListener {
            PermissionUtils.requestPermission(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.REQUEST_CODE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            toMain()
        }
    }

    private fun toMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}