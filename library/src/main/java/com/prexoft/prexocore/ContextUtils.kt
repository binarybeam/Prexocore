package com.prexoft.prexocore

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import android.icu.util.Calendar
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.CalendarContract
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Telephony
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.text.InputType
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.FontRes
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import kotlin.reflect.KClass
import kotlin.toString
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import com.prexoft.prexocore.anon.App
import com.prexoft.prexocore.anon.CalendarEvent
import com.prexoft.prexocore.anon.Contact
import com.prexoft.prexocore.anon.Media
import com.prexoft.prexocore.anon.SimSlot
import com.prexoft.prexocore.anon.Sms
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
        share(uri, "image/*", subject)
    }
}

fun Context.share(uri: Uri, contentType: String = "*/*", subject: String = "") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = contentType
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(intent, "Share via"))
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

@RequiresPermission(Manifest.permission.VIBRATE)
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

fun Context.alert(title: String?, description: Any?, action: String = "Close", required: Boolean = true, @FontRes fontFamily: Int, acknowledged: (Boolean) -> Unit = {}) {
    vibrate(legacyFallback = false, minimal = true)
    val dialog = Dialog(this)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.alert)

    val button = dialog.findViewById<CardView>(R.id.okay)
    val actionView = dialog.findViewById<TextView>(R.id.textView)
    val main = dialog.findViewById<CardView>(R.id.main)
    val titleView = dialog.findViewById<TextView>(R.id.title)
    val descView = dialog.findViewById<TextView>(R.id.desc)

    dialog.setCancelable(required)
    try {
        val typeFace = ResourcesCompat.getFont(this, fontFamily)

        titleView.typeface = typeFace
        descView.typeface = typeFace
        actionView.typeface = typeFace
    }
    catch (_: Exception) { }

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
    }

    titleView.text = title.toString()
    descView.text = description.toString()
    actionView.text = action
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()
}

fun Context.after(seconds: Double, repeat: Int = 1, feedback: Boolean = false, action: () -> Unit) {
    if (repeat > 0) {
        Handler(mainLooper).postDelayed({
            action()
            if (feedback) vibrate(legacyFallback = false, minimal = true)
            after(seconds, repeat-1, feedback, action)
        }, (seconds*1000).toLong())
    }
}

fun Context.after(seconds: Int, repeat: Int = 1, feedback: Boolean = false, action: () -> Unit) {
    after(seconds.toDouble(), repeat, feedback, action)
}

fun Context.input(title: String? = "Enter an input", description: String? = "", hint: String? = "Type here...", required: Boolean = false, inputType: Int = InputType.TYPE_CLASS_TEXT, @FontRes fontFamily: Int? = null, onResult: (String) -> Unit) {
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
    try {
        if (fontFamily != null) {
            val typeFace = ResourcesCompat.getFont(this, fontFamily)

            inputView.typeface = typeFace
            titleView.typeface = typeFace
            descView.typeface = typeFace
            actionView.typeface = typeFace
        }

        inputView.inputType = inputType

    }
    catch (_: Exception) { }

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
        if (required && inputView.text.toString().isBlank()) safeToast("This field is required.", gapInSeconds = 5)
        else {
            onResult(inputView.text.toString())
            dialog.dismiss()
        }
    }

    dialog.setOnCancelListener {
        onResult("")
    }

    titleView.text = title.toString()
    inputView.hint = hint.toString()
    descView.text = description.toString().ifBlank { if (required) "Required" else "Optional" }
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()
}

@RequiresApi(Build.VERSION_CODES.M)
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
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
@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun Context.postNotification(
    title: String,
    content: String,
    icon: Int,
    notificationId: Int = 1,
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
        .setSmallIcon(icon)
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

fun Context.writeInternalFile(fileName: String, text: String) {
    File(filesDir, fileName).writeText(text)
}

fun Context.speak(
    text: CharSequence,
    rate: Float = 1.0f,
    pitch: Float = 1.0f,
    locale: Locale = Locale.getDefault(),
    onDone: (() -> Unit)? = null
) {
    Tts.speak(this, text.toString(), rate, pitch, locale, onDone)
}

fun Context.shutdownSpeaker() {
    Tts.shutdown()
}

@RequiresPermission(Manifest.permission.RECORD_AUDIO)
fun Context.listenSpeech(keepListening: Boolean = false, onResult: (String) -> Unit) {
    if (!SpeechRecognizer.isRecognitionAvailable(this)) {
        onResult("")
        return
    }

    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }

    val listener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull()?:""

            onResult(text)
            if (keepListening) after(1) { speechRecognizer.startListening(intent) }
            else speechRecognizer.destroy()
        }

        override fun onError(error: Int) {
            onResult("")
            if (keepListening) after(1) { speechRecognizer.startListening(intent) }
            else speechRecognizer.destroy()
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    speechRecognizer.setRecognitionListener(listener)
    speechRecognizer.startListening(intent)
}

fun Context.playSound(source: Any): MediaPlayer? {
    return try {
        val mediaPlayer = MediaPlayer()
        when (source) {
            is Int -> {
                val uri = "android.resource://$packageName/$source".toUri()
                mediaPlayer.setDataSource(this, uri)
            }
            is String -> {
                if (source.startsWith("http://") || source.startsWith("https://")) mediaPlayer.setDataSource(source)
                else mediaPlayer.setDataSource(source)
            }
            is File -> mediaPlayer.setDataSource(source.absolutePath)
            is Uri -> mediaPlayer.setDataSource(this, source)
            else -> null
        }

        mediaPlayer.setOnPreparedListener { it.start() }
        mediaPlayer.prepareAsync()
        mediaPlayer
    }
    catch (_: Exception) {
        null
    }
}

@RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
fun Context.getPhotosForOlderDevice(maxCount: Int = Int.MAX_VALUE): List<Media> {
    return postPermissionHandledPhotos(maxCount, 110011)
}

@RequiresApi(Build.VERSION_CODES.R)
@RequiresPermission(Manifest.permission.READ_MEDIA_IMAGES)
fun Context.getPhotos(maxCount: Int = Int.MAX_VALUE): List<Media> {
    return postPermissionHandledPhotos(maxCount, 110011)
}

fun Context.postPermissionHandledPhotos(maxCount: Int, accessCode: Int): List<Media> {
    if (accessCode != 110011) return emptyList()
    val images = mutableListOf<Media>()
    val contentResolver = applicationContext.contentResolver
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.DISPLAY_NAME
    )
    val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
    val cursor = contentResolver.query(uri, projection, null, null, sortOrder)

    cursor?.use {
        var count = 0
        while (it.moveToNext() && count < maxCount) {
            val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            val dateTaken = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN))
            val imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())

            try {
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    images.add(Media(bitmap, dateTaken))
                    count++
                }
            }
            catch (e: Exception) {
                e.message.log()
            }
        }
    }
    return images
}

@RequiresApi(Build.VERSION_CODES.M)
@RequiresPermission(allOf = [Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE])
fun Context.sendSms(phone: String, message: String, sim: SimSlot = SimSlot.DEFAULT): Boolean {
    val smsManager = getSmsManagerForSim(sim) ?: return false
    return try {
        smsManager.sendTextMessage(phone, null, message, null, null)
        true
    }
    catch (_: Exception) {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.M)
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun Context.getSmsManagerForSim(sim: SimSlot): SmsManager? {
    return try {
        when (sim) {
            SimSlot.SIM_1, SimSlot.SIM_2 -> {
                val subId = getSimSubscriptionId(sim.ordinal) ?: return null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    getSystemService(SmsManager::class.java)?.createForSubscriptionId(subId)
                }
                else {
                    @Suppress("DEPRECATION")
                    SmsManager.getSmsManagerForSubscriptionId(subId)
                }
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    getSystemService(SmsManager::class.java)
                }
                else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            }
        }
    }
    catch (_: Exception) {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.M)
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun Context.getSimSubscriptionId(slotIndex: Int): Int? {
    val subscriptionManager = getSystemService(SubscriptionManager::class.java)
    return subscriptionManager.activeSubscriptionInfoList
        ?.firstOrNull { it.simSlotIndex == slotIndex }
        ?.subscriptionId
}

@RequiresPermission(Manifest.permission.READ_SMS)
fun Context.getSms(maxCount: Int = Int.MAX_VALUE): List<Sms> {
    val uri = Telephony.Sms.Inbox.CONTENT_URI
    val projection = arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)
    val sortOrder = "${Telephony.Sms.DATE} DESC"
    val cursor = contentResolver.query(uri, projection, null, null, sortOrder)
    val messages = mutableListOf<Sms>()

    cursor?.use {
        var count = 0
        while (it.moveToNext() && count < maxCount) {
            val id = it.getString(it.getColumnIndexOrThrow(Telephony.Sms._ID))
            val address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
            val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
            val timeInMillis = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))

            messages.add(Sms(
                id = id,
                sender = address,
                body = body,
                time = timeInMillis
            ))
            count++
        }
    }
    return messages
}

@RequiresApi(Build.VERSION_CODES.R)
fun Context.fileStructure(directory: File = Environment.getExternalStorageDirectory(), onResult: (String) -> Unit) {
    structure = ""
    if (!Environment.isExternalStorageManager()) {
        goTo(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        onResult("")
    }
    else {
        listFileStructure(directory)
        after(1) { onResult(structure) }
    }
}

@RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
fun Context.fileStructureForOlderDevice(directory: File = Environment.getExternalStorageDirectory(), onResult: (String) -> Unit) {
    structure = ""
    listFileStructure(directory)
    after(1) { onResult(structure) }
}

private var structure = ""
fun listFileStructure(directory: File, indent: String = "") {
    val files = directory.listFiles()

    files?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))?.forEach { file ->
        structure += "$indent${file.name}\n"
        if (file.isDirectory) listFileStructure(file, "$indent    ")
    }
}

fun Context.getCallLogs(maxCount: Int = Int.MAX_VALUE): List<com.prexoft.prexocore.anon.CallLog> {
    val uri = CallLog.Calls.CONTENT_URI
    val projection = arrayOf(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE)
    val cursor = contentResolver.query(uri, projection, null, null, null)
    val calls = mutableListOf<com.prexoft.prexocore.anon.CallLog>()

    cursor?.use {
        var count = 0
        while (it.moveToNext() && count < maxCount) {
            val id = it.getString(it.getColumnIndexOrThrow(CallLog.Calls._ID))
            val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            val type = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
            val time = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))

            calls.add(com.prexoft.prexocore.anon.CallLog(
                id = id,
                number = number,
                type = type,
                time = time
            ))
            count++
        }
    }
    return calls
}

fun Context.getContacts(): List<Contact> {
    val contactsMap = mutableListOf<Contact>()
    val contactCursor: Cursor? = contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
        null, null, null
    )

    contactCursor?.use {
        val idCol = it.getColumnIndex(ContactsContract.Contacts._ID)
        val nameCol = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

        while (it.moveToNext()) {
            val contactId = it.getString(idCol)
            val contactName = it.getString(nameCol)

            val phoneCursor: Cursor? = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                arrayOf(contactId),
                null
            )

            phoneCursor?.use { pCursor ->
                val numberCol = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (pCursor.moveToNext()) {
                    pCursor.getString(numberCol)?.let { phoneNumber ->
                        contactsMap.add(Contact(
                            id = contactId,
                            name = contactName,
                            phone = phoneNumber
                        ))
                    }
                }
            }
        }
    }
    return contactsMap
}

@RequiresApi(Build.VERSION_CODES.N)
fun Context.getCalenderEvents(numberOfDays: Int = 365): List<CalendarEvent> {
    val contentResolver: ContentResolver = applicationContext.contentResolver
    val uri = CalendarContract.Events.CONTENT_URI
    val projection = arrayOf(CalendarContract.Events._ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.EVENT_LOCATION)

    val calendar = Calendar.getInstance()
    val startTimeMillis = calendar.timeInMillis

    calendar.add(Calendar.DAY_OF_YEAR, numberOfDays)

    val endTimeMillis = calendar.timeInMillis
    val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTEND} <= ?"
    val selectionArgs = arrayOf(startTimeMillis.toString(), endTimeMillis.toString())
    val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
    val events = mutableListOf<CalendarEvent>()

    cursor?.use {
        while (it.moveToNext()) {
            val id = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
            val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
            val description = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
            val startDate = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
            val endDate = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
            val location = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))

            events.add(CalendarEvent(
                id = id,
                title = title,
                description = description,
                location = location,
                startTime = startDate,
                endTime = endDate
            ))
        }
    }
    return events
}

@RequiresPermission(Manifest.permission.QUERY_ALL_PACKAGES)
fun Context.getApps(avoidSystemApps: Boolean = false): List<App> {
    val apps = mutableListOf<App>()

    if (avoidSystemApps) {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resolveInfos) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            apps.add(App(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    icon = packageManager.getApplicationIcon(appInfo)
                )
            )
        }
    }
    else {
        for (appInfo in packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
            apps.add(App(
                packageName = appInfo.packageName,
                appName = packageManager.getApplicationLabel(appInfo).toString(),
                icon = packageManager.getApplicationIcon(appInfo)
            ))
        }
    }
    return apps
}

@RequiresApi(Build.VERSION_CODES.M)
fun Context.torchMode(enable: Boolean) {
    val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList[0]
    cameraManager.setTorchMode(cameraId, enable)
}

fun Context.getIconOfInstalledPackage(packageName: String): Drawable {
    return packageManager.getApplicationIcon(packageName)
}