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

    // Загрузка нового слова
    fun loadNewWord() {
        val word = repository.getRandomWord()
        _uiState.value = GameUIState(
            word,
            word.letters.shuffled(),
            List(word.letters.size){null},
            false
        )
    }

    // Перетаскиваемая буква
    fun onLetterDragStart(letter: Char){
        draggedLetter = letter
    }

    // Сброс перетаскиваемой буквы в указанный слот
    fun onLetterDrop(index: Int) {
        if (draggedLetter == null) return
        val currentState = _uiState.value

        if (index !in currentState.userSlots.indices) return

        // Не занят ли уже этот слот другой буквой
        if (currentState.userSlots[index] != null) {
            draggedLetter = null
            return
        }

        val sourceIndex = currentState.shuffledLetters.indexOfFirst { it == draggedLetter }
        if (sourceIndex == -1) {
            draggedLetter = null
            return
        }

        // Удаляем с верхнего ряда перетаскиваемую букву
        val newShuffled = currentState.shuffledLetters.toMutableList()
        newShuffled[sourceIndex] = null

        // Добавляем в нижний ряд букву
        val newUserSlots = currentState.userSlots.toMutableList()
        newUserSlots[index] = draggedLetter

        // Проверяем, не собрано ли всё слово правильно
        val guessed = newUserSlots == currentState.currentWord.letters

        _uiState.value = currentState.copy(
            shuffledLetters = newShuffled,
            userSlots = newUserSlots,
            isWordGuessed = guessed
        )
        draggedLetter = null
    }

    // Сброс состояния игры до начального
    fun resetGame() {
        val currentState = _uiState.value
        val word = currentState.currentWord

        _uiState.value = currentState.copy(
            shuffledLetters = word.letters.shuffled(),
            userSlots = List(word.letters.size) { null },
            isWordGuessed = false
        )
        draggedLetter = null
    }
}