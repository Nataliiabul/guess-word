package com.example.guessword.ui.game

import com.example.guessword.data.model.Word

data class GameUIState(
    val currentWord: Word,                  // текущее слово
    val shuffledLetters: List<Char?>,       // перемещанные буквы
    val userSlots: List<Char?>,             // символы в слотах пользователя
    val isWordGuessed: Boolean,             // отгадано ли слово
)
