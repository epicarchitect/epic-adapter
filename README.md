# BindingRecyclerViewAdapter

### Add the JitPack repository to your build file 
Add it in your root build.gradle at the end of repositories:
```Kotlin
allprojects {
    repositories {
        maven("https://jitpack.io")
    }
}
```

### Add the dependency
```Kotlin
dependencies {
    implementation("com.github.alexander-kolmachikhin:BindingRecyclerViewAdapter:1.0.3")
}
```
### Usage
```Kotlin
recyclerView.adapter = BindingRecyclerViewAdapter {
    setup<Item, ItemBinding>(ItemBinding::inflate) {
        bind { item ->
            textView.text = item.text
        }
    }
}

recyclerView.requireBindingRecyclerViewAdapter().loadItems(items)
```
