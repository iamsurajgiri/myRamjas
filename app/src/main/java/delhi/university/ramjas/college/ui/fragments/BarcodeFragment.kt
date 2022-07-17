package delhi.university.ramjas.college.ui.fragments

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import delhi.university.ramjas.college.R
import delhi.university.ramjas.college.databinding.FragmentBarcodeBinding
import delhi.university.ramjas.college.parcels.SetupParcel
import delhi.university.ramjas.college.utils.BarcodeBoxView
import delhi.university.ramjas.college.utils.QrCodeAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias CameraQRAnalyzerListener = (text: String?) -> Unit
class BarcodeFragment : Fragment() {
    private lateinit var mContext: Context

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeBoxView: BarcodeBoxView
    private var _binding: FragmentBarcodeBinding? = null
    private lateinit var setupParcel: SetupParcel


    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val temp: SetupParcel? = arguments?.getParcelable("setupParcel")
        if (temp != null) setupParcel = temp
        else Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBarcodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestAllPermissions()

        barcodeBoxView = BarcodeBoxView(mContext)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun requestAllPermissions() {
        multiPermissionCallback.launch(
            Manifest.permission.CAMERA
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Image analyzer
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor, QrCodeAnalyzer { result ->
                            if (result != null) {
                                if (result.isEmpty()) {
                                    Toast.makeText(
                                        mContext,
                                        "No Text Detected",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                } else {
                                    var statusReport = "failed"
                                    if (result.uppercase().contains(setupParcel.collegeID!!)) statusReport = "success"
                                    setupParcel.verification = statusReport
                                    val bundle = bundleOf(
                                        "setupParcel" to setupParcel
                                    )
                                    findNavController().navigate(
                                        R.id.action_barcodeFragment_to_setVerificationStatusFragment,
                                        bundle)
                                }
                            }
                        }
                    )
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                ).cameraControl.setLinearZoom(0.3f)

            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(mContext))
    }

    private val multiPermissionCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isEnabled ->
            if (isEnabled) {
                startCamera()
            } else {
                Toast.makeText(mContext, "Please provide the permission", Toast.LENGTH_SHORT)
                    .show()
                requestAllPermissions()
            }

        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}