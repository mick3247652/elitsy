package com.ru.astron.adapters

import com.ru.astron.R
import com.ru.astron.databinding.CardArticleBinding
import com.ru.astron.models.ArticleNews

class ArticlesListAdapter : CategoryAdapter<ArticleNews, CardArticleBinding>(
        R.layout.card_article
) {
    override fun onBindViewHolder(holder: CategoryHolder<CardArticleBinding>, position: Int) =
            with(holder.binding) {
                holder.itemView.setOnClickListener { onItemClick(position) }
                article = getItem(position)
                executePendingBindings()
            }
}