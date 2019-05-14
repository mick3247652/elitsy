package com.ru.ruchurch.models

import android.arch.lifecycle.ViewModel
import com.prof.rssparser.Parser
import com.ru.ruchurch.adapters.ArticlesListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ArticleViewModel: ViewModel(){
    var url = "http://ruchurch.club/blog/feed"
    private val viewModelJob = Job()
    private val mainScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val articleNews = mutableListOf<ArticleNews>()

    val articleAdapter = ArticlesListAdapter()

    init {
        //reloadNews()
    }

    fun reloadNews(){
        articleNews.clear()
        mainScope.launch {
            val parser = Parser()
            val articleList = parser.getArticles(url)
            for(article in articleList) articleNews.add(ArticleNews(article))
            articleAdapter.setItems(articleNews)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}