package com.ru.ruchurch.utils

object ParseURL {
    fun getDomain(url: String) = url.substringAfterLast('.')

    fun testDomain(url: String, domain: String): Boolean {
        val d = getDomain(url)
        val res = d.startsWith(domain)
        return res
    }

    fun getChannel(url: String) = url.substringBeforeLast('.').replace("http://","")
}