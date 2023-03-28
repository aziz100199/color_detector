package com.note.colordetext.activities
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.note.colordetext.R
import com.note.colordetext.databinding.ActivityColorBinding

import com.note.colordetext.handler.ColorDetectHandler
import com.note.colordetext.model.UserColor
import com.note.colordetext.utils.timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ColorActivity : AppCompatActivity() {
    private var binding: ActivityColorBinding? = null

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 26
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        private const val REQUEST_CODE = 112

    }

    private lateinit var cameraExecutor: ExecutorService

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(this)
    }

    // Used to bind the lifecycle of cameras to the lifecycle owner
    private val cameraProvider by lazy {
        cameraProviderFuture.get()
    }
    private var isBackCamera = true

    // Select back camera as a default
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private val colorDetectHandler = ColorDetectHandler()

    private var timerTask: Job? = null

    private var currentColor = UserColor()

    private var isImageShown = false

    private var currentColorList: MutableList<UserColor> =
        mutableListOf()

//    private val colorAdapter: ColorAdapter by lazy {
//        ColorAdapter(this) {
////            val detailDialog = ColorDetailDialog(this, it, removeColorInList)
//            detailDialog.show()
//        }
//    }
/*

    private val colorsFragment: ColorsFragment by lazy {
        ColorsFragment()
    }
    private val colorViewModel: ColorViewModel by lazy {
        ViewModelProvider(
            this,
            ColorViewModel.ColorViewModelFactory(application)
        )[ColorViewModel::class.java]
    }
*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColorBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        initControls(savedInstanceState)
        initEvents()
    }

    fun getLayoutId(): Int = R.layout.activity_color

    fun initControls(savedInstanceState: Bundle?) {
        if (allPermissionsGranted()) {

            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        binding?.apply {
            val layoutManager =
                LinearLayoutManager(this@ColorActivity, LinearLayoutManager.HORIZONTAL, false)
            rvColor.layoutManager = layoutManager
            rvColor.setHasFixedSize(true)
//        rv_color.adapter = colorAdapter
        }


    }


    fun initEvents() {
        binding?.apply {
            btnPickColor.setOnClickListener {
                addColor()
            }
            btnAddListColor.setOnClickListener {
                if (currentColorList.isNotEmpty()) {
//                val colorDialog = ColorDialog(this, colorViewModel, colorAdapter, clearColorList)
//                colorDialog.show()
                }
            }

            btnChangeCamera.setOnClickListener {
                if (!isImageShown) {
                    if (isBackCamera) {
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        isBackCamera = false
                    } else {
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        isBackCamera = true
                    }
                    startCamera()
                }
            }

            btnPickImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE)
            }

            btnShowCamera.setOnClickListener {
                if (isImageShown) {
                    btnShowCamera.visibility = View.GONE
                    imageView.visibility = View.GONE
                    isImageShown = false
                    startCamera()
                }
            }

            /* btn_show_colors.setOnClickListener {
                 showBottomSheetFragment()
             }*/
        }


    }


    private fun startCamera() {

        cameraProviderFuture.addListener({

            timerTask?.cancel()

            // Preview
//           val preview =
//                Preview.Builder()
//                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                    .setTargetRotation(binding?.cameraPreview?.display?.rotation!!)
//                    .build()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding?.cameraPreview?.surfaceProvider)
//                    it.setSurfaceProvider(binding?.cameraPreview?.createSurfaceProvider())
                }


            timerTask = CoroutineScope(Dispatchers.Main).timer(1000) {
                Log.d("jejeje", "Color startCamera start")
                currentColor =
                    colorDetectHandler.detect(binding?.cameraPreview!!, binding?.pointer!!)
                Log.d("jejeje", "Color : ${currentColor.hex}")
                binding?.txtHex?.text = currentColor.hex
                binding?.cardColor?.setCardBackgroundColor(Color.parseColor(currentColor.hex))

//                withContext(Dispatchers.Main) {
//                    Log.d("jejeje", "startCamera:currentColor.hex...${currentColor.hex} ")
//                    binding?.txtHex?.text = currentColor.hex
//                    binding?.cardColor?.setCardBackgroundColor(Color.parseColor(currentColor.hex))
//                }
            }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
//                preview.setSurfaceProvider(binding?.cameraPreview?.surfaceProvider)

            } catch (exc: Exception) {
                Log.e("jejeje", "Use case binding failed",)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val x = event.x

            val y = when {
                event.y < binding?.guidelineTop!!.y -> binding?.guidelineTop!!.y
                event.y > binding?.guidelineBottom1!!.y -> binding?.guidelineBottom1!!.y - binding?.pointer?.height!!
                else -> event.y
            }

            setPointerCoordinates(x, y)
        }

        return super.onTouchEvent(event)
    }

    private fun setPointerCoordinates(x: Float, y: Float) {

        binding?.pointer!!.x = x
        binding?.pointer!!.y = y

        val marginBottom = this.resources.getDimension(R.dimen._20sdp)
        binding?.cardColorPreview!!.y = y - marginBottom - binding?.pointer!!.height


        val cardColorPreviewX = when {
            x < binding?.guidelineLeft!!.x ->
                x
            x >= binding?.guidelineRight!!.x -> {
                x - binding?.cardColorPreview!!.width
            }
            else ->
                x - (binding?.cardColorPreview!!.width / 2)
        }

        binding?.cardColorPreview!!.x = cardColorPreviewX


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {

            if (!isImageShown) {
                binding?.imageView!!.visibility = View.VISIBLE
                binding?.btnShowCamera!!.visibility = View.VISIBLE
                isImageShown = true
            }
            if (data?.data != null) {
                binding?.imageView!!.setImageURI(data.data)
                startDetectColorFromImage(decodeUriToBitmap(data.data!!))
            }


            /*   Glide.with(this)
                   .asBitmap()
                   .load(data?.data)
                   .into(object : CustomTarget<Bitmap>() {
                       override fun onResourceReady(
                           resource: Bitmap,
                           transition: Transition<in Bitmap>?
                       ) {
                           image_view.setImageBitmap(resource)
                           startDetectColorFromImage(resource)
                       }

                       override fun onLoadCleared(placeholder: Drawable?) = Unit
                   })*/
        }
    }

    private fun startDetectColorFromImage(bitmap: Bitmap) {

        cameraProvider.unbindAll()

        timerTask?.cancel()

        /*  For debugging
          val w = image_view.width
          val h = image_view.height
          */

        // Set Pointer Coordinates at center of image view
        setPointerCoordinates(binding?.imageView!!.width / 2f, binding?.imageView!!.height / 2f)

        /**
         * At this point I don't know how to explain  this code.
         * But I will definitely add it in the future
         */

        var isFitHorizontally = true

        var marginTop: Float = binding?.layoutTop!!.height.toFloat()

        var marginLeft = 0f

        val ratio = if (bitmap.width >= bitmap.height) {
            bitmap.width / (binding?.imageView!!.width * 1.0f)
        } else {
            isFitHorizontally = false
            bitmap.height / (binding?.imageView!!.height * 1.0f)
        }


        if (isFitHorizontally) {
            marginTop += (binding?.imageView!!.height - bitmap.height / ratio) / 2
        } else {
            marginLeft += (binding?.imageView!!.width - bitmap.width / ratio) / 2
        }

        timerTask = CoroutineScope(Dispatchers.Default).timer(1000) {
            Log.d("jejeje", "Color startDetectColorFromImage start")

            currentColor =
                colorDetectHandler.detect(bitmap, binding?.pointer!!, marginTop, marginLeft, ratio)
               Log.d("jejeje", "Color startDetectColorFromImage: ${currentColor.hex}")

            withContext(Dispatchers.Main) {
                Log.d("jejeje", "Color startDetectColorFromImage : ${currentColor.hex}")
                binding?.txtHex!!.text = currentColor.hex
                binding?.cardColor!!.setCardBackgroundColor(Color.parseColor(currentColor.hex))
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun addColor() {
        try {
            //Check color before add to list
            Color.parseColor(currentColor.hex)

            colorDetectHandler.convertRgbToHsl(currentColor)
            currentColorList.add(0, currentColor)

//            colorAdapter.notifyData(currentColorList)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(
                this,
                resources.getString(R.string.unknown_color),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val clearColorList: () -> Unit = {
        currentColorList.clear()
//        colorAdapter.notifyData(currentColorList)
    }

    /*private fun showBottomSheetFragment() {
        colorsFragment.show(supportFragmentManager, colorsFragment.tag)
    }*/

    private val removeColorInList: (UserColor) -> Unit = {
        currentColorList.remove(it)
//        colorAdapter.notifyData(currentColorList)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerTask?.cancel()
        cameraProvider.unbindAll()
        cameraExecutor.shutdown()
    }

    private fun decodeUriToBitmap(uri: Uri): Bitmap = try {
        val inputStream = contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        (binding?.imageView!!.drawable as BitmapDrawable).bitmap
    }

}