package com.example.guessword.data.repository

import com.example.guessword.data.model.Word

class WordRepository {
    private val words = listOf(
        Word("forest", listOf('f', 'o', 'r', 'e', 's', 't')),
        Word("cloud", listOf('c', 'l', 'o', 'u', 'd'))
    )

    // Получение рандомного слова
    fun getRandomWord(): Word = words.random()
}