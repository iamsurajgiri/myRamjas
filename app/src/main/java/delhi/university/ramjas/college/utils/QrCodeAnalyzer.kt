package delhi.university.ramjas.college.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import delhi.university.ramjas.college.ui.fragments.CameraQRAnalyzerListener

class QrCodeAnalyzer(
    private val qrListener: CameraQRAnalyzerListener,
) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val img = image.image
        if (img != null) {
            val inputImage = InputImage.fromMediaImage(img, image.imageInfo.rotationDegrees)

            // Process image searching for barcodes
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_CODE_128)
                .build()

            val scanner = BarcodeScanning.getClient(options)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if(barcodes.isNotEmpty()){
                        val barcode = barcodes[0].rawValue
                        qrListener(barcode)
                        Log.i("BARCODE", "analyze: $barcode")
                    }
                }
                .addOnFailureListener { }
        }

        image.close()
    }
}