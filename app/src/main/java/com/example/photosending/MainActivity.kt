package com.example.photosending

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var photoUri: Uri
    private lateinit var mailAddress: String

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val editMail = findViewById<EditText>(R.id.editMail)
        val btnSend = findViewById<Button>(R.id.btnSend)

        btnSend.setOnClickListener {

            // メール取得
            mailAddress = editMail.text.toString()

            // 保存ファイル
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

            // カメラIntent
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

            // メール起動
            val mailIntent = Intent(Intent.ACTION_SEND)

            mailIntent.type = "image/jpeg"

            mailIntent.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf(mailAddress)
            )

            mailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "写真送信"
            )

            mailIntent.putExtra(
                Intent.EXTRA_TEXT,
                "撮影した写真です"
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
    }
}