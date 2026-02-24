package com.example.guessword.ui.game

import com.example.guessword.data.model.Word

data class GameUIState(
    val currentWord: Word,
    val shuffledLetters: List<Char>,
    val userSlots: List<Char?>,
    val isWordGuessed: Boolean,
)
