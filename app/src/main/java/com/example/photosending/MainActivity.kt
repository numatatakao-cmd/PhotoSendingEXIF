package com.example.photosending

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationServices
import java.io.File
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

class MainActivity : AppCompatActivity() {

    private lateinit var photoUri: Uri
    private lateinit var mailAddress: String
    private lateinit var editMail: AutoCompleteTextView

    private var latitude = 0.0
    private var longitude = 0.0

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val prefs =
            getSharedPreferences(
                "mail_history",
                MODE_PRIVATE
            )

        val savedMails =
            prefs.getStringSet(
                "mails",
                mutableSetOf()
            ) ?: mutableSetOf()

        editMail =
            findViewById<AutoCompleteTextView>(R.id.editMail)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            savedMails.toList()
        )

        editMail.setAdapter(adapter)

        editMail.threshold = 1

        editMail.setOnClickListener {
            editMail.showDropDown()
        }


        // 位置情報許可
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )

        btnSend.setOnClickListener {

            mailAddress = editMail.text.toString()
            savedMails.add(mailAddress)

            prefs.edit()
                .putStringSet(
                    "mails",
                    savedMails
                )
                .apply()

            // GPS取得
            getLocation()

            // 写真保存先
            val photoFile = File.createTempFile(
                "photo_",
                ".jpg",
                cacheDir
            )

            // URI
            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                photoFile
            )

            // カメラ起動
            val cameraIntent =
                Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            cameraIntent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                photoUri
            )

            startActivityForResult(
                cameraIntent,
                CAMERA_REQUEST_CODE
            )
        }
    }

    private fun getLocation() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                if (location != null) {

                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode == CAMERA_REQUEST_CODE) {

            val mapUrl =
                "https://maps.google.com/?q=$latitude,$longitude"

            val body = """
                Latitude: $latitude
                Longitude: $longitude
                
                $mapUrl
            """.trimIndent()

            // メール
            val mailIntent = Intent(Intent.ACTION_SEND)

            mailIntent.type = "image/jpeg"

            mailIntent.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf(mailAddress)
            )

            mailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "位置情報付き写真"
            )

            mailIntent.putExtra(
                Intent.EXTRA_TEXT,
                body
            )

            mailIntent.putExtra(
                Intent.EXTRA_STREAM,
                photoUri
            )

            mailIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            startActivity(
                Intent.createChooser(
                    mailIntent,
                    "メール送信"
                )
            )
        }
        editMail.setText(mailAddress)
    }
}