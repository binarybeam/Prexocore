# 📦 Prexocore

**Prexocore** is a Kotlin-first Android utility library designed to supercharge your development workflow. With powerful, minimal, and expressive APIs, it simplifies everyday Android tasks like view manipulation, permission handling, network monitoring, image loading, navigation, and much more — all while eliminating repetitive boilerplate code.

Whether you’re creating a small utility app or a large-scale production product, **Prexocore** helps you build faster, write cleaner code, and keep your logic focused. No XML setup. No weird configurations. Just power-packed utilities — ready to use.

---

## 🚀 Why Prexocore?

* ⚡ **Minimal Setup:** Add one dependency and you're ready.
* 🧠 **Smart Extensions:** Idiomatic Kotlin extensions for `Activity`, `Context`, `View`, `RecyclerView`, and more.
* 🎨 **No Layout Fuss:** Internally bundled view layouts and dialogs — no need to define your own XML.
* 🛡️ **Safe & Clean:** Includes debounce handling, safe click listeners, and guarded toasts to avoid UI spam.
* 🔌 **Universal Compatibility:** Works across `Activity`, `Fragment`, `Context`, and `LifecycleOwner` seamlessly.

---

## 🛠️ Setup

### Gradle Dependency

Add JitPack to your project-level `settings.gradle.kts`:

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

Then, add this to your app-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.binarybeam:Prexocore:1.0.0")
}
```

---

## ✨ Top Features

All extensions are available in any `Context`, `Activity`, `Fragment`, or `LifecycleOwner` unless specified.

---

### 🖱️ Safe & Enhanced Click Handling

```kotlin
view.onClick { /* Handle click */ }
view.onSafeClick(1.5) { /* Avoid double-taps */ }
view.onDoubleClick { /* Handle double tap */ }
view.onLongClick { /* Long press */ }
```

---

### 📃 RecyclerView Binding Simplified

Quickly bind and update lists without adapter classes:

```kotlin
recyclerView.adapter(R.layout.item_layout, itemsList) { pos, view, item ->
    view.view<TextView>(R.id.title).text = item.title
}

adapterWrapper.updateItems(newList)
```

---

### 📢 Alerts & Input Dialogs

Built-in, customizable alert and input dialogs — no XML or DialogFragments needed:

```kotlin
alert("Title", "Message", "Close") { acknowledged -> ... }

input("Feedback", "Please share your thoughts", "Type here") { inputText -> ... }
```

---

### 📷 One-line Image Loading

```kotlin
imageView.loadFromUrl("https://example.com/image.jpg", R.drawable.placeholder)
```

---

### 🌐 Network State Monitoring *(Requires `ACCESS_NETWORK_STATE`)*

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

```kotlin
lifecycleOwner.observeNetworkStatus { isConnected -> ... }

if (isNetworkAvailable()) { /* online */ }
```

---

### 🔐 Permission Handling

* **Classic Request:**

```kotlin
getPermission(listOf(Manifest.permission.CAMERA))
```

* **Modern API with Callback:**

```kotlin
getPermission(Manifest.permission.READ_EXTERNAL_STORAGE) { granted ->
    if (granted) { /* Permission granted */ }
}
```

* **Check Permission:**

```kotlin
havePermission(Manifest.permission.ACCESS_FINE_LOCATION)
```

---

### ⌨️ Keyboard Tracking

Track keyboard visibility and height:

```kotlin
onKeyboardChange { isOpen -> ... }
onKeyboardChange { isOpen, height -> ... }
```

---

### 📸 Capture Screen

```kotlin
captureScreen { bitmap -> /* Use the captured Bitmap */ }
```

---

### ⚙️ View Utilities

* **Visibility & Animation:**

```kotlin
view.show()
view.hide()
view.fadeIn(300)
view.fadeOut(300)
view.bounce()
```

* **Resize / Transform:**

```kotlin
view.setHeight(120)
view.setWidth(200)
view.scaleUp(true)
view.rotate()
```

* **Generic View Lookup:**

```kotlin
val button = view.view<Button>(R.id.submitButton)
```

---

### 📅 Time Formatting

```kotlin
val now = System.currentTimeMillis()
now.formatAsTime()         // e.g., 08:30 AM
now.formatAsDate()         // e.g., 30.07.2025
now.formatAsDateAndTime()  // e.g., 08:30 AM, 30 Jul 2025
```

---

### 🚗 Navigation Made Easy

```kotlin
goTo(MyActivity::class)
goTo("tel:1234567890")
goTo("https://prexoft.com")
goTo(myIntent)
```

---

### 🔊 Toasts & Vibration

```kotlin
safeToast("Action completed")
toast("Simple message")
vibrate()
```

---

### 📜 Scroll Listeners

```kotlin
scrollView.onScroll(
    onTop = { /* reached top */ },
    onBottom = { /* reached bottom */ },
    other = { /* in between */ },
    percentCallback = { percent -> /* scroll % */ }
)
```

---

### 🧰 Misc Utilities

```kotlin
"Text to share".share()
"Copy this".copy()
1234567890.dial()
intent.start()
file.read()
"abc".append("def")
```

---

## 📄 License

```text
Apache License 2.0
Copyright (c) 2025 Prexoft
```

---

Made with ❤️ by [Prexoft](https://github.com/binarybeam)
