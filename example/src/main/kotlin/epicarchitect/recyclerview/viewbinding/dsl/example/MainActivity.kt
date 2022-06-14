package epicarchitect.recyclerview.viewbinding.dsl.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import epicarchitect.recyclerview.viewbinding.dsl.BindingRecyclerViewAdapter
import epicarchitect.recyclerview.viewbinding.dsl.example.databinding.Item1Binding
import epicarchitect.recyclerview.viewbinding.dsl.example.databinding.Item2Binding
import epicarchitect.recyclerview.viewbinding.dsl.example.databinding.SealedItemBinding
import epicarchitect.recyclerview.viewbinding.dsl.requireBindingRecyclerViewAdapter

class MainActivity : AppCompatActivity() {

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        recyclerView.adapter = BindingRecyclerViewAdapter {
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

            setup<SealedItem, SealedItemBinding>(SealedItemBinding::inflate) {
                bind { item ->
                    when (item) {
                        is SealedItem.Closed -> {
                            textView.text = "Closed"
                        }
                        is SealedItem.Selected -> {
                            textView.text = "Selected"
                        }
                    }
                }
            }
        }

        recyclerView.requireBindingRecyclerViewAdapter().loadItems(fakeItems)
    }
}

sealed class SealedItem{
    class Selected : SealedItem()
    class Closed : SealedItem()
}

data class Item1(
    val id: Int,
    val text: String
)

data class Item2(
    val id: Int,
    val text: String
)

val fakeItems = buildList {
    repeat(10) {
        add(
            if (it % 2 == 0) Item1(it, "Item1 $it")
            else Item2(it, "Item2 $it")
        )
    }

    repeat(10) {
        add(
            if (it % 2 == 0) SealedItem.Closed()
            else SealedItem.Selected()
        )
    }
}