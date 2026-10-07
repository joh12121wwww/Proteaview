package za.co.proteaview.anpr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var cameraPreview: PreviewView
    private lateinit var statusText: TextView
    private lateinit var plateText: TextView
    private lateinit var detectionInfo: TextView
    private lateinit var startButton: Button

    private var anprRunning = false

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                statusText.text = "● CAMERA PERMISSION REQUIRED"
                detectionInfo.text = "Allow camera access to use Proteaview"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraPreview = findViewById(R.id.cameraPreview)
        statusText = findViewById(R.id.statusText)
        plateText = findViewById(R.id.plateText)
        detectionInfo = findViewById(R.id.detectionInfo)
        startButton = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            toggleAnpr()
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {

        statusText.text = "● CAMERA STARTING | DEVICE 01"

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(cameraPreview.surfaceProvider)
                    }

                val cameraSelector =
                    CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview
                )

                statusText.text =
                    "● LIVE | ANPR DEVICE 01"

                detectionInfo.text =
                    "Camera online • Ready for ANPR"

            } catch (e: Exception) {

                statusText.text =
                    "● CAMERA ERROR"

                detectionInfo.text =
                    e.message ?: "Unable to start camera"
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleAnpr() {

        anprRunning = !anprRunning

        if (anprRunning) {

            startButton.text = "STOP ANPR"
            statusText.text = "● LIVE | ANPR ACTIVE"
            plateText.text = "SCANNING..."
            detectionInfo.text = "Searching for licence plates"

        } else {

            startButton.text = "START ANPR"
            statusText.text = "● LIVE | ANPR STANDBY"
            plateText.text = "NO PLATE DETECTED"
            detectionInfo.text = "ANPR stopped"
        }
    }
}
