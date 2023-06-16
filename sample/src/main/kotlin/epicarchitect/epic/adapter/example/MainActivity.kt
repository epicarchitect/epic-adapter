package epicarchitect.epic.adapter.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import epicarchitect.epic.adapter.example.databinding.CartItemBinding
import epicarchitect.epic.adapter.example.databinding.Item1Binding
import epicarchitect.epic.adapter.example.databinding.Item2Binding
import epicarchitect.recyclerview.EpicAdapter
import epicarchitect.recyclerview.bind
import epicarchitect.recyclerview.requireEpicAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }

    private val listState = MutableStateFlow(fakeItems)

    private fun addAmount(item: CartItem, add: Int) {
        listState.value = listState.value.map {
            if (it == item) item.copy(amount = item.amount + add)
            else it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        recyclerView.adapter = EpicAdapter {
            setup<Item1, Item1Binding>(Item1Binding::inflate) {
                bind { item ->
                    textView1.text = item.text
                }
            }

            setup<SealedItem, Item2Binding>(Item2Binding::inflate) {
                bind { item ->
                    when (item) {
                        is SealedItem.Closed -> {
                            textView2.text = "Closed"
                        }

                        is SealedItem.Selected -> {
                            textView2.text = "Selected"
                        }
                    }
                }
            }

            setup<CartItem, CartItemBinding>(CartItemBinding::inflate) {
                init { item ->
                    buttonAdd.setOnClickListener {
                        addAmount(item.value, 1)
                    }

                    buttonRemove.setOnClickListener {
                        addAmount(item.value, -1)
                    }
                }
                bind { _, payloads, item ->
                    textViewName.text = item.name
                    textViewAmount.text = item.amount.toString()
                    textViewAmount.rotation = item.amount.absoluteValue % 36f
                    if (payloads.contains(UpdateAmount)) {
                        textViewAmount.animate().rotation(item.amount.absoluteValue % 36f).start()
                    }
                }
                diffUtil {
                    areItemsTheSame { oldItem, newItem -> oldItem.id == newItem.id }
                    payload { oldItem, newItem ->
                        if (oldItem.amount != newItem.amount) {
                            UpdateAmount
                        } else {
                            null
                        }
                    }
                }
            }
        }

        listState.onEach {
            recyclerView.requireEpicAdapter().loadItems(it)
        }.launchIn(lifecycleScope)
    }
}

object UpdateAmount

data class CartItem(
    val id: Int,
    val name: String,
    val amount: Int
)

sealed class SealedItem {
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
    repeat(30) {
        add(
            CartItem(
                id = it,
                name = "Item $it",
                amount = 0
            )
        )
    }

    repeat(30) {
        add(
            if (true) Item1(it, "Item1 $it")
            else Item2(it, "Item2 $it")
        )
    }

    repeat(30) {
        add(
            if (it % 2 == 0) SealedItem.Closed()
            else SealedItem.Selected()
        )
    }
}.shuffled()