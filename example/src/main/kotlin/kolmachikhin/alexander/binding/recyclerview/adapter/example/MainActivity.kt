package kolmachikhin.alexander.binding.recyclerview.adapter.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kolmachikhin.alexander.binding.recyclerview.adapter.BindingRecyclerViewAdapter
import kolmachikhin.alexander.binding.recyclerview.adapter.example.databinding.Item1Binding
import kolmachikhin.alexander.binding.recyclerview.adapter.requireBindingRecyclerViewAdapter

class MainActivity : AppCompatActivity() {

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Simple adapter
        BindingRecyclerViewAdapter {

        }
        recyclerView.adapter = BindingRecyclerViewAdapter(lifecycleScope) {
            setup<Item1, Item1Binding>(Item1Binding::inflate) {
                init {
                    textView1.clipToOutline = true
                }

                bind { item ->
                    textView1.text = item.text
                }

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

        // adapter with viewTypes
        /*recyclerView.adapter = buildBindingRecyclerViewAdapter(lifecycleScope) {
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
        }*/

        recyclerView.requireBindingRecyclerViewAdapter().loadItems(itemsForSimpleUsage)
    }
}

data class Item1(
    val id: Int,
    val text: String
)

data class Item2(
    val id: Int,
    val text: String
)

val itemsForSimpleUsage = buildList {
    repeat(100) {
        add(Item1(it, "Item $it"))
    }
}

val itemsForViewTypeUsage = buildList {
    repeat(100) {
        add(Item1(it, "Item $it"))
        add(Item2(it, "Item $it"))
    }
}