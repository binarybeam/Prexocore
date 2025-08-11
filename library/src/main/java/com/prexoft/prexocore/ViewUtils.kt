package com.prexoft.prexocore

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prexoft.prexocore.anon.Prexo
import com.prexoft.prexocore.helper.AdapterWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.contracts.Effect
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.reflect.KClass

fun EditText.focus() {
    this.requestFocus()
    this.post {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun EditText.distract() {
    this.clearFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun EditText.onTextChange(
    needPasteInfo: Boolean = true,
    callback: (text: String, isPasted: Boolean) -> Unit
) {
    var isPasteOperation = false

    if (needPasteInfo) {
        setEditableFactory(object : Editable.Factory() {
            override fun newEditable(source: CharSequence): Editable {
                return object : SpannableStringBuilder(source) {
                    override fun replace(
                        start: Int,
                        end: Int,
                        tb: CharSequence?,
                        tbstart: Int,
                        tbend: Int
                    ): SpannableStringBuilder {
                        val insertedLength = tbend - tbstart
                        val deletedLength = end - start

                        if (insertedLength > 1 && deletedLength == 0) {
                            isPasteOperation = true
                        }

                        return super.replace(start, end, tb, tbstart, tbend)
                    }
                }
            }
        })
    }

    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (needPasteInfo && count > 1 && before == 0) isPasteOperation = true

            callback(s?.toString() ?: "", isPasteOperation)
            isPasteOperation = false
        }

        override fun afterTextChanged(s: Editable?) { }
    })
}

fun TextView.onTextChange(
    callback: (text: String) -> Unit
) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { callback(s?.toString() ?: "") }
        override fun afterTextChanged(s: Editable?) { }
    })
}

fun View.setHeight(dp: Double) {
    val px = context.dpToPx(dp)
    val params = layoutParams ?: ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        px
    )
    params.height = px
    layoutParams = params
}

fun View.setHeightAndWidth(height: Double, width: Double) {
    val px1 = context.dpToPx(height)
    val px2 = context.dpToPx(width)

    val params = layoutParams ?: ViewGroup.LayoutParams(
        px2,
        px1
    )
    params.height = px1
    params.width = px2
    layoutParams = params
}

fun View.setHeightAndWidth(height: Int, width: Int) {
    setHeightAndWidth(height.toDouble(), width.toDouble())
}

fun View.setHeightAndWidth(height: Double, width: Int) {
    setHeightAndWidth(height, width.toDouble())
}

fun View.setHeightAndWidth(height: Int, width: Double) {
    setHeightAndWidth(height.toDouble(), width)
}

fun View.setHeight(dp: Int) {
    setHeight(dp.toDouble())
}

fun View.setWidth(dp: Double) {
    val px = context.dpToPx(dp)
    val params = layoutParams ?: ViewGroup.LayoutParams(
        px,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.width = px
    layoutParams = params
}

fun View.setWidth(dp: Int) {
    setWidth(dp.toDouble())
}

fun View.redirect(cls: Class<*>, extras: Bundle? = null, flags: Int? = null, options: Bundle? = null) {
    this.onClick {
        context.goTo(cls, extras, flags, options)
    }
}

fun View.redirect(kClass: KClass<*>, extras: Bundle? = null, flags: Int? = null, options: Bundle? = null) {
    this.onClick {
        context.goTo(kClass, extras, flags, options)
    }
}

fun View.redirect(intent: Intent) {
    this.onClick {
        context.goTo(intent)
    }
}

fun View.redirect(uri: Uri) {
    this.onClick {
        context.goTo(uri)
    }
}

fun View.redirect(number: Int) {
    this.onClick {
        context.goTo(number)
    }
}

fun View.redirect(uri: String) {
    this.onClick {
        context.goTo(uri)
    }
}

fun View.show(duration: Long = 300) {
    if (duration == 0L) this.isVisible = true
    else this.fadeIn(duration)
}

fun View.hide(duration: Long = 300) {
    if (duration == 0L) this.isVisible = false
    else this.fadeOut(duration)
}

fun View.bounce(duration: Long = 300) {
    val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 0.85f, 1.0f)
    val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0.85f, 1.0f)

    scaleX.interpolator = AccelerateDecelerateInterpolator()
    scaleY.interpolator = AccelerateDecelerateInterpolator()

    AnimatorSet().apply {
        playTogether(scaleX, scaleY)
        this.duration = duration
        start()
    }
}

fun View.click() {
    context.vibrate(false, minimal = true)
    this.performClick()
}

fun View.view(@IdRes id: Int): View = findViewById(id)

fun View.onClick(effect: Boolean = true, feedback: Boolean = true, listener: (View) -> Unit) {
    this.setOnClickListener {
        if (feedback) context.vibrate(legacyFallback = false, minimal = true)
        if (effect) it.bounce()
        listener(it)
    }
}

fun View.onSafeClick(durationInSeconds: Double, effect: Boolean = true, feedback: Boolean = true, listener: (View) -> Unit) {
    var lastClicked = 0L
    this.setOnClickListener {
        val now = System.currentTimeMillis()
        if (now - lastClicked > durationInSeconds*1000) {
            lastClicked = now
            
            if (feedback) context.vibrate(legacyFallback = false, minimal = true)
            if (effect) it.bounce()
            listener(it)
        }
    }
}

fun View.onFirstClick(effect: Boolean = true, feedback: Boolean = true, listener: (View) -> Unit) {
    this.setOnClickListener {
        if (feedback) context.vibrate(legacyFallback = false, minimal = true)
        if (effect) it.bounce()

        listener(it)
        this.setOnClickListener { null }
        this.isClickable = false
    }
}

fun View.onDoubleClick(gapInSeconds: Double = 0.3, effect: Boolean = true, feedback: Boolean = true, listener: (View) -> Unit) {
    var lastClicked = 0L
    this.setOnClickListener {
        val now = System.currentTimeMillis()
        if (now - lastClicked < gapInSeconds*1000) {
            lastClicked = 0
            if (feedback) context.vibrate(legacyFallback = false, minimal = true)
            if (effect) it.bounce()
            listener(it)
        }
        else lastClicked = now
    }
}

fun View.onLongClick(consume: Boolean = true, effect: Boolean = true, feedback: Boolean = true, listener: (View) -> Unit) {
    this.setOnLongClickListener {
        if (feedback) context.vibrate(legacyFallback = false, minimal = true)
        if (effect) it.bounce()
        listener(it)
        consume
    }
}

fun View.onSafeClick(durationInSeconds: Int = 1, effect: Boolean = true, feedback: Boolean = true, listener: (View) -> Unit) {
    onSafeClick(durationInSeconds.toDouble(), effect, feedback, listener)
}

fun View.scaleDown(show: Boolean, duration: Long = 300) {
    if (show) {
        isVisible = true
        scaleY = 1f
    }
    else scaleY = 0f
    animate()
        .scaleY(if (show) 0f else 1f)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction { isVisible = show }
        .start()
}

fun View.scaleUp(show: Boolean, duration: Long = 300) {
    if (show) {
        isVisible = true
        scaleY = 1f
    }
    else scaleY = 0f
    animate()
        .scaleY(if (show) 0f else 1f)
        .setDuration(duration)
        .setInterpolator(AccelerateInterpolator())
        .withEndAction { isVisible = show }
        .start()
}

fun View.rotate(angle: Int = 360, duration: Long = 300) {
    animate()
        .rotation(angle.toFloat())
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .start()
}

fun View.fadeIn(duration: Long) {
    isVisible = true
    animate()
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .start()
}

fun <T : View> View?.getViews(type: KClass<T>): List<T> {
    val result = mutableListOf<T>()

    fun recurse(view: View, add: Boolean = true) {
        if (add && type.java.isInstance(view)) {
            @Suppress("UNCHECKED_CAST")
            result.add(view as T)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                recurse(view.getChildAt(i))
            }
        }
    }

    if (this == null) return result
    recurse(this, false)
    return result
}

fun View?.getViews(): List<View> {
    val result = mutableListOf<View>()

    fun recurse(view: View, toAdd: Boolean = true) {
        if (toAdd) result.add(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                recurse(view.getChildAt(i))
            }
        }
    }

    if (this == null) return result
    recurse(this, false)
    return result
}

fun View.fadeOut(duration: Long) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .withEndAction { isVisible = false }
        .start()
}

fun <T> RecyclerView.adapter(
    @LayoutRes layout: Int,
    items: List<T>,
    layoutManager: RecyclerView.LayoutManager? = null,
    bind: (position: Int, view: View, item: T) -> Unit
): AdapterWrapper<T> {
    if (this.layoutManager == null) {
        this.layoutManager = layoutManager ?: LinearLayoutManager(this.context)
    }

    val listItems = items.toMutableList()
    val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = listItems.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            bind(position, holder.itemView, listItems[position])
        }

        @SuppressLint("NotifyDataSetChanged")
        fun update(newItems: List<T>) {
            listItems.clear()
            listItems.addAll(newItems)
            notifyDataSetChanged()
        }
        val updateItems: (List<T>) -> Unit = ::update
    }

    this.adapter = adapter
    return AdapterWrapper(adapter, adapter.updateItems)
}

fun <T> RecyclerView.adapter(
    items: List<T>,
    layoutType: Prexo = Prexo.LINEAR_LAYOUT,
    spanCountForGrid: Int = 2,
    bind: (position: Int, icon: ImageView, textView: TextView, item: T) -> Unit
): AdapterWrapper<T> {
    this.layoutManager = when (layoutType) {
        Prexo.GRID_LAYOUT -> GridLayoutManager(this.context, spanCountForGrid)
        else -> LinearLayoutManager(this.context)
    }

    val listItems = items.toMutableList()
    val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = listItems.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                when (layoutType) {
                    Prexo.GRID_LAYOUT -> R.layout.grid_recycler_item
                    else -> R.layout.linear_recycler_item
                }, parent, false)
            return object : RecyclerView.ViewHolder(view) { }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            bind(position, holder.itemView.findViewById(R.id.icon), holder.itemView.findViewById(R.id.textView), listItems[position])
        }

        @SuppressLint("NotifyDataSetChanged")
        fun update(newItems: List<T>) {
            listItems.clear()
            listItems.addAll(newItems)
            notifyDataSetChanged()
        }
        val updateItems: (List<T>) -> Unit = ::update
    }

    this.adapter = adapter
    return AdapterWrapper(adapter, adapter.updateItems)
}

fun RecyclerView.adapter(
    @LayoutRes layout: Int,
    itemCount: Int,
    layoutManager: RecyclerView.LayoutManager? = null,
    bind: (position: Int, view: View) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder> {
    if (this.layoutManager == null) {
        this.layoutManager = layoutManager ?: LinearLayoutManager(this.context)
    }

    val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = itemCount

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            bind(position, holder.itemView)
        }
    }

    this.adapter = adapter
    return adapter
}

fun RecyclerView.adapter(
    itemCount: Int,
    layoutType: Prexo = Prexo.LINEAR_LAYOUT,
    spanCountForGrid: Int = 2,
    bind: (position: Int, icon: ImageView, textView: TextView) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder> {

    this.layoutManager = when (layoutType) {
        Prexo.GRID_LAYOUT -> GridLayoutManager(this.context, spanCountForGrid)
        else -> LinearLayoutManager(this.context)
    }

    val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount(): Int = itemCount

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                when (layoutType) {
                    Prexo.GRID_LAYOUT -> R.layout.grid_recycler_item
                    else -> R.layout.linear_recycler_item
                }, parent, false)

            return object : RecyclerView.ViewHolder(view) { }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            bind(position, holder.itemView.findViewById(R.id.icon), holder.itemView.findViewById(R.id.textView))
        }
    }

    this.adapter = adapter
    return adapter
}

fun ScrollView.onScroll (
    onTop: () -> Unit = {},
    onBottom: () -> Unit = {},
    other: () -> Unit = {},
    percentCallback: ((Int) -> Unit)? = null
) {
    var lastState = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val maxScroll = max(getChildAt(0).measuredHeight - height, 1)
            val percent = ((scrollY.toFloat() / maxScroll) * 100).roundToInt().coerceIn(0, 100)
            percentCallback?.invoke(percent)

            when {
                scrollY == 0 -> {
                    if (lastState != 0) {
                        lastState = 0
                        context.vibrate(legacyFallback = false, minimal = true)
                        onTop()
                    }
                }
                scrollY >= maxScroll -> {
                    if (lastState != 1) {
                        lastState = 1
                        context.vibrate(legacyFallback = false, minimal = true)
                        onBottom()
                    }
                }
                else -> {
                    if (lastState != 2) {
                        lastState = 2
                        other()
                    }
                }
            }
        }
    }
}

fun HorizontalScrollView.onScroll (
    onTop: () -> Unit = {},
    onBottom: () -> Unit = {},
    other: () -> Unit = {},
    percentCallback: ((Int) -> Unit)? = null
) {
    var lastState = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setOnScrollChangeListener { _, scrollX, _, _, _ ->
            val maxScroll = max(getChildAt(0).measuredHeight - height, 1)
            val percent = ((scrollX.toFloat() / maxScroll) * 100).roundToInt().coerceIn(0, 100)
            percentCallback?.invoke(percent)

            when {
                scrollX == 0 -> {
                    if (lastState != 0) {
                        lastState = 0
                        context.vibrate(legacyFallback = false, minimal = true)
                        onTop()
                    }
                }
                scrollX >= maxScroll -> {
                    if (lastState != 1) {
                        lastState = 1
                        context.vibrate(legacyFallback = false, minimal = true)
                        onBottom()
                    }
                }
                else -> {
                    if (lastState != 2) {
                        lastState = 2
                        other()
                    }
                }
            }
        }
    }
}

fun List<TextView>.setText(list: List<String>) {
    this.forEach {
        it.text = list.getOrNull(this.indexOf(it)) ?: ""
    }
}

fun ImageView.load(source: Any?, @DrawableRes placeholder: Int? = null, onResult: (Boolean) -> Unit = {}) {
    if (source == null) return
    val imageView = this
    if (placeholder != null) this.setImageResource(placeholder)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val bitmap = when (source) {
                is Drawable -> source.toBitmap()
                is Bitmap -> source
                is Int -> BitmapFactory.decodeResource(context.resources, source)
                is String -> {
                    if (source.startsWith("http")) {
                        val url = URL(source)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.doInput = true
                        conn.connect()
                        BitmapFactory.decodeStream(conn.inputStream)
                    }
                    else BitmapFactory.decodeFile(source)
                }
                is File -> BitmapFactory.decodeFile(source.absolutePath)
                is Uri -> {
                    val inputStream = context.contentResolver.openInputStream(source)
                    BitmapFactory.decodeStream(inputStream)
                }
                else -> null
            }

            bitmap?.let {
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(it)
                    onResult(true)
                }
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            onResult(false)
        }
    }
}