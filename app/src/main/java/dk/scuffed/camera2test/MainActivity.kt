package dk.scuffed.camera2test

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.InvalidPropertiesFormatException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        } else{
            setUp()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                setUp()
            }
            else{
                Toast.makeText(this, "The app require camera permission!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun onAllCamerasOpened(cameras: ArrayList<CameraDevice>) {
        for (camera in cameras) {
            Log.i("TEST", camera.id)
        }

    }

    @SuppressLint("MissingPermission")
    private fun setUp() {
        setContentView(R.layout.activity_main)
        val cameraManager = getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
        val cameraIdList = getCameraIds(cameraManager)/*.filter {cameraId ->
            cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }*/

        val cameras = ArrayList<CameraDevice>()

        var camerasAdded = 0

        cameraIdList.forEach { cameraId ->
            logCameraInfo(cameraManager, cameraId)
            if (0 != 0) {
                try {
                    cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                        override fun onOpened(cameraDevice: CameraDevice) {
                            val cameraId = cameraDevice.id
                            val cameraCharacteristics =
                                cameraManager.getCameraCharacteristics(cameraId)
                            val facing =
                                cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                            val facingString = when (facing) {
                                CameraCharacteristics.LENS_FACING_FRONT -> "front"
                                CameraCharacteristics.LENS_FACING_BACK -> "back"
                                CameraCharacteristics.LENS_FACING_EXTERNAL -> "external"
                                else -> "UNKNOWN"
                            }
                            Log.i("TEST", "Id: $cameraId Facing: $facingString")
                            cameras.add(cameraDevice)
                            camerasAdded++

                            if (camerasAdded == cameraIdList.size) {
                                onAllCamerasOpened(cameras)
                            }
                        }

                        override fun onDisconnected(cameraDevice: CameraDevice) {
                            cameras.remove(cameraDevice)
                        }

                        override fun onError(cameraDevice: CameraDevice, error: Int) {
                            val cameraId = cameraDevice.id
                            val stringError = cameraErrorToString(error)
                            throw Exception("Camera error on $cameraId: $stringError")
                        }

                    }, null)
                }
                catch (e: Exception) {
                    Log.e("TEST", e.toString())
                    camerasAdded++
                }
            }
        }

        //logCameraInfo(cameraManager, "2")
        //logCameraInfo(cameraManager, "3")
    }

    private fun getCameraIds(cameraManager: CameraManager): List<String> {
        val cameraIds = ArrayList<String>()
        var currentCameraId = 0

        while (true) {
            try {
                cameraManager.getCameraCharacteristics(currentCameraId.toString())
                cameraIds.add(currentCameraId.toString())
                currentCameraId++
            } catch (e: Exception)
            {
                Log.e("TEST", e.toString())
                break
            }
        }

        return cameraIds
    }

    private fun cameraErrorToString(error: Int): String {
        return when (error) {
            CameraDevice.StateCallback.ERROR_CAMERA_DEVICE -> "ERROR_CAMERA_DEVICE"
            CameraDevice.StateCallback.ERROR_CAMERA_DISABLED -> "ERROR_CAMERA_DISABLED"
            CameraDevice.StateCallback.ERROR_CAMERA_IN_USE -> "ERROR_CAMERA_IN_USE"
            CameraDevice.StateCallback.ERROR_CAMERA_SERVICE -> "ERROR_CAMERA_SERVICE"
            CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE -> "ERROR_MAX_CAMERAS_IN_USE"
            else -> "UNKNOWN ERROR: $error"
        }
    }

    private fun lensFacingToString(lensFacing: Int?): String {
        return when (lensFacing) {
            CameraCharacteristics.LENS_FACING_FRONT -> "front"
            CameraCharacteristics.LENS_FACING_BACK -> "back"
            CameraCharacteristics.LENS_FACING_EXTERNAL -> "external"
            else -> "unknown"
        }
    }

    private fun logCameraInfo(cameraManager: CameraManager, cameraId: String) {
        val cameraInfo = cameraManager.getCameraCharacteristics(cameraId)
        val lensFacing = cameraInfo.get(CameraCharacteristics.LENS_FACING)
        val lensFacingStr = lensFacingToString(lensFacing)
        val capabilities = cameraInfo.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        val hasMultiCamera = capabilities!!.contains(
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA)
        val hasDepthOutput = capabilities!!.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT)
        Log.i("TEST", "Camera ID: $cameraId facing: $lensFacingStr multiCam: $hasMultiCamera depth: $hasDepthOutput")
    }
}