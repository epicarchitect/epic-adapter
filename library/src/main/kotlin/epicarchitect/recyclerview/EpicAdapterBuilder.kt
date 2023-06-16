@file:Suppress("UNCHECKED_CAST")

package epicarchitect.recyclerview

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

fun RecyclerView.requireEpicAdapter() = adapter as EpicAdapter

fun ViewPager2.requireEpicAdapter() = adapter as EpicAdapter

fun LifecycleOwner.EpicAdapter(
    setup: EpicAdapterBuilder.() -> Unit
) = EpicAdapter(lifecycleScope, setup)

fun EpicAdapter(
    coroutineScope: CoroutineScope,
    setup: EpicAdapterBuilder.() -> Unit
) = EpicAdapterBuilder(coroutineScope).apply(setup).build()

class EpicAdapterBuilder(val coroutineScope: CoroutineScope) {

    val adapter = EpicAdapter()

    inline fun <reified ITEM : Any, VB : ViewBinding> setup(
        noinline bindingFactory: (LayoutInflater, ViewGroup, Boolean) -> VB,
        setup: ItemProviderBuilder<ITEM, VB>.() -> Unit = {}
    ) = adapter.addItemConfig(
        ITEM::class,
        ItemProviderBuilder<ITEM, VB>(coroutineScope, bindingFactory).apply(setup).build()
    )

    fun build() = adapter
}

class ItemProviderBuilder<ITEM : Any, VB : ViewBinding>(
    private val coroutineScope: CoroutineScope,
    private val bindingFactory: (LayoutInflater, ViewGroup, Boolean) -> VB
) {
    private var initFunction: VB.(Lazy<ITEM>) -> Unit = {}
    private var bindFunction: suspend VB.(CoroutineScope, EpicAdapter.BindingHolder, payloads: List<Any>, ITEM) -> Unit =
        { _, _, _, _ -> }
    private val diffUtilItemCallbackBuilder = DiffUtilItemCallbackBuilder<ITEM>()

    fun init(function: VB.(Lazy<ITEM>) -> Unit) {
        initFunction = function
    }

    fun bind(function: suspend VB.(CoroutineScope, EpicAdapter.BindingHolder, payloads: List<Any>, ITEM) -> Unit) {
        bindFunction = function
    }

    fun diffUtil(setup: DiffUtilItemCallbackBuilder<ITEM>.() -> Unit) {
        diffUtilItemCallbackBuilder.setup()
    }

    fun build() = object : EpicAdapter.ItemConfig(
        bindingHolderFactory = { layoutInflater, container, attachToToot ->
            object : EpicAdapter.BindingHolder(
                bindingFactory(layoutInflater, container, attachToToot)
            ) {
                private var bindJob: kotlinx.coroutines.Job? = null
                private var item: Any? = null

                init {
                    (binding as VB).initFunction(
                        object : Lazy<ITEM> {
                            override val value: ITEM
                                get() = item as ITEM

                            override fun isInitialized() = item != null
                        }
                    )
                }

                override fun bind(item: Any, payloads: List<Any>) {
                    this.item = item
                    val holder = this
                    bindJob = coroutineScope.launch {
                        (binding as VB).bindFunction(
                            this,
                            holder,
                            payloads,
                            item as ITEM
                        )
                    }
                }

                override fun recycle() {
                    bindJob?.cancel()
                    bindJob = null
                    item = null
                }
            }
        },
        diffUtilItemCallback = diffUtilItemCallbackBuilder.build()
    ) {}
}

class DiffUtilItemCallbackBuilder<ITEM : Any> {
    private var areItemsTheSameFunction: (oldItem: Any, newItem: Any) -> Boolean =
        { oldItem, newItem -> oldItem == newItem }
    private var areContentsTheSameFunction: (oldItem: Any, newItem: Any) -> Boolean =
        { oldItem, newItem -> oldItem == newItem }
    private var payloadFunction: (oldItem: Any, newItem: Any) -> Any? =
        { oldItem, newItem -> null }

    fun areItemsTheSame(function: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        areItemsTheSameFunction = function as (Any, Any) -> Boolean
    }

    fun areContentsTheSame(function: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        areContentsTheSameFunction = function as (Any, Any) -> Boolean
    }

    fun payload(function: (oldItem: ITEM, newItem: ITEM) -> Any?) {
        payloadFunction = function as (Any, Any) -> Any?
    }

    fun build() = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any) =
            oldItem::class == newItem::class && areItemsTheSameFunction(oldItem, newItem)

        override fun areContentsTheSame(oldItem: Any, newItem: Any) =
            oldItem::class == newItem::class && areContentsTheSameFunction(oldItem, newItem)

        override fun getChangePayload(oldItem: Any, newItem: Any) =
            payloadFunction(oldItem, newItem)
    }
}

fun <ITEM : Any, VB : ViewBinding> ItemProviderBuilder<ITEM, VB>.bind(
    function: suspend VB.(EpicAdapter.BindingHolder, payloads: List<Any>, ITEM) -> Unit
) = bind { _, bindingHolder, payloads, item ->
    function(bindingHolder, payloads, item)
}

fun <ITEM : Any, VB : ViewBinding> ItemProviderBuilder<ITEM, VB>.bind(
    function: suspend VB.(EpicAdapter.BindingHolder, ITEM) -> Unit
) = bind { _, bindingHolder, _, item ->
    function(bindingHolder, item)
}

fun <ITEM : Any, VB : ViewBinding> ItemProviderBuilder<ITEM, VB>.bind(
    function: suspend VB.(ITEM) -> Unit
) = bind { _, _, _, item ->
    function(item)
}
