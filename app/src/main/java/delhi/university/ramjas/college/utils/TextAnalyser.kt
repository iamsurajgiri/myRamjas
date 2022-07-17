package delhi.university.ramjas.college.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import delhi.university.ramjas.college.ui.fragments.CameraTextAnalyzerListener


class TextAnalyser(
    private val textListener: CameraTextAnalyzerListener,
    private var context: Context,
    private val fromFile: Uri,
) {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun analyseImage() {

        try {


            val image = InputImage.fromFilePath(context, fromFile)


            recognizer.process(image)
                .addOnSuccessListener { visionText ->

                    Log.i("OCR Text", visionText.text)

                    textListener(visionText.text)

                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    e.localizedMessage?.let { Log.e("text", it) }
                }


        } catch (e: Exception) {

            e.localizedMessage?.let { Log.e("text ", it) }

        }
    }


}

