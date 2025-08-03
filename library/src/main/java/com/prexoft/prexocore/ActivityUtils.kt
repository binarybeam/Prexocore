package com.prexoft.prexocore

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import androidx.core.graphics.createBitmap
import kotlin.reflect.KClass

@RequiresApi(Build.VERSION_CODES.M)
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

fun Activity.captureScreen(callback: (Bitmap?) -> Unit) {
    try {
        val view = window.decorView
        val bitmap = createBitmap(view.width, view.height)
        val location = IntArray(2)
        view.getLocationInWindow(location)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PixelCopy.request(window,
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

fun <T : View> Activity.getViews(type: KClass<T>, parent: View = this.findViewById(android.R.id.content)): List<T> {
    return parent.getViews(type)
}

fun Activity.getViews(parent: View = this.findViewById(android.R.id.content)): List<View> {
    return parent.getViews()
}