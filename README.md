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
    implementation("com.github.epicarchitect:epic-adapter:1.0.8")
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
recyclerView.adapter = EpicAdapter {
    setup<Item, ItemBinding>(ItemBinding::inflate) {
        // Optional
        init { item: Lazy<Item> ->
            textView.clipToOutline = true
            // add click listeners in init
            buttonAdd.setOnClickListener {
                viewModel.check(item.value)
            }
        }

        // There are three overloaded methods "bind" here
        bind { item ->
            textView.text = item.text
        }
        
        bind { coroutineScope, item -> 
            textView.text = item.text
        }

        bind { coroutineScope, holder, item ->
            textView.text = holder.bindingAdapterPosition.toString()
        }
     
    }

}

// load the data
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



