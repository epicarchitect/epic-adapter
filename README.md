# Epic Adapter

### Add the JitPack repository to your root build file

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
    implementation("com.github.epicarchitect:epic-adapter:1.0.9")
}
```

### Preview

```
• Simple and convenient adapter
• Will allow you not to create a separate class
• Works with RecyclerView and ViewPager2
```

### What's included

```
• Adapter works completely on coroutines
• DiffUtil
• ViewTypes
```

### Simple usage (Similarly used with ViewPager2)

```Kotlin
data class MyItem(
    val id: Int,
    val text: String
)

recyclerView.adapter = EpicAdapter {
    setup<MyItem, MyItemBinding>(MyItemBinding::inflate) {
        // Optional
        init { item: Lazy<MyItem> ->
            textView.clipToOutline = true // for example
            // add click listeners in init
            buttonAdd.setOnClickListener {
                viewModel.addItem(item.value) // for example
            }
        }

        // There are 4 overloaded methods "bind" here
        bind { item: MyItem ->
            textView.text = item.text
        }
        // or with holder and item
        bind { holder: EpicAdapter.BindingHolder, item: MyItem ->
            textView.text = item.text
        }
        // or with holder, payloads and item
        bind { holder: EpicAdapter.BindingHolder, payloads: List<Any>, item: MyItem ->
            textView.text = item.text
        }
        // or with coroutineScope, holder, payloads and item
        bind { scope: CoroutineScope, holder: EpicAdapter.BindingHolder, payloads: List<Any>, item: MyItem ->
            textView.text = item.text
        }
    }
}

// load the data
val items: List<Any>
recyclerView.requireEpicAdapter().loadItems(items)
```

### DiffUtil usage

```Kotlin
// lifecycleScope is optional
recyclerView.adapter = EpicAdapter {
    setup<Item, ItemBinding>(ItemBinding::inflate) {

        bind { item ->
            textView.text = item.text
        }

        // Diffutils uses the equals function by default, but you can override it:
        diffUtil {
            areItemsTheSame { oldItem, newItem ->
                oldItem.id == newItem.id
            }
            areContentsTheSame { oldItem, newItem ->
                oldItem.text == newItem.text
            }
            payload { oldItem, newItem ->
                if (oldItem.amount != newItem.amount) {
                    UpdateAmount // for example
                } else {
                    null
                }
            }
        }
    }
}

recyclerView.requireEpicAdapter().loadItems(items)
```

### ViewTypes usage

```Kotlin
 recyclerView.adapter = EpicAdapter {
    // There are 2 setups for each viewType
    setup<Item1, Item1Binding>(Item1Binding::inflate) {
        bind { item ->
            textView1.text = item.text
        }
    }
    setup<Item2, Item2Binding>(Item2Binding::inflate) {
        bind { item ->
            textView2.text = item.text
        }
    }
}

// There is no familiar condition for determining the viewType
// Instead of the usual condition, for define a viewType we using hash codes of classes, as well as primitives
// This means that the contentList must be of type Any, which will contain different objects or primitives
// The advantage of this approach is that it has a better architecture and flexibility

val contentList: ArrayList<Any> = ArrayList()
contentList.add(Item1(text = "viewType1"))
contentList.add(Item2(text = "viewType2"))

recyclerView.requireEpicAdapter().loadItems(contentList)
```



