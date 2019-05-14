package com.ru.ruchurch.adapters

import com.ru.ruchurch.R
import com.ru.ruchurch.databinding.CardArticleBinding
import com.ru.ruchurch.models.ArticleNews

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