package io.nethermind.pushnotifications

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

data class ScanResult(val barcodes: List<Barcode>, val imageWidth: Int, val imageHeight: Int)

class BarCodeAnalyzer(val onBarCodeDetected: (result: ScanResult) -> Unit):ImageAnalysis.Analyzer {

    private var isBusy = AtomicBoolean(false)

    @androidx.camera.core.ExperimentalGetImage
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        if (isBusy.compareAndSet(false, true)) {
            val visionImage = InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
            BarcodeScanning.getClient().process(visionImage)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let { result ->
                            onBarCodeDetected(ScanResult(result, image.width, image.height))
                        }
                    } else {
                        Log.w("BarcodeAnalyzer", "failed to scan image: ${task.exception?.message}")
                    }
                    image.close()
                    isBusy.set(false)
                }
        } else {
            image.close()
        }
    }

    companion object{
        private const val TAG = "BarcodeAnalyzer"
    }
}