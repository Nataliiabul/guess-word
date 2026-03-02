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
    private var draggedLetter : Char? = null

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

    fun onLetterDragStart(letter: Char){
        draggedLetter = letter
    }

    fun onLetterDrop(index: Int){
        if (draggedLetter == null) return
        val currentState = _uiState.value
        val newUserSlots = currentState.userSlots.toMutableList()

        if (index !in newUserSlots.indices) return

        if (currentState.userSlots[index] != null){
            draggedLetter = null
            return
        } else {
            newUserSlots[index] = draggedLetter
        }

        var guessed = newUserSlots == currentState.currentWord.letters
        _uiState.value = currentState.copy(
            userSlots = newUserSlots,
            isWordGuessed = guessed
        )
        draggedLetter = null
    }
}