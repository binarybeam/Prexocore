package com.prexoft.prexocore

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

fun Int.dial(context: Context) {
    context.goTo(this)
}

fun Long.dial(context: Context) {
    context.goTo(this)
}

fun String.preview(context: Context) {
    context.goTo(this)
}

fun Uri.open(context: Context) {
    context.goTo(this)
}

fun Intent.start(context: Context) {
    context.goTo(this)
}

fun String.share(context: Context, subject: String = "") {
    context.share(this, subject)
}

@RequiresApi(Build.VERSION_CODES.Q)
fun Bitmap.share(context: Context, subject: String = "") {
    context.share(this, subject)
}

fun String.copy(context: Context) {
    context.copy(this)
}

fun File.read(): String {
    return try { this.readText() }
    catch (_: Exception) { "" }
}

fun Int.fromDpToPx(context: Context): Int = context.dpToPx(this)
fun Int.fromPxToDp(context: Context): Double = context.pxToDp(this)

fun Any?.log(tag: String = "DEBUG") {
    Log.d(tag, this.toString())
}

fun Long.formatAsTime(pattern: String = "hh:mm a", locale: Locale = Locale.US): String {
    return SimpleDateFormat(pattern, locale).format(this)
}

fun Long.formatAsDate(pattern: String = "dd.MM.yyyy", locale: Locale = Locale.getDefault()): String {
    return formatAsTime(pattern, locale)
}

fun Long.formatAsDateAndTime(pattern: String = "hh:mm a, dd MMM yyyy", locale: Locale = Locale.getDefault()): String {
    return formatAsTime(pattern, locale)
}

fun String.append(value: String): String {
    return this + value
}