@file:Suppress("UNCHECKED_CAST")

package kolmachikhin.alexander.binding.recyclerview.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun RecyclerView.requireBindingRecyclerViewAdapter() = adapter as BindingRecyclerViewAdapter
fun ViewPager2.requireBindingRecyclerViewAdapter() = adapter as BindingRecyclerViewAdapter

fun LifecycleOwner.BindingRecyclerViewAdapter(
    setup: BindingRecyclerViewAdapterBuilder.() -> Unit
) = BindingRecyclerViewAdapter(lifecycleScope, setup)

fun BindingRecyclerViewAdapter(
    coroutineScope: CoroutineScope,
    setup: BindingRecyclerViewAdapterBuilder.() -> Unit
) = BindingRecyclerViewAdapterBuilder(coroutineScope).apply(setup).build()

class BindingRecyclerViewAdapterBuilder(val coroutineScope: CoroutineScope) {

    val adapter = BindingRecyclerViewAdapter()

    inline fun <reified ITEM : Any, VIEW_BINDING : ViewBinding> setup(
        noinline bindingFactory: (LayoutInflater, ViewGroup, Boolean) -> VIEW_BINDING,
        setup: ItemProviderBuilder<ITEM, VIEW_BINDING>.() -> Unit = {}
    ) = adapter.addItemConfig(
        ITEM::class,
        ItemProviderBuilder<ITEM, VIEW_BINDING>(coroutineScope, bindingFactory).apply(setup).build()
    )

    fun build() = adapter
}

class ItemProviderBuilder<ITEM : Any, VIEW_BINDING : ViewBinding>(
    private val coroutineScope: CoroutineScope,
    private val bindingFactory: (LayoutInflater, ViewGroup, Boolean) -> VIEW_BINDING
) {
    private var initFunction: VIEW_BINDING.() -> Unit = {}
    private var bindFunction: suspend VIEW_BINDING.(CoroutineScope, ITEM, holder: BindingRecyclerViewAdapter.BindingHolder) -> Unit = { _, _, _ -> }
    private val diffUtilItemCallbackBuilder = DiffUtilItemCallbackBuilder<ITEM>()

    fun init(function: VIEW_BINDING.() -> Unit) {
        initFunction = function
    }

    fun bind(function: suspend VIEW_BINDING.(ITEM) -> Unit) {
        bindFunction = { _, item, _ -> function(item) }
    }

    fun bind(function: suspend VIEW_BINDING.(CoroutineScope, ITEM) -> Unit) {
        bindFunction = { scope, item, _ -> function(scope, item) }
    }

    fun bind(function: suspend VIEW_BINDING.(CoroutineScope, ITEM, BindingRecyclerViewAdapter.BindingHolder) -> Unit) {
        bindFunction = function
    }

    fun diffUtil(setup: DiffUtilItemCallbackBuilder<ITEM>.() -> Unit) {
        diffUtilItemCallbackBuilder.setup()
    }

    fun build() = object : BindingRecyclerViewAdapter.ItemConfig(
        bindingHolderFactory = { layoutInflater, container, attachToToot ->
            object : BindingRecyclerViewAdapter.BindingHolder(bindingFactory(layoutInflater, container, attachToToot)) {
                private var bindJob: kotlinx.coroutines.Job? = null

                init {
                    (binding as VIEW_BINDING).initFunction()
                }

                override fun bind(item: Any) {
                    val holder = this
                    bindJob = coroutineScope.launch {
                        (binding as VIEW_BINDING).bindFunction(this, item as ITEM, holder)
                    }
                }

                override fun recycle() {
                    bindJob?.cancel()
                    bindJob = null
                }
            }
        },
        diffUtilItemCallback = diffUtilItemCallbackBuilder.build()
    ) {}
}

class DiffUtilItemCallbackBuilder<ITEM : Any> {
    private var areItemsTheSameFunction: (oldItem: Any, newItem: Any) -> Boolean = { oldItem, newItem -> oldItem == newItem }
    private var areContentsTheSameFunction: (oldItem: Any, newItem: Any) -> Boolean = { oldItem, newItem -> oldItem == newItem }

    fun areItemsTheSame(function: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        areItemsTheSameFunction = function as (Any, Any) -> Boolean
    }

    fun areContentsTheSame(function: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        areContentsTheSameFunction = function as (Any, Any) -> Boolean
    }

    fun build() = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any) =
            oldItem::class == newItem::class && areItemsTheSameFunction(oldItem, newItem)

        override fun areContentsTheSame(oldItem: Any, newItem: Any) =
            oldItem::class == newItem::class && areContentsTheSameFunction(oldItem, newItem)
    }
}

