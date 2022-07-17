package delhi.university.ramjas.college.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs
import delhi.university.ramjas.college.R
import kotlin.math.max
import com.snatik.storage.Storage
import delhi.university.ramjas.college.constants.Constants.Companion.RATIO_16_9_VALUE
import delhi.university.ramjas.college.constants.Constants.Companion.RATIO_4_3_VALUE
import delhi.university.ramjas.college.customviews.ProgressDialog
import delhi.university.ramjas.college.databinding.FragmentCameraBinding
import delhi.university.ramjas.college.parcels.SetupParcel
import delhi.university.ramjas.college.utils.TextAnalyser
import delhi.university.ramjas.college.utils.snack

import kotlin.math.min


typealias CameraTextAnalyzerListener = (text: String) -> Unit
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private lateinit var mContext: Context
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private lateinit var setupParcel: SetupParcel


    private val executor by lazy {
        Executors.newSingleThreadExecutor()
    }
    private lateinit var progressDialog: ProgressDialog
    lateinit var binding: FragmentCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val temp: SetupParcel? = arguments?.getParcelable("setupParcel")
        if (temp != null) setupParcel = temp
        else Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show()

        Log.i("CameraFragment", "onCreate: $setupParcel")

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentCameraBinding.bind(view)


        progressDialog = ProgressDialog(mContext, false)

        binding.viewFinder.post {
            requestAllPermissions()
        }
        binding.ivImageCapture.setOnClickListener {
            progressDialog.show()
            takePicture()
        }

    }


    @Suppress("SameParameterValue")
    private fun createFile(baseFolder: File, format: String, extension: String) =
        File(
            baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension
        )

    private fun takePicture() {

        val file = createFile(
            getOutputDirectory(
                mContext
            ),
            "yyyy-MM-dd-HH-mm-ss-SSS",
            ".png"
        )
        val outputFileOptions =
            ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(
            outputFileOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {


                    // sending the captured image for analysis
                    GlobalScope.launch(Dispatchers.IO) {
                        TextAnalyser({ result:String ->
                            if (result.isEmpty()) {

                                progressDialog.dismiss()
                                Toast.makeText(
                                    mContext,
                                    "No Text Detected",
                                    Toast.LENGTH_SHORT
                                ).show()


                            } else {
                                var statusReport = "failed"
                                if(result.uppercase().contains(setupParcel.collegeID!!)) statusReport = "success"
                                progressDialog.dismiss()
                                setupParcel.verification = statusReport
                                val bundle = bundleOf(
                                    "setupParcel" to setupParcel
                                )
                                findNavController().navigate(
                                    R.id.action_cameraFragment_to_setVerificationStatusFragment,
                                    bundle
                                    )
                            }

                        }, mContext, Uri.fromFile(file)).analyseImage()

                    }
                }

                override fun onError(exception: ImageCaptureException) {

                    progressDialog.dismiss()
                    Log.e("error", exception.localizedMessage!!)
                }
            })
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun startCamera() {


        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        val rotation = binding.viewFinder.display.rotation


        // Bind the CameraProvider to the LifeCycleOwner
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
        cameraProviderFuture.addListener({

            // CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation
                .setTargetRotation(rotation)

                .build()

            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            // ImageCapture
            imageCapture = initializeImageCapture(screenAspectRatio, rotation)

            // ImageAnalysis


            cameraProvider.unbindAll()

            try {
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo
                cameraControl.setLinearZoom(0.1f)


            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(mContext))


    }

    private fun getOutputDirectory(context: Context): File {

        val storage = Storage(context)
        val mediaDir = storage.internalCacheDirectory?.let {
            File(it, "Intelligible OCR").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }


    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


    private fun initializeImageCapture(
        screenAspectRatio: Int,
        rotation: Int
    ): ImageCapture {
        return ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
    }

    private val multiPermissionCallback =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if ( map.entries.size <3){
                Toast.makeText(mContext, "Please Accept all the permissions", Toast.LENGTH_SHORT).show()
            }else{
                startCamera()
            }

        }


    private fun requestAllPermissions(){
        multiPermissionCallback.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        )
    }


    override fun onResume() {
        super.onResume()
        binding.root.snack(getString(R.string.pointing_message),mContext)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}