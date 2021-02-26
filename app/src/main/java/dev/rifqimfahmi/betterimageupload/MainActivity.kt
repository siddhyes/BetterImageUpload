package dev.rifqimfahmi.betterimageupload

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import dev.rifqimfahmi.betterimageupload.util.AndroidUtilities
import java.io.FileInputStream
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var btnChooseImg: Button? = null
    private var originalImg: ImageView? = null
    private var originalDimension: TextView? = null
    private var originalSize: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindView()
        setupBtnChooseImg()
    }

    private fun bindView() {
        btnChooseImg = findViewById(R.id.btn_choose_image)
        originalImg = findViewById(R.id.original_image)
        originalDimension = findViewById(R.id.original_dimension)
        originalSize = findViewById(R.id.original_size)
    }

    private fun setupBtnChooseImg() {
        btnChooseImg?.setOnClickListener {
            if (!isPermissionGranted()) {
                requestRequiredPermission()
            } else {
                chooseImage()
            }
        }
    }

    private fun chooseImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        val chooser = Intent.createChooser(intent, "Select Picture")
        startActivityForResult(chooser, REQUEST_SELECT_PICTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_PICTURE) {
            onSuccessReturnFromChooseImage(data)
        }
    }

    private fun onSuccessReturnFromChooseImage(data: Intent?) {
        val imageUri = data?.data ?: return
        loadOriginalImage(imageUri)
    }

    private fun loadOriginalImage(imageUri: Uri) {
        val filePath = AndroidUtilities.getPath(this, imageUri)
        val fileSize = updateImageMetaDataSize(filePath)
        val bmOptions = updateImageMetaDataDimension(filePath)
        originalSize?.text = "${fileSize / 1024} KB"
        originalDimension?.text = "${bmOptions.outWidth}x${bmOptions.outHeight}"
        Glide.with(this).load(filePath).into(originalImg!!)
    }

    private fun updateImageMetaDataSize(filePath: String?): Long {
        var fileSize: Long = 0
        try {
            val fileInput = FileInputStream(filePath)
            fileSize = fileInput.channel.size()
            fileInput.close()
        } catch (ignore: Exception) {}
        return fileSize
    }

    private fun updateImageMetaDataDimension(filePath: String?): BitmapFactory.Options {
        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, bmOptions)
        return bmOptions
    }

    private fun isPermissionGranted(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRequiredPermission() {
        ActivityCompat.requestPermissions(
            this,
            PERMISSIONS_STORAGE,
            REQUEST_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        const val REQUEST_SELECT_PICTURE = 1
        const val REQUEST_EXTERNAL_STORAGE = 2
    }
}