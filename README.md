# 📦 Prexocore

**Prexocore** isn’t just another utility library — it’s a curated collection of high-performance, developer-first Kotlin tools engineered to elevate Android development to the next level. It dramatically reduces boilerplate while offering safe, expressive, and delightful APIs across UI, navigation, feedback, and system utilities.

Imagine handling permissions, dialogs, clicks, navigation, image loading, toasts, or notifications — all with one-liners and no XML. **Prexocore** empowers you with elegant APIs that just work, with sane defaults and seamless context awareness.

---

## 🚀 Why Prexocore?

- ⚡ **Zero Config, Zero XML:** Internally bundled views and layouts. No XML hassle.
- 💎 **Kotlin-First, Boilerplate-Zero:** Idiomatic Kotlin extensions that feel native.
- 🧠 **Context-Aware Everywhere:** Whether you're in an `Activity`, `Context`, or `Fragment`, everything works.
- 🧩 **All-in-One Toolkit:** From UI and system tools to advanced interactions.
- 🧪 **Innovative Abstractions:** Handles one-time clicks, seamless sharing, chained vibrations, dynamic navigation, and more.

---

## 🛠️ Setup

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
    implementation("com.github.binarybeam:Prexocore:1.0.0")
}
```

---

## ✨ Feature Highlights

All utilities work in any `Context`, `Activity`, or `Fragment`. Prexocore detects the environment internally so you don’t have to worry.

---

### 🧠 Intelligent Feedback System

#### 🔊 Smart Toasts & Vibrations
```kotlin
toast("Simple Message")        // Auto vibrates
safeToast("Debounced Message")
vibrate()                       // Tactile feedback
```

#### 🎯 Snackbar with Action
```kotlin
snack("Message", "Retry") { clicked -> if (clicked) { ... } }
```

#### 🛎️ Notifications (Android N+)
```kotlin
postNotification(
    title = "Hello",
    content = "This is a notification",
    smallIcon = R.drawable.ic_notify,
    launchIntent = Intent(this, MainActivity::class.java)
)
```

---

### 🕹️ UI & Input Enhancements

#### 🎈 View Utilities
```kotlin
view.show()
view.hide()
view.fadeIn(300)
view.bounce()
view.setHeight(120)
view.setWidth(200)
```

#### 🎯 Click Management
```kotlin
view.onClick { ... }
view.onSafeClick(1.5) { ... }
view.onFirstClick { ... }         // One-time only click
view.onDoubleClick { ... }
```

#### 🔁 Redirects & Navigation Bindings
```kotlin
button.redirect(MyActivity::class)
link.redirect("https://prexoft.com")
```

#### ⌨️ Input Focus Management
```kotlin
editText.focus()
editText.distract()
```

---

### 🧭 Navigation System
```kotlin
goTo(MyActivity::class)
goTo("tel:1234567890")
goTo("mailto:support@prexoft.com")
goTo(myIntent)
goTo(1234567890L)
goTo(uri)
```

---

### 📷 Media Sharing
```kotlin
share("Text to share")
share(bitmap)          // Android Q+
copy("Copy this text")
```

---

### 🧮 Dimension & Delay Utilities
```kotlin
val px = dpToPx(16)
val dp = pxToDp(64)

after(1.5, loop = 3) {
    // Delayed execution every 1.5 seconds, 3 times
}
```

---

### 📢 Dialog & Input Prompts
```kotlin
alert("Info", "Message", "OK") { acknowledged -> ... }
input("Name", "Enter your name") { name -> ... }
```

---

### 🗓️ Time Format Helpers
```kotlin
val now = System.currentTimeMillis()
now.formatAsTime()
now.formatAsDate()
now.formatAsDateAndTime()
```

---

### 📡 Network Monitoring *(Requires ACCESS_NETWORK_STATE)*
```kotlin
observeNetworkStatus { isConnected -> ... }
if (isNetworkAvailable()) { ... }
```

---

### 📃 RecyclerView Quick Adapter
```kotlin
recyclerView.adapter(R.layout.item_layout, list) { pos, view, item ->
    view.view<TextView>(R.id.title).text = item.title
}

adapterWrapper.updateItems(newItems)
```

---

### 📜 Scroll Detection
```kotlin
scrollView.onScroll(
    onTop = { /* reached top */ },
    onBottom = { /* reached bottom */ },
    percentCallback = { percent -> ... }
)
```

---

### 🧰 Miscellaneous Tools
```kotlin
intent.start()
file.read()
"abc".append("def")
listOf(tv1, tv2).setText(listOf("A", "B"))
```

---

## 📄 License

```text
Apache License 2.0
Copyright (c) 2025 Prexoft
```

---

Made with ❤️ by [Prexoft](https://github.com/binarybeam)
