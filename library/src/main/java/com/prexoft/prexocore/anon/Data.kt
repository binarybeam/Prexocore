package com.prexoft.prexocore.anon

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class Media(
    val content: Bitmap,
    val time: Long
)

data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val startTime: Long,
    val endTime: Long
)

data class Contact(
    val id: String,
    val name: String,
    val phone: String
)

data class CallLog(
    val id: String,
    val number: String,
    val type: String,
    val time: Long
)

data class Sms(
    val id: String,
    val sender: String,
    val body: String,
    val time: Long
)

data class App(
    val packageName: String,
    val appName: String,
    val icon: Drawable
)