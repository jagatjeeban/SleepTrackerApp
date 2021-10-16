package com.example.android.trackmysleepquality.sleeptracker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The RecyclerView will need to distinguish each item's view type,
 * so that it can correctly assign a view holder to it.
 */
private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1
private val adapterScope = CoroutineScope(Dispatchers.Default)

class SleepNightAdapter(private val clickListener:SleepNightListener):
    androidx.recyclerview.widget.ListAdapter<DataItem, RecyclerView.ViewHolder>(SleepNightDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){          //return the appropriate view holder for each item type
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    /**
     * Instead of using submitList(), provided by the ListAdapter,
     * to submit the list, we will use this function to add a header and then submit the list.
     */
    fun addHeaderAndSubmitList(list: List<SleepNight>?){
        adapterScope.launch {
            val items = when(list){
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it) }
            }
            withContext(Dispatchers.Main){
                submitList(items)
            }
        }
    }

    // Display the data for one list item at the specified position.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){      //Add a condition to only assign data to the view holder if the holder is a ViewHolder.
            is ViewHolder ->{

                //Cast the object type returned by getItem() to DataItem.SleepNightItem
                val nightItem = getItem(position) as DataItem.SleepNightItem
                holder.bind(nightItem.sleepNight, clickListener)
            }
        }
    }

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header, parent, false)

                return TextViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM

        }
    }

    class ViewHolder private constructor(private val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SleepNight, clickListener: SleepNightListener) {

            binding.sleep = item
            binding.clickListener = clickListener
            binding.executePendingBindings() //asks data binding to execute any pending bindings right away.
        }

        /**
         * The from() function needs to be in a companion object so it can be called on the ViewHolder class,
         * not called on a ViewHolder instance.
         */
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSleepNightBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

  class SleepNightDiffCallback : DiffUtil.ItemCallback<DataItem>() {

    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

      @SuppressLint("DiffUtilsEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}
  class SleepNightListener(val clickListener: (sleepId:Long) -> Unit){
    fun onClick(night: SleepNight) = clickListener(night.nightId)
}

/**
 * A sealed class defines a closed type, which means that all subclasses of DataItem must be defined in this file.
 * As a result, the number of subclasses is known to the compiler.
 * It's not possible for another part of your code to define a new type of DataItem that could break your adapter.
 */
  sealed class DataItem{

    abstract val id:Long
      data class SleepNightItem(val sleepNight:SleepNight): DataItem() {
          override val id: Long = sleepNight.nightId
      }

    //Header has no actual data. So, we can declare it as an object.
    // (There will only ever be one instance of Header)
    object Header:DataItem() {
        override val id: Long = Long.MIN_VALUE //A constant holding the minimum value a long can have, -2^63.
    }

}

