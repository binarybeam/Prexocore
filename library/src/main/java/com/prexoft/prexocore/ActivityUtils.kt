package com.prexoft.prexocore

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.createBitmap

fun Activity.getPermission(permissions: List<String>, requestCode: Int = 100) {
    this.requestPermissions(permissions.toTypedArray(), requestCode)
}

fun Activity.isKeyboardOpen(): Boolean {
    val insets = ViewCompat.getRootWindowInsets(window.decorView)
    val keyboard = insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom
    return (keyboard ?: 0) > 0
}

fun Activity.onKeyboardChange(callback: (isOpen: Boolean, keyboardHeight: Int) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView.rootView) { _, insets ->
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        callback(imeInsets > 0, imeInsets)
        insets
    }
}

fun Activity.onKeyboardChange(callback: (isOpen: Boolean) -> Unit) {
    onKeyboardChange { isOpen, _ ->
        callback(isOpen)
    }
}

fun Activity.snack(message: Any?, action: String? = "", duration: Int = Snackbar.LENGTH_SHORT, onClick: (Boolean) -> Unit = {}) {
    Snackbar.make(window.decorView.rootView, message.toString(), duration).setAction(action, {
        vibrate(false, minimal = true)
        onClick(true)
    }).show()
}

fun Activity.view(@IdRes id: Int): View = findViewById(id)

fun ComponentActivity.getPermission(
    permission: String,
    callback: (Boolean) -> Unit = { }
) {
    val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        callback(it)
    }
    launcher.launch(permission)
}

fun LifecycleOwner.observeNetworkStatus(
    context: Context,
    onStatusChanged: (Boolean) -> Unit
) {
    val liveData = MutableLiveData<Boolean>()
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            liveData.postValue(true)
        }

        override fun onLost(network: Network) {
            liveData.postValue(false)
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            liveData.postValue(hasInternet)
        }
    }

    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    liveData.observe(this, Observer(onStatusChanged))
    connectivityManager.registerNetworkCallback(networkRequest, callback)

    lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    })
}

fun Activity.captureScreen(callback: (Bitmap?) -> Unit) {
    try {
        val view = window.decorView
        val bitmap = createBitmap(view.width, view.height)
        val location = IntArray(2)
        view.getLocationInWindow(location)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PixelCopy.request(this.window,
                Rect(location[0], location[1], location[0] + view.width, location[1] + view.height),
                bitmap, { result ->
                    if (result == PixelCopy.SUCCESS) callback(bitmap)
                    else callback(null)
                }, Handler(Looper.getMainLooper())
            )
        }
        else {
            val view = window.decorView.rootView
            view.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(view.drawingCache)

            view.isDrawingCacheEnabled = false
            callback(bitmap)
        }
    }
    catch (e: IllegalArgumentException) {
        e.printStackTrace()
        callback(null)
    }
}