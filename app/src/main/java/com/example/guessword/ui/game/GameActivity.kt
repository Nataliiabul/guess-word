package com.example.guessword.ui.game

import android.content.ClipData
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
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

        // Подписка на изменения состояния игры
        lifecycleScope.launch{
            viewModel.uiState.collect {
                    state -> updateUI(state)
            }
        }

        // Кнопка сброса текущего состояния до начального
        binding.btnReset.setOnClickListener {
            viewModel.resetGame()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Обновление интерфейса на основе текущего состояния игры
    private fun updateUI(state: GameUIState) {
        val size = resources.getDimensionPixelSize(R.dimen.tile_size)
        val allSlotsFilled = state.userSlots.all { it != null }
        val gameFinished = state.isWordGuessed || (allSlotsFilled && !state.isWordGuessed)

        binding.shuffledLetters.removeAllViews()
        binding.userLetters.removeAllViews()

        // Отрисовка верхнего ряда (перемешанные буквы)
        for (slotValue in state.shuffledLetters) {
            val tile = if (slotValue != null) {
                TextView(this, null, 0, R.style.TileLetter)
            } else {
                TextView(this, null, 0, R.style.TileEmpty)
            }
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(8, 0, 8, 0)
            tile.layoutParams = params

            if (slotValue != null) {
                tile.text = slotValue.toString()
                if (!gameFinished) {
                    tile.setOnLongClickListener { view ->
                        view.alpha = 0.4f
                        viewModel.onLetterDragStart(slotValue)
                        val data = ClipData.newPlainText("letter", slotValue.toString())
                        view.startDragAndDrop(data, View.DragShadowBuilder(view), null, 0)
                        true
                    }
                    tile.setOnDragListener { view, event ->
                        if (event.action == DragEvent.ACTION_DRAG_ENDED) {
                            view.alpha = 1.0f
                        }
                        true
                    }
                }
            }
            binding.shuffledLetters.addView(tile)
        }

        // Отрисовка нижнего ряда (слоты пользователя)
        for (slotValue in state.userSlots) {
            // Выбор стиля в зависимости от исхода игры
            val slot = when {
                // угадано
                state.isWordGuessed -> TextView(this, null, 0, R.style.TileCorrect)
                // ошибка
                allSlotsFilled && !state.isWordGuessed -> TextView(this, null, 0, R.style.TileWrong)
                else -> if (slotValue != null) { // заполненный слот
                    TextView(this, null, 0, R.style.TileLetter)
                } else { // пустой слот
                    TextView(this, null, 0, R.style.TileEmpty)
                }
            }
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(8, 0, 8, 0)
            slot.layoutParams = params

            if (slotValue != null) {
                slot.text = slotValue.toString()
            }

            if (!gameFinished) {
                slot.setOnDragListener { view, event ->
                    when (event.action) {
                        // Разрешаем начало перетаскивания только если слот пустой
                        DragEvent.ACTION_DRAG_STARTED -> {
                            (view as? TextView)?.text?.isEmpty() ?: true
                        }
                        // При наведении на слот меняем фон
                        DragEvent.ACTION_DRAG_ENTERED -> {
                            view.setBackgroundResource(R.drawable.btn_highlighted)
                            true
                        }
                        // При уходе сбрасываем фон
                        DragEvent.ACTION_DRAG_EXITED -> {
                            view.setBackgroundResource(R.drawable.empty_button_bg)
                            true
                        }
                        // Сброс буквы в слот
                        DragEvent.ACTION_DROP -> {
                            val item = event.clipData.getItemAt(0)
                            val letter = item.text?.singleOrNull()
                            if (letter != null) {
                                val index = binding.userLetters.indexOfChild(view)
                                viewModel.onLetterDrop(index)
                            }
                            view.setBackgroundResource(R.drawable.empty_button_bg)
                            true
                        }
                        // Сброс фона после окончания перетаскивания
                        DragEvent.ACTION_DRAG_ENDED -> {
                            view.setBackgroundResource(R.drawable.empty_button_bg)
                            true
                        }
                        else -> true
                    }
                }
            }
            binding.userLetters.addView(slot)
        }
    }
}