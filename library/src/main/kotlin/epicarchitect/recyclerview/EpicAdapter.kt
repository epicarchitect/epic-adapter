package epicarchitect.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

private typealias ViewType = Int

class EpicAdapter : RecyclerView.Adapter<EpicAdapter.BindingHolder>() {

    private val itemConfigs = mutableMapOf<ViewType, ItemConfig>()
    private val viewTypes = mutableMapOf<KClass<*>, ViewType>()
    private var items = emptyList<Any>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = getItemConfig(viewType).bindingHolderFactory(
        LayoutInflater.from(parent.context),
        parent,
        false
    )

    override fun onBindViewHolder(
        holder: BindingHolder,
        position: Int
    ) = holder.bind(items[position])

    override fun onViewRecycled(holder: BindingHolder) = holder.recycle()

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) =
        getItemViewType(items[position])


    private fun getItemViewType(item: Any) = viewTypes.getOrPut(item::class) { calculateItemViewType(item) }

    private fun calculateItemViewType(item: Any) =
        (listOf(item::class) + item::class.allSuperclasses)
            .firstOrNull { itemConfigs.containsKey(it.hashCode()) }
            ?.hashCode()
            ?: error("View type not found for ${item::class.simpleName}")

    private fun getItemConfig(viewType: Int) = checkNotNull(itemConfigs[viewType])

    private fun getItemConfig(item: Any) = getItemConfig(getItemViewType(item))

    fun addItemConfig(itemClass: KClass<*>, itemConfig: ItemConfig) {
        itemConfigs[itemClass.hashCode()] = itemConfig
    }

    fun loadItems(newItems: List<Any>) {
        val diffResult = DiffUtil.calculateDiff(DiffCallback(items, newItems))
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    abstract class ItemConfig(
        val bindingHolderFactory: (LayoutInflater, ViewGroup, Boolean) -> BindingHolder,
        val diffUtilItemCallback: DiffUtil.ItemCallback<Any>
    )

    abstract class BindingHolder(protected val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: Any)
        abstract fun recycle()
    }

    private inner class DiffCallback(
        private val oldItems: List<Any>,
        private val newItems: List<Any>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ) = getItemConfig(newItems[newItemPosition]).diffUtilItemCallback.areItemsTheSame(
            oldItems[oldItemPosition],
            newItems[newItemPosition]
        )

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ) = getItemConfig(newItems[newItemPosition]).diffUtilItemCallback.areContentsTheSame(
            oldItems[oldItemPosition],
            newItems[newItemPosition]
        )

        override fun getChangePayload(
            oldItemPosition: Int,
            newItemPosition: Int
        ) = getItemConfig(newItems[newItemPosition]).diffUtilItemCallback.getChangePayload(
            oldItems[oldItemPosition],
            newItems[newItemPosition]
        )

        override fun getOldListSize() = oldItems.size

        override fun getNewListSize() = newItems.size
    }
}