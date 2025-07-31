# Prexocore 🎞️

**Prexocore** is a lightweight yet powerful Kotlin utility library for Android that dramatically simplifies day-to-day development. It offers streamlined APIs and Kotlin extension functions for common tasks such as permission handling, navigation, view manipulation, input prompts, toast control, and much more — all while removing boilerplate code.

Whether you're working on a small side project or a large-scale app, this library helps you move faster and write cleaner code with built-in layouts and intuitive abstractions.

---

## 🚀 Features Overview

This library includes a broad range of tools and utilities that cover:

* 🔧 Activity and context-level helpers
* 🖼️ View visibility, dimension, and animation controls
* 🧠 Keyboard tracking and helper
* 🔐 Easy Permission handling (classic and modern APIs)
* 🌐 Network connectivity monitoring
* 🔊 Safe and standard alerts
* 📢 Runtime Alert dialogs and input prompts
* 🔁 Intent-based navigation and deep linking
* ⏱️ Data formatting helpers
* 📃 Easiest RecyclerView adapter binding
* 🧲 Advanced Interaction hanling
* 📷 One-line image loading
* 🔀 Scroll state handling
* 📂 File and string extensions

All components are internally bundled. No XML configuration or external layout files are needed.

---

## 🛠️ Setup

### Gradle Dependency

To use this library, add the following dependency in your `build.gradle`:

```kotlin
    dependencies {
        implementation("com.github.binarybeam:Prexocore:1.0.0")
    }
```

Add it in your settings.gradle.kts at the end of repositories:

```kotlin
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
                	mavenCentral()
			maven { url = uri("https://jitpack.io") }
               }
	}
```
---

## 📱 Activity Utilities

### 📸 Capture Screen as Bitmap

Capture the current screen and receive the result as a `Bitmap`:

```kotlin
activity.captureScreen { bitmap ->
    // Handle bitmap
}
```

### 🍽️ Keyboard Visibility Listener

Track keyboard show/hide events globally:

```kotlin
activity.onKeyboardChange { isOpen -> ... }
activity.onKeyboardChange { isOpen, height -> ... }
```

### 📝 View Lookup Shortcut

Access views with generic casting:

```kotlin
val button = activity.view<Button>(R.id.myButton)
```

### 📣 Snackbar Helper

Quickly show a snackbar with optional action:

```kotlin
activity.snack("Message shown", "Retry") { clicked -> ... }
```

---

## 🔐 Permission Handling

### 📍 Classic Request

Trigger runtime permission request (legacy approach):

```kotlin
activity.getPermission(listOf(Manifest.permission.CAMERA))
```

### 📍 Request With Callback

Use the modern Activity Result API to request and handle permission results:

```kotlin
activity.getPermission(Manifest.permission.READ_EXTERNAL_STORAGE) { granted ->
    if (granted) { ... }
}
```

### ✅ Check Permission Status

```kotlin
context.havePermission(Manifest.permission.ACCESS_FINE_LOCATION)
```

---

## 🌐 Network State

### 📱 Observe Network Connectivity

React to changes in online/offline state:

```kotlin
lifecycleOwner.observeNetworkStatus(context) { isConnected -> ... }
```

### 🔌 Quick Availability Check

```kotlin
if (context.isNetworkAvailable()) { ... }
```

---

## 🔊 Toasts & Vibration

### ✨ Safe Toast (Rate-limited)

Avoid spamming the user with repeated messages:

```kotlin
context.safeToast("Action completed")
```

### 🎧 Regular Toast

Standard Android toast:

```kotlin
context.toast("Simple toast")
```

### 🚗 Trigger Vibration

```kotlin
context.vibrate()
```

---

## ⚠️ Alerts & Input Prompts

### 📢 Alert Dialog

```kotlin
context.alert("Title", "Message", "Close") { acknowledged -> ... }
```

### ⌨️ Input Dialog

```kotlin
context.input("Feedback", "Please share your thoughts", "Type here") { inputText -> ... }
```

---

## 🔄 Navigation Helpers

### 🚀 Intent-based Navigation

```kotlin
context.goTo(MyActivity::class)
context.goTo("tel:1234567890")
context.goTo("https://prexoft.com")
context.goTo(myIntent)
```

---

## 📅 Time Utilities

### ⏰ Format `Long` timestamps

```kotlin
val now = System.currentTimeMillis()
now.formatAsTime()         // e.g., 08:30 AM
now.formatAsDate()         // e.g., 30.07.2025
now.formatAsDateAndTime()  // e.g., 08:30 AM, 30 Jul 2025
```

---

## 📊 View Utilities

### ⇳ View Size Adjustment

```kotlin
view.setHeight(120)
view.setWidth(200)
view.setHeightAndWidth(120, 200)
```

### 👁️ View Visibility & Animation

```kotlin
view.show()
view.hide()
view.fadeIn(300)
view.fadeOut(300)
view.bounce()
```

### 💪 Transformations

```kotlin
view.scaleDown(true)
view.scaleUp(false)
view.rotate()
```

---

## 🔍 Click Listeners

### 🖱️ Enhanced Click Handling

```kotlin
view.onClick { ... }
view.onSafeClick(1.5) { ... }   // Prevent rapid double-taps
view.onDoubleClick { ... }
view.onLongClick { ... }
```

---

## 📃 RecyclerView Adapter Setup

Bind a list with minimal code:

```kotlin
recyclerView.adapter(R.layout.item_layout, itemsList) { pos, view, item ->
    view.view<TextView>(R.id.title).text = item.title
}
```

Update list data dynamically:

```kotlin
adapterWrapper.updateItems(newList)
```

---

## 🚗 Scroll Detection

### 📜 ScrollView & HorizontalScrollView

Listen to scroll state and position:

```kotlin
scrollView.onScroll(
    onTop = { ... },
    onBottom = { ... },
    other = { ... },
    percentCallback = { percent -> ... }
)
```

---

## 📷 Image Loading

### 🖼️ Load from URL with Placeholder

```kotlin
imageView.loadFromUrl("https://example.com/image.jpg", R.drawable.placeholder)
```

---

## 🧰 Miscellaneous Utilities

```kotlin
"Share this text".share(context)
"Copy me".copy(context)
1234567890.dial(context)
intent.start(context)
file.read()
"abc".append("def")
```

---

## 📄 License

This library is distributed under the APACHE-2.0 LICENSE.

---

Made with ❤️ by [Prexoft](https://github.com/binarybeam)
