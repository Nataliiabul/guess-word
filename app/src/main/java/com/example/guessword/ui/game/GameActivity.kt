package com.example.guessword.ui.game

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.guessword.R
import com.example.guessword.data.repository.WordRepository
import com.example.guessword.databinding.ActivityGameBinding
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this, object: ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass : Class<T>): T{
                    return GameViewModel(WordRepository()) as T
                }
            }
        ).get(GameViewModel::class.java)

        lifecycleScope.launch{
            viewModel.uiState.collect {
                    state -> updateUI(state)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateUI(state: GameUIState){
        println("Слово: ${state.currentWord.text}")
        println("Перемешанные буквы: ${state.shuffledLetters}")
        println("Слоты пользователя: ${state.userSlots}")
        println("Победа: ${state.isWordGuessed}")
        println("---")
    }

}