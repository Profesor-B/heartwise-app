package com.example.heartwise

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import com.example.heartwise.databinding.ActivityCameraMonitorBinding
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit
typealias OpencvListener = (message: String, bitmap: Bitmap) -> Unit

class CameraMonitor : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCameraMonitorBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private var caseFile: File? = null
    private var faceDetector: CascadeClassifier? = null

    private lateinit var cameraExecutor: ExecutorService

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraMonitorBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        System.loadLibrary("opencv_java4")
        loadClassifier()
    }

    private fun loadClassifier() {
        val fileResource = resources.openRawResource(R.raw.haarcascade_frontalface_alt)
        val cascadeDir = getDir("cascade",Context.MODE_PRIVATE)
        caseFile = File(cascadeDir,"haarcascade_frontalface_alt.xml")
        val fos = FileOutputStream(caseFile)
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while ((fileResource.read(buffer).also { bytesRead = it }) != -1) {
            fos.write(buffer, 0, bytesRead)
        }
        fileResource.close()
        fos.close()

        faceDetector = CascadeClassifier(caseFile!!.absolutePath)
        if(faceDetector!!.empty()) {
            faceDetector = null;
        }
        cascadeDir.delete();
    }


    private fun takePhoto() {
        Toast.makeText(this,"Take photo not implemented yet.",Toast.LENGTH_SHORT).show();
    }

    private fun captureVideo() {
        Toast.makeText(this,"Video capture not implemented yet.",Toast.LENGTH_SHORT).show();
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder().build();

            val opencvAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LightnessAnalyzer { _, bitmap ->
                        runOnUiThread {
                            try {
                                val rotation = when (viewBinding.opencvImage.display.rotation) {
                                    Surface.ROTATION_0 -> 90
                                    Surface.ROTATION_90 -> 0
                                    Surface.ROTATION_180 -> 270
                                    Surface.ROTATION_270 -> 180
                                    else -> 0
                                }
                                if (rotation == 0) {
                                    viewBinding.opencvImage.setImageBitmap(bitmap)
                                } else {
                                    val matrix = android.graphics.Matrix()
                                    matrix.postRotate(rotation.toFloat())
                                    val rotatedBitmap = Bitmap.createBitmap(
                                        bitmap,
                                        0,
                                        0,
                                        bitmap.width,
                                        bitmap.height,
                                        matrix,
                                        true
                                    )
                                    viewBinding.opencvImage.setImageBitmap(rotatedBitmap)
                                }
                            } catch (ex: Exception) {
                                viewBinding.opencvImage.setImageBitmap(bitmap)
                            }
                        }
                    }.loadClassifier(faceDetector)
                    )
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector,imageCapture,opencvAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).toTypedArray()
    }

    private class LightnessAnalyzer(private val listener:OpencvListener) : ImageAnalysis.Analyzer {

        private var faceDetector: CascadeClassifier? = null;

        public fun loadClassifier(classifier: CascadeClassifier?): LightnessAnalyzer {
            this.faceDetector = classifier;
            return this
        }

        fun Image.yuvToRgba(): Mat {
            val rgbaMat = Mat()

            if (format == ImageFormat.YUV_420_888 && planes.size == 3){
                val chromaPixelStride = planes[1].pixelStride

                if (chromaPixelStride == 2) // chroma channels are interleaved
                {
                    assert(planes[0].pixelStride == 1)
                    assert(planes[2].pixelStride == 2)
                    val yPlane = planes[0].buffer
                    val uvPlane1 = planes[1].buffer
                    val uvPlane2 = planes[2].buffer

                    val yMat = Mat(height, width, CvType.CV_8UC1, yPlane)
                    val uvMat1 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane1)
                    val uvMat2 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane2)
                    val addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr()

                    if (addrDiff > 0) {
                        assert(addrDiff == 1L)
                        Imgproc.cvtColorTwoPlane(yMat,uvMat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
                    } else {
                        assert(addrDiff == -1L)
                        Imgproc.cvtColorTwoPlane(yMat, uvMat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
                    }
                }
                else // chroma channels are not interleaved
                {
                    val yuvBytes = ByteArray(width * (height + height / 2))
                    val yPlane = planes[0].buffer
                    val uPlane = planes[1].buffer
                    val vPlane = planes[2].buffer

                    yPlane.get(yuvBytes, 0, width * height)

                    val chromaRowStride = planes[1].rowStride
                    val chromaRowPadding = chromaRowStride - width / 2

                    var offset = width * height

                    if (chromaRowPadding == 0) {
                        // When the row stride of the chroma channels equals their width, we can copy
                        // the entire channels in one go
                        uPlane.get(yuvBytes, offset, width * height / 4)
                        offset += width * height / 4
                        vPlane.get(yuvBytes, offset, width * height / 4)
                    } else {
                        // When not equal, we need to copy the channels row by row
                        for (i in 0 until height / 2) {
                            uPlane.get(yuvBytes, offset, width / 2)
                            offset += width / 2
                            if (i < height / 2 - 1) {
                                uPlane.position(uPlane.position() + chromaRowPadding)
                            }
                        }
                        for (i in 0 until height / 2) {
                            vPlane.get(yuvBytes, offset, width / 2)
                            offset += width / 2
                            if (i < height / 2 - 1) {
                                vPlane.position(vPlane.position() + chromaRowPadding)
                            }
                        }
                    }

                    val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
                    yuvMat.put(0,0,yuvBytes)
                    Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
                }
            }
            return rgbaMat
        }

        @androidx.annotation.OptIn(ExperimentalGetImage::class)
        override fun analyze(image: ImageProxy) {
            image.image?.let {
                if (it.format == ImageFormat.YUV_420_888 && it.planes.size == 3) {
                    val rgbMatFrame = it.yuvToRgba()
                    val grayFrame = Mat()
                    // Imgproc.cvtColor(rgbMatFrame,grayFrame,6)
                    val faceDetection = MatOfRect();
                    if(faceDetector == null) {
                        Log.e("Detection","No classifier detected");
                    }
                    faceDetector?.detectMultiScale(rgbMatFrame,faceDetection,1.3,5)
                    Log.i("Detection", faceDetection.toArray().size.toString())

                    for(face in faceDetection.toArray()) {
                        Imgproc.rectangle(rgbMatFrame, Point(face.x.toDouble(),face.y.toDouble()),
                            Point(face.x.toDouble() + face.width.toDouble(),face.y.toDouble() + face.height.toDouble()), Scalar
                        (255.0,0.0,0.0),4
                        )
                    }

                    // Core.flip(rgbMatFrame,rgbMatFrame,1)

                    // convert into bitmap and send it to the listener
                    val bmp = Bitmap.createBitmap(rgbMatFrame.cols(), rgbMatFrame.rows(), Bitmap.Config.ARGB_8888)
                    val message = "Frame"

                    Utils.matToBitmap(rgbMatFrame, bmp)

                    listener(message, bmp)
                }
            }

            image.close()
        }
    }

}