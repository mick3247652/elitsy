package com.ru.ruchurch.adapters

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup

abstract class CategoryAdapter<T : Any, V : ViewDataBinding>(val layout: Int) :
        RecyclerView.Adapter<CategoryAdapter.CategoryHolder<V>>() {
    private var items = mutableListOf<T>()
    var onItemClick: (Int) -> Unit = { v -> Log.v("NOT BINDING", "v = $v") }

    fun setItems(list: List<T>) {
        items = list.toMutableList()
        notifyDataSetChanged()
        Log.w("ADAPTER", "setItems list size = ${list.size}")
    }

    fun getItems() = items

    fun removeItem(i: Int) {
        items.removeAt(i)
    }

    open fun getItem(position: Int) = items[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder<V> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<V>(inflater, layout, parent, false)
        return CategoryHolder<V>(binding)
    }

    override fun getItemCount() = items.size

    class CategoryHolder<V : ViewDataBinding>(val binding: V) : RecyclerView.ViewHolder(binding.root)
}