package com.ramanhmr.audioplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ramanhmr.audioplayer.databinding.ActivityMainBinding
import com.ramanhmr.audioplayer.repositories.FileRepository
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fileRepository: FileRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionCheck()

        binding.tvMediaFiles.setOnClickListener {
            val songs = fileRepository.getAllFiles()
            binding.tvMediaFiles.text = songs.size.toString()
            val oneSong = songs[0]
            Toast.makeText(
                this,
                "${oneSong.title}\n${oneSong.artist}\n${oneSong.album}\n${oneSong.duration}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO: 28-Jul-21 load Media Files
            } else {
                // TODO: 28-Jul-21 ask again politely
                permissionRequest()
            }
        }
    }

    private fun permissionCheck() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequest()
        } else {
            Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun permissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE
        )
    }

    companion object {
        private const val REQUEST_CODE = 3001
    }
}