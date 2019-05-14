package com.ru.ruchurch.models

import com.prof.rssparser.Article
import com.ru.ruchurch.parser.DateParser
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

class ArticleNews(article: Article) {
    var title: String = ""
    var author: String = ""
    var content: String = ""
    var description: String = ""
    var link: String = ""
    var pubDate: String = ""
    var image = ""
    private val shortDateTimeFormat: DateTimeFormatter = DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault())
    init {
        title = if(article.title != null) article.title!! else ""
        author = if(article.author != null) article.author!! else ""
        content = if(article.content != null) article.content!! else ""
        description = if(article.description != null) article.description!! else ""
        link = if(article.link != null) article.link!! else ""
        pubDate = if(article.pubDate != null) article.pubDate!! else ""
        image = if(article.image != null) article.image!! else ""

        //val l = LocalDate.parse(pubDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        //val dateTime = ZonedDateTime.from(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).parse("Tuesday, August 23, 2016 1:12:45 PM EET"))
        if(!pubDate.isEmpty()) {
            val date = DateParser.parseDate(pubDate, Locale.ENGLISH)
            val pub = DateTime(date.time)/*.withZone(DateTimeZone.getDefault())*/.toString(shortDateTimeFormat)
            pubDate = pub
        }
    }
}