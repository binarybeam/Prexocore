# Prexocore

> **Android devs: tired of bloated XML, click hell, and boilerplate?**
> **Prexocore** is your Kotlin-first escape hatch — handle toasts, permissions, inputs, dialogs, navigation, and more, with expressive one-liners and zero friction.

---

## ⚡ What Makes Prexocore Different?

🧠 **Context-aware** – Works inside any `Context`, `Activity`, or `Fragment`, seamlessly.

💥 **No XML** – All common views & layouts bundled internally.

🔐 **Safe by default** – Click debounce, single-toast, no memory leaks.

🛠️ **Swiss Army Knife** – One toolkit to rule them all.

🧬 **Minimal & expressive** – Feels native in Kotlin.

---

## 🚀 Real Problems. Real Fixes.

| Pain                       | Prexocore Fix                            |
| -------------------------- | ---------------------------------------- |
| 10+ lines to show a dialog | `alert("Title", "Message", "OK") {}`     |
| Repeated toast spamming    | `safeToast("One toast at a time")`       |
| Double clicks on buttons   | `view.onSafeClick {}`                    |
| Keyboard height handling   | `onKeyboardChange { isOpen, height -> }` |
| Manual permissions code    | `permission.request(...) { granted -> }` |
| Tedious RecyclerView setup | `recyclerView.adapter(...) {}`           |

---

## 📸 Quick Demo

> Want to see it in action? Check out [this here →](https://prexocore.prexoft.com)

---

## 🧪 Before vs After (Dialog Example)

**🔴 Without Prexocore**

```kotlin
AlertDialog.Builder(this)
    .setTitle("Title")
    .setMessage("Message")
    .setPositiveButton("OK") { dialog, _ ->
        dialog.dismiss()
    }
    .show()
```

**🟢 With Prexocore**

```kotlin
alert("Title", "Message", "OK") { acknowledged -> ... }
```

---


## Setup

### Gradle Dependency

Add JitPack to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

And include the dependency in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.binarybeam:Prexocore:1.2.0")
}
```

---

## 🧰 Full Feature List

#### Notifications

```kotlin
postNotification(
    title = "Hello",
    content = "This is a notification"

    // More optional customisations available
)
```

### Keyboard Handling

```kotlin
onKeyboardChange() { isOpen, height->
    // view.setHeight(dp = height.toDp())
}

// Or get ont time status
if (isKeyboardOpen) { ... }
```

---

### UI & Input Enhancements

#### View Utilities

```kotlin
view.show()       // With stunning fade in/out effect
view.hide()
view.setHeight(dp = 12)
view.setWidth(dp = 20)
```

#### Click Management

```kotlin
view.onClick { ... }                        // With enahanced feedback
view.onSafeClick(seconds = 1.5) { ... }     // Avoid multiple clicks frequently
view.onFirstClick { ... }                   // One-time only click
view.onDoubleClick { ... }
```

#### Smart Messages

```kotlin
toast("Simple Message")        
safeToast("Debounced Message")  // One toast at a time
snack("Message", "Retry") { clicked ->  }
```

#### Redirects & Navigation Bindings

```kotlin
button.redirect(MyActivity::class)
link.redirect("https://prexoft.com")
mail.redirect("founder@prexoft.com")
```

#### Input Focus Management

```kotlin
editText.focus()             // Auto handles keyboard visibility
editText.distract()
```

---

### Navigation System

```kotlin
goTo(MyActivity::class)
goTo("1234567890")
goTo("mailto:support@prexoft.com")
goTo(myIntent)
goTo("github.com/binarybeam")
```

---

### `similar` Function

Compare two objects loosely based on their string representation.

#### Features

* Case-insensitive comparison
* Optional whitespace ignoring
* Normalizes diacritics (é ≈ e)

```kotlin
"John Doe".similar("johndoe")               // true
"12345".similar(12345)                      // true
"résumé".similar("resume")                  // true
null.similar("null")                        // true (default ignoreNull = true)
null.similar("null", ignoreNull = false)    // false
```

---

### `normalize` Function

This helper strips diacritics from any `CharSequence` to create a normalized string for comparison.

```kotlin
"résumé".normalize()      // "resume"
"Café".normalize()        // "Cafe"
"mañana".normalize()      // "manana"
"Ångström".normalize()    // "Angstrom"
```

---

### Media Sharing

```kotlin
"Some String".share()
share(bitmap)
"Infortant Info".copy()
```

---

### Delay Utilities

```kotlin
after(seconds = 1.5, loop = 3) {
    // Delayed execution every 1.5 seconds, 3 times
}

5.loop { i ->
    // Loop 5 times safely
}

loop(10) { i ->
    // Loop with optional safety for large numbers
}
```

---

### `speak` Utility Function

Quick helper to speak text with configurable rate, pitch, and locale. `onDone` is invoked when speaking finishes.

```kotlin
speak("Hello, world!") {
    // finished speaking
}
```

```kotlin
override fun onDestroy() {
    shutdownSpeaker()
    super.onDestroy()
}
```

---

### Dialog & Input Prompts

```kotlin
alert("Info", "Message", "OK") { acknowledged -> ... }
input("Name", "Enter your name", required = true) { name -> ... }
```

---

### Permission Handing

```kotlin
private val permission = Permission(this)
```

```kotlin
if (havePermission(permission.CAMERA)) { ... }
else {
    permission.request(permission.CAMERA) { granted ->
        if (granted) { ... }
    }
}
```

---

### Time Format Helpers

```kotlin
now().formatAsTime()              // 11:31 pm
now().formatAsDate()              // 31.07.2025
now().formatAsDateAndTime()       // 11:31 pm, 31 Jul 2025
```

---

### Network Monitoring

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

```kotlin
observeNetworkStatus { isConnected -> ... }

// Or get one time status
if (isNetworkAvailable()) { ... }
```

---

### RecyclerView Quick Adapter

```kotlin
val adapter = recyclerView.adapter(R.layout.item_layout, list) { position, view, item ->
    // handle list rows
}

adapter.updateItems(newItems)
```

---

### Advanced RecyclerView Bindings

```kotlin
recyclerView.adapter(itemCount = 5) { pos, icon, label ->
    // Bind icons and labels for list
}

recyclerView.adapter(layout = R.layout.item_layout, itemCount = 10) { pos, view ->
    // Bind view by layout
}

recyclerView.adapter(listOfData, layoutType = Prexo.GRID_LAYOUT) { pos, icon, label, item ->
    // Grid or linear layouts
}
```

---

### Scroll Detection

```kotlin
scrollView.onScroll(
    onTop = { /* reached top */ },
    onBottom = { /* reached bottom */ },
    other = { /* somewhere else */ }
)
{ percent ->
    // scrolled $percent %
}
```

---

### Stylings

Parse Markdown Styling

```kotlin
markdownText.parseMarkdown(inHtmlFormat = false) { spanned ->
    textView.text = spanned
}
```

Parse Html Styling

```kotlin
htmlText.parseHtml() { spanned ->
    textView.text = spanned
}
```

Unemojify

```kotlin
tooMuchEmojisText.unEmojify() { textWithNoEmojis ->
    textView.text = textWithNoEmojis
}
```

---

### Miscellaneous Tools

```kotlin
intent.start()
uri.open()
123456.dial()
file.read()
"A useful info".writeInternalFile("info.txt")
readInternalFile("info.txt")
"abc".append("def")
listOf(tv1, tv2).setText(listOf("A", "B"))
captureScreen { bitmap -> ... }
vibrate(minimal = true)
view(R.id.text)
button.redirect("github.com/binarybeam")
```

---

### View Tree Utilities

```kotlin
val allViews = rootView.getViews()
val allButtons = rootView.getViews(Button::class)
```

---

### Speech Recognition

```kotlin
listenSpeech(keepListening = true) { result ->
    // handle voice input
}
```

---

## License

```text
Apache License 2.0
Copyright (c) 2025 Prexoft
```

---

Made with ❤️ by [Prexoft](https://github.com/binarybeam)
