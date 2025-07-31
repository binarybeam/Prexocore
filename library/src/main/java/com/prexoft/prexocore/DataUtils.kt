package com.prexoft.prexocore

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.net.Uri
import android.net.http.HttpException
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.text.toSpanned
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

fun Int.dial(context: Context) {
    context.goTo(this)
}

fun String.preview(context: Context) {
    context.goTo(this)
}

fun String.parseHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    }
    else Html.fromHtml(this)
}

fun CharSequence.unEmojify(): String {
    val emojiRegex = Regex(
        "[\uD83C-\uDBFF\uDC00-\uDFFF\u2600-\u27BF" +
                "\uFE00-\uFE0F" +
                "\u200D" +
                "\u23E9-\u23FA" +
                "]"
    )
    return emojiRegex.replace(this, "")
}

fun String.parseMarkdown(inHtmlFormat: Boolean = false): Spanned {
    val lines = this.lines()
    val html = StringBuilder()
    var inList = false
    var inOrderedList = false
    var inCodeBlock = false
    var inBlockquote = false

    for (line in lines) {
        val trimmed = line.trim()

        when {
            trimmed.startsWith("```") -> {
                if (inList) {
                    html.append("</ul>\n")
                    inList = false
                }
                if (inOrderedList) {
                    html.append("</ol>\n")
                    inOrderedList = false
                }
                if (inBlockquote) {
                    html.append("</blockquote>\n")
                    inBlockquote = false
                }
                if (inCodeBlock) {
                    html.append("</pre></code>\n")
                    inCodeBlock = false
                } else {
                    html.append("<pre><code>")
                    inCodeBlock = true
                }
                continue
            }

            inCodeBlock -> {
                html.append("${line}\n")
                continue
            }
        }

        when {
            trimmed.startsWith("###### ") -> html.append("<h6>${parseInlineMarkdown(trimmed.drop(7))}</h6>\n")
            trimmed.startsWith("##### ") -> html.append("<h5>${parseInlineMarkdown(trimmed.drop(6))}</h5>\n")
            trimmed.startsWith("#### ") -> html.append("<h4>${parseInlineMarkdown(trimmed.drop(5))}</h4>\n")
            trimmed.startsWith("### ") -> html.append("<h3>${parseInlineMarkdown(trimmed.drop(4))}</h3>\n")
            trimmed.startsWith("## ") -> html.append("<h2>${parseInlineMarkdown(trimmed.drop(3))}</h2>\n")
            trimmed.startsWith("# ") -> html.append("<h1>${parseInlineMarkdown(trimmed.drop(2))}</h1>\n")

            trimmed.startsWith("> ") -> {
                if (!inBlockquote) {
                    html.append("<blockquote>\n")
                    inBlockquote = true
                }
                html.append("<p>${parseInlineMarkdown(trimmed.drop(2))}</p>\n")
            }

            trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                if (inList) {
                    html.append("</ul>\n")
                    inList = false
                }
                if (!inOrderedList) {
                    html.append("<ol>\n")
                    inOrderedList = true
                }
                val content = trimmed.replaceFirst(Regex("^\\d+\\.\\s"), "")
                html.append("<li>${parseInlineMarkdown(content)}</li>\n")
            }

            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                if (inOrderedList) {
                    html.append("</ol>\n")
                    inOrderedList = false
                }
                if (!inList) {
                    html.append("<ul>\n")
                    inList = true
                }
                val content = trimmed.drop(2)
                html.append("<li>${parseInlineMarkdown(content)}</li>\n")
            }

            trimmed.matches(Regex("^[-*_]{3,}$")) -> {
                if (inList) {
                    html.append("</ul>\n")
                    inList = false
                }
                if (inOrderedList) {
                    html.append("</ol>\n")
                    inOrderedList = false
                }
                if (inBlockquote) {
                    html.append("</blockquote>\n")
                    inBlockquote = false
                }
                html.append("<hr>\n")
            }

            trimmed.isEmpty() -> {
                if (inList) {
                    html.append("</ul>\n")
                    inList = false
                }
                if (inOrderedList) {
                    html.append("</ol>\n")
                    inOrderedList = false
                }
                if (inBlockquote) {
                    html.append("</blockquote>\n")
                    inBlockquote = false
                }
            }

            else -> {
                if (inList) {
                    html.append("</ul>\n")
                    inList = false
                }
                if (inOrderedList) {
                    html.append("</ol>\n")
                    inOrderedList = false
                }
                if (inBlockquote) {
                    html.append("</blockquote>\n")
                    inBlockquote = false
                }
                html.append("<p>${parseInlineMarkdown(trimmed)}</p>\n")
            }
        }
    }

    if (inList) html.append("</ul>\n")
    if (inOrderedList) html.append("</ol>\n")
    if (inBlockquote) html.append("</blockquote>\n")
    if (inCodeBlock) html.append("</pre></code>\n")

    return if (inHtmlFormat) html.toSpanned()
    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(html.toString(), Html.FROM_HTML_MODE_LEGACY)
    else Html.fromHtml(html.toString())
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

fun String.writeInternalFile(context: Context, fileName: String): String {
    File(context.filesDir, fileName).writeText(this)
    return this
}

fun Any?.similar(other: Any?): Boolean {
    return this.toString().lowercase() == other.toString().lowercase()
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

fun parseInlineMarkdown(text: String): String {
    return text
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
        .replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
        .replace(Regex("`(.+?)`"), "<code>$1</code>")
        .replace(Regex("\\[([^]]+)]\\(([^)]+)\\)"), "<a href=\"$2\">$1</a>")
        .replace(Regex("~~(.+?)~~"), "<del>$1</del>")
        .replace(Regex("\\*\\*\\*(.+?)\\*\\*\\*"), "<strong><em>$1</em></strong>")
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
        .replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
}

fun List<Pair<Bitmap, String>>.optimisedMultiPhotos(): Bitmap {
    val imageSize = 200
    val captionHeight = 40
    val totalHeight = imageSize + captionHeight
    val cols = 4
    val rows = (this.size + cols - 1)/cols

    val width = cols * imageSize
    val height = rows * totalHeight
    val data = createBitmap(width, height)
    val canvas = android.graphics.Canvas(data)

    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    val textPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 24f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    for (i in this.indices) {
        val row = i / cols
        val col = i % cols
        val x = col * imageSize
        val y = row * totalHeight

        val (bitmap, timestamp) = this[i]
        val scaledBitmap = bitmap.scale(imageSize, imageSize)

        canvas.drawBitmap(scaledBitmap, x.toFloat(), y.toFloat(), null)
        canvas.drawText(timestamp, (x + imageSize / 2).toFloat(), (y + imageSize + 30).toFloat(), textPaint)
        scaledBitmap.recycle()
    }
    return data
}