package com.example.guessword.ui.game

import androidx.lifecycle.ViewModel
import com.example.guessword.data.model.Word
import com.example.guessword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(private val repository: WordRepository): ViewModel() {
    private val _uiState = MutableStateFlow(GameUIState(
        Word("", emptyList<Char>()),
        emptyList<Char>(),
        emptyList<Char?>(),
        false
    ))

    val uiState: StateFlow<GameUIState> = _uiState.asStateFlow()

    init {
        loadNewWord()
    }

    fun loadNewWord() {
        val word = repository.getRandomWord()
        _uiState.value = GameUIState(
            word,
            word.letters.shuffled(),
            List(word.letters.size){null},
            false
        )
    }
}