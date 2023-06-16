# Epic Adapter

This is an android library for RecyclerView adapters with ViewBinding.
With it, you can quickly and easily set up RecyclerView, no more huge adapters!

```Kotlin
// add the JitPack repository in settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}

// add the dependency in your build.gradle.kts
dependencies {
    implementation("com.github.epicarchitect:epic-adapter:1.0.9")
}
```

### Simple usage (Similarly used with ViewPager2)

```Kotlin
data class MyItem(
    val id: Int,
    val text: String
)

recyclerView.adapter = EpicAdapter {
    setup<MyItem, MyItemBinding>(MyItemBinding::inflate) {
        // optional
        init { item: Lazy<MyItem> ->
            textView.clipToOutline = true // for example
            // add click listeners in init
            buttonAdd.setOnClickListener {
                viewModel.addItem(item.value) // for example
            }
        }

        // there are 4 overloaded methods "bind" here
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
val items: List<Any> = listOf(MyItem(1, "item1"), MyItem(2, "item2"))
recyclerView.requireEpicAdapter().loadItems(items)
```

### DiffUtil usage

```Kotlin
recyclerView.adapter = EpicAdapter {
    setup<Item, ItemBinding>(ItemBinding::inflate) {
        bind { item ->
            textView.text = item.text
        }

        // diffutils uses the equals function by default, but you can override it:
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

val items: ArrayList<Any> = ArrayList()
items.add(Item1(text = "viewType1"))
items.add(Item2(text = "viewType2"))

recyclerView.requireEpicAdapter().loadItems(items)
```



