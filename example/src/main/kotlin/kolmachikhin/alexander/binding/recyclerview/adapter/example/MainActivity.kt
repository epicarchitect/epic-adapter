package kolmachikhin.alexander.binding.recyclerview.adapter.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import kolmachikhin.alexander.binding.recyclerview.adapter.BindingRecyclerViewAdapter
import kolmachikhin.alexander.binding.recyclerview.adapter.example.databinding.ItemBinding
import kolmachikhin.alexander.binding.recyclerview.adapter.requireBindingRecyclerViewAdapter

class MainActivity : AppCompatActivity() {

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        recyclerView.adapter = BindingRecyclerViewAdapter {
            setup<Item, ItemBinding>(ItemBinding::inflate) {
                bind { item ->
                    textView.text = item.text
                }
            }
        }

        recyclerView.requireBindingRecyclerViewAdapter().loadItems(items)
    }
}

data class Item(
    val text: String
)

val items = buildList {
    repeat(100) {
        add(Item("Item $it"))
    }
}