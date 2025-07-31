package com.prexoft.prexocore

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import kotlin.reflect.KClass
import kotlin.toString
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import java.io.File
import java.util.Locale

fun Context.havePermission(permission: String): Boolean {
    return havePermission(listOf(permission))
}

fun Context.havePermission(permissions: List<String>): Boolean {
    permissions.forEach { it->
        if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) return false
    }
    return true
}

fun Context.dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
fun Context.dpToPx(dp: Double): Int = (dp * resources.displayMetrics.density).toInt()
fun Context.pxToDp(px: Int): Double = (px / resources.displayMetrics.density).toDouble()

fun Context.goTo(cls: Class<*>, extras: Bundle? = null, flags: Int? = null, options: Bundle? = null) {
    vibrate(legacyFallback = false, minimal = true)
    val intent = Intent(this, cls)
    extras?.let { intent.putExtras(it) }

    val resolvedFlags = when {
        flags != null -> {
            if (this !is Activity && flags and Intent.FLAG_ACTIVITY_NEW_TASK == 0) {
                flags or Intent.FLAG_ACTIVITY_NEW_TASK
            } else flags
        }
        this !is Activity -> Intent.FLAG_ACTIVITY_NEW_TASK
        else -> null
    }

    resolvedFlags?.let { intent.flags = it }
    startActivity(intent, options)
}

fun Context.goTo(kClass: KClass<*>, extras: Bundle? = null, flags: Int? = null, options: Bundle? = null) {
    goTo(kClass.java, extras, flags, options)
}

fun Context.goTo(uri: String) {
    vibrate(legacyFallback = false, minimal = true)
    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
    }
    catch (_: Exception) {
        if (uri.removePrefix("+").replace(" ", "").replace("-", "").isDigitsOnly()) {
            val newUri = "tel:$uri"
            startActivity(Intent(Intent.ACTION_DIAL, newUri.toUri()))
        }
        else if (uri.contains("@") && uri.split("@")[1].contains(".")) {
            val newUri = "mailto:$uri"
            startActivity(Intent(Intent.ACTION_SENDTO, newUri.toUri()))
        }
        else if (uri.contains(".") && !uri.startsWith("http://") && !uri.startsWith("https://")) {
            val newUri = "https://$uri"
            startActivity(Intent(Intent.ACTION_VIEW, newUri.toUri()))
        }
    }
}

fun Context.goTo(intent: Intent) {
    vibrate(legacyFallback = false, minimal = true)
    startActivity(intent)
}
fun Context.goTo(number: Int) {
    vibrate(legacyFallback = false, minimal = true)
    startActivity(Intent(Intent.ACTION_DIAL, "tel:$number".toUri()))
}

fun Context.goTo(number: Long) {
    vibrate(legacyFallback = false, minimal = true)
    startActivity(Intent(Intent.ACTION_DIAL, "tel:$number".toUri()))
}

fun Context.goTo(uri: Uri) {
    vibrate(legacyFallback = false, minimal = true)
    startActivity(Intent(Intent.ACTION_VIEW, uri))
}

fun Context.share(text: String, subject: String = "") {
    vibrate(legacyFallback = false, minimal = true)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "Share via"))
}

@RequiresApi(Build.VERSION_CODES.Q)
fun Context.share(bitmap: Bitmap, subject: String = "") {
    vibrate(legacyFallback = false, minimal = true)

    val filename = "shared_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val uri = contentResolver.insert(contentUri, values)

    if (uri != null) {
        contentResolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        contentResolver.update(uri, values, null, null)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Share via"))
    }
}


fun Context.copy(text: String) {
    vibrate(legacyFallback = false, minimal = true)
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}

private var lastToast = 0L
fun Context.safeToast(message: Any?, duration: Int = Toast.LENGTH_SHORT, gapInSeconds: Int = 3) {
    val now = System.currentTimeMillis()
    if (now - lastToast > gapInSeconds*1000) {
        lastToast = now
        vibrate(false, minimal = true)
        Toast.makeText(this, message.toString(), duration).show()
    }
}
fun Context.toast(message: Any?, duration: Int = Toast.LENGTH_SHORT) {
    vibrate(false, minimal = true)
    Toast.makeText(this, message.toString(), duration).show()
}

fun Context.vibrate(legacyFallback: Boolean = true, minimal: Boolean = false) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    }
    else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrator.vibrate(VibrationEffect.createPredefined(if (minimal) VibrationEffect.EFFECT_TICK else VibrationEffect.EFFECT_CLICK))
    }
    else if (legacyFallback) {
        @Suppress("DEPRECATION")
        vibrator.vibrate(if (minimal) 25 else 50)
    }
}

fun Context.alert(title: String?, description: Any?, action: String = "Close", cancelable: Boolean = true, acknowledged: (Boolean) -> Unit = {}) {
    vibrate(legacyFallback = false, minimal = true)
    val dialog = Dialog(this)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.alert)

    val button = dialog.findViewById<CardView>(R.id.okay)
    val actionView = dialog.findViewById<TextView>(R.id.textView)
    val main = dialog.findViewById<CardView>(R.id.main)
    val titleView = dialog.findViewById<TextView>(R.id.title)
    val descView = dialog.findViewById<TextView>(R.id.desc)

    dialog.setCancelable(cancelable)

    if (isDarkTheme()) {
        main.setCardBackgroundColor("#212121".toColorInt())
        button.setCardBackgroundColor("#ffffff".toColorInt())
        actionView.setTextColor("#000000".toColorInt())
        titleView.setTextColor("#ffffff".toColorInt())
    }
    else {
        main.setCardBackgroundColor("#ffffff".toColorInt())
        button.setCardBackgroundColor("#000000".toColorInt())
        actionView.setTextColor("#ffffff".toColorInt())
        titleView.setTextColor("#000000".toColorInt())
    }

    main.onClick { }
    button.onClick {
        acknowledged(true)
        dialog.dismiss()
    }

    dialog.setOnCancelListener {
        acknowledged(false)
        dialog.dismiss()
    }

    titleView.text = title.toString()
    descView.text = description.toString()
    actionView.text = action
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()
}

fun Context.after(seconds: Double, loop: Int = 1, feedback: Boolean = false, action: () -> Unit) {
    if (loop > 0) {
        Handler(mainLooper).postDelayed({
            action()
            if (feedback) vibrate(legacyFallback = false, minimal = true)
            after(seconds, loop-1, feedback, action)
        }, (seconds*1000).toLong())
    }
}

fun Context.after(seconds: Int, loop: Int = 1, feedback: Boolean = false, action: () -> Unit) {
    after(seconds.toDouble(), loop, feedback, action)
}

fun Context.input(title: String? = "Enter an input", description: String? = "", hint: String? = "Type here...", required: Boolean = false, onResult: (String) -> Unit) {
    vibrate(legacyFallback = false, minimal = true)
    val dialog = Dialog(this)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.input)

    val button = dialog.findViewById<CardView>(R.id.okay)
    val actionView = dialog.findViewById<TextView>(R.id.textView)
    val main = dialog.findViewById<CardView>(R.id.main)
    val titleView = dialog.findViewById<TextView>(R.id.title)
    val descView = dialog.findViewById<TextView>(R.id.desc)
    val inputView = dialog.findViewById<EditText>(R.id.editTextText)

    dialog.setCancelable(!required)

    if (isDarkTheme()) {
        main.setCardBackgroundColor("#212121".toColorInt())
        button.setCardBackgroundColor("#ffffff".toColorInt())
        actionView.setTextColor("#000000".toColorInt())
        titleView.setTextColor("#ffffff".toColorInt())
        inputView.setTextColor("#ffffff".toColorInt())
    }
    else {
        main.setCardBackgroundColor("#ffffff".toColorInt())
        button.setCardBackgroundColor("#000000".toColorInt())
        actionView.setTextColor("#ffffff".toColorInt())
        titleView.setTextColor("#000000".toColorInt())
        inputView.setTextColor("#000000".toColorInt())
    }

    main.onClick { }
    button.onClick {
        if (required && inputView.text.toString().isBlank()) toast("This field is required.")
        else {
            onResult(inputView.text.toString())
            dialog.dismiss()
        }
    }

    dialog.setOnCancelListener {
        onResult("")
        dialog.dismiss()
    }

    titleView.text = title.toString()
    inputView.hint = hint.toString()
    descView.text = description.toString().ifBlank { if (required) "Required" else "Optional" }
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()
}

@RequiresApi(Build.VERSION_CODES.M)
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun Context.isDarkTheme(): Boolean {
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> false
        else -> false
    }
}

@RequiresApi(Build.VERSION_CODES.N)
fun Context.postNotification(
    title: String,
    content: String,
    notificationId: Int = 1,
    smallIcon: Int,
    launchIntent: Intent? = null,
    channelId: String = "channel_1",
    channelName: String = "General Notifications",
    importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
    block: (NotificationCompat.Builder.() -> Unit)? = null
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, channelName, importance)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(this, channelId)
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(smallIcon)
        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        .setPriority(
            when (importance) {
                NotificationManager.IMPORTANCE_HIGH -> NotificationCompat.PRIORITY_HIGH
                NotificationManager.IMPORTANCE_LOW -> NotificationCompat.PRIORITY_LOW
                NotificationManager.IMPORTANCE_MIN -> NotificationCompat.PRIORITY_MIN
                else -> NotificationCompat.PRIORITY_DEFAULT
            }
        )
        .setAutoCancel(true)

    launchIntent?.let {
        val pendingIntent = PendingIntent.getActivity(this, 2, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)
    }

    block?.invoke(builder)
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    NotificationManagerCompat.from(this).notify(notificationId, builder.build())
}

fun Context.readInternalFile(fileName: String): String {
    return File(filesDir, fileName).read()
}

fun Context.speak(
    text: String,
    rate: Float = 1.0f,
    pitch: Float = 1.0f,
    locale: Locale = Locale.getDefault(),
    onDone: (() -> Unit)? = null
) {
    EasyTts.speak(this, text, rate, pitch, locale, onDone)
}

fun Context.shutdownSpeaker() {
    EasyTts.shutdown()
}