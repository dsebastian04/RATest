package com.tshare.testra

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity(), Scene.OnUpdateListener {
    override fun onUpdate(p0: FrameTime?) {
        val frame: Frame? = arView?.arFrame
        val updateAugmentedImage: Collection<AugmentedImage>? = frame?.getUpdatedTrackables(AugmentedImage::class.java)
        updateAugmentedImage?.forEach { augmentedImage: AugmentedImage ->
            run {
                if (augmentedImage.trackingState == TrackingState.TRACKING) {
                    if (augmentedImage.name == "lion") {
                        val node = MyARNode(this, R.raw.lion)
                        node.image = augmentedImage
                        arView?.scene?.addChild(node)
                    }
                }
            }
        }

    }

    var arView: ArSceneView? = null
    var session: Session? = null
    var shouldConfigureSession: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arView = findViewById(R.id.arView)

        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {

                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setupSession()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    Toast.makeText(this@MainActivity, "Permission required to display camera", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }).check()

        initSceneView()
    }

    private fun initSceneView() {
        arView?.scene?.addOnUpdateListener(this)
    }

    private fun setupSession() {
        if (session == null) {
            try {
                session = Session(this)
            } catch (e: UnavailableArcoreNotInstalledException) {
                e.printStackTrace()
            } catch (e: UnavailableApkTooOldException) {
                e.printStackTrace()
            } catch (e: UnavailableSdkTooOldException) {
                e.printStackTrace()
            } catch (e: UnavailableDeviceNotCompatibleException) {
                e.printStackTrace()
            }
        }
        shouldConfigureSession = true
        if (shouldConfigureSession) {
            configSession()
            shouldConfigureSession = false
            arView?.setupSession(session)
        }
        try {
            session?.resume()
            arView?.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
            session = null
            return
        }
    }

    private fun configSession() {
        var config = Config(session)
        if (!buildDatabase(config)) {
            Toast.makeText(this, "Error reading database", Toast.LENGTH_SHORT).show()
        }
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session?.configure(config)
    }

    private fun buildDatabase(config: Config): Boolean {
        var bitmap: Bitmap? = loadImage()
        if (bitmap == null)
            return false

        var augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("lion", bitmap)
        config.augmentedImageDatabase = augmentedImageDatabase
        return true

    }

    private fun loadImage(): Bitmap? {
        try {
            var iS: InputStream = assets.open("lion_qr.jpeg")
            return BitmapFactory.decodeStream(iS)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {

                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setupSession()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    Toast.makeText(this@MainActivity, "Permission required to display camera", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }).check()
    }

    override fun onPause() {
        super.onPause()
        arView?.pause()
        session?.pause()
    }
}
