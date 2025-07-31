# 📦 Prexocore

**Prexocore** isn’t just another utility library — it’s a collection of high-performance, developer-first Kotlin tools engineered to ease Android development to the next level. It dramatically reduces boilerplate while offering safe, expressive, and delightful APIs across UI, navigation, feedback, and system utilities.

Imagine handling permissions, dialogs, inputs, clicks, navigation, image loading, toasts, or notifications - all with one-liners and no XML. **Prexocore** empowers you with elegant APIs that just work, with sane defaults and seamless context awareness.

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
toast("Simple Message")        
safeToast("Debounced Message")  // One toast at a time
vibrate()                       // Tactile feedback
```

#### 🎯 Snackbar with Action
```kotlin
snack("Message", "Retry") { clicked ->  }
```

#### 🛎️ Notifications (Android N+)
```kotlin
postNotification(
    title = "Hello",
    content = "This is a notification"

    // More optional customisations available 
)
```

---

### 🕹️ UI & Input Enhancements

#### 🎈 View Utilities
```kotlin
view.show()
view.hide()
view.fadeIn(300)
view.setHeight(dp = 120)
view.setWidth(dp = 200)
```

#### 🎯 Click Management
```kotlin
view.onClick { ... }
view.onSafeClick(1.5) { ... }     // Avoid multiple clicks frequently
view.onFirstClick { ... }         // One-time only click
view.onDoubleClick { ... }
```

#### 🔁 Redirects & Navigation Bindings
```kotlin
button.redirect(MyActivity::class)
link.redirect("https://prexoft.com")
mail.redirect("founder@prexoft.com")
```

#### ⌨️ Input Focus Management
```kotlin
editText.focus()             // Auto handles keyboard visibility
editText.distract()
```

---

### 🧭 Navigation System
```kotlin
goTo(MyActivity::class)
goTo("1234567890")
goTo("mailto:support@prexoft.com")
goTo(myIntent)
goTo("github.com/binarybeam")
```

---

### 📷 Media Sharing
```kotlin
"Some String".share()
share(bitmap)
"Infortant Info".copy()
```

---

### 🧮 Delay Utilities
```kotlin
after(seconds = 1.5, loop = 3) {
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

### 📡 Network Monitoring, Requires `ACCESS_NETWORK_STATE` Permission
```kotlin
observeNetworkStatus { isConnected -> ... }

// Or get one time status
if (isNetworkAvailable()) { ... }
```

---

### 📃 RecyclerView Quick Adapter
```kotlin
val adapter = recyclerView.adapter(R.layout.item_layout, list) { position, view, item ->
    // handle list rows
}

adapter.updateItems(newItems)
```

---

### 📜 Scroll Detection
```kotlin
scrollView.onScroll(
    onTop = { /* reached top */ },
    onBottom = { /* reached bottom */ },
    percentCallback = { percent -> /* Scrolled $percent % */ }
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
