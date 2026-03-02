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
        val size = resources.getDimensionPixelSize(R.dimen.tile_size)

        binding.shuffledLetters.removeAllViews()
        binding.userLetters.removeAllViews()

        for (letter in state.shuffledLetters){
            val tile = TextView(this, null, 0, R.style.TileLetter)
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(8, 0, 8, 0)
            tile.layoutParams = params
            tile.text = letter.toString()

            tile.setOnLongClickListener { view ->
                val letter = tile.text.toString().single()
                viewModel.onLetterDragStart(letter)

                val data = ClipData.newPlainText("letter", letter.toString())
                view.startDragAndDrop(data, View.DragShadowBuilder(view), null, 0)
                true
            }
            binding.shuffledLetters.addView(tile)
        }

        for (slotValue in state.userSlots) {
            val slot = TextView(this, null, 0, R.style.TileEmpty)
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(8, 0, 8, 0)
            slot.layoutParams = params
            if (slotValue != null) {
                slot.text = slotValue.toString()
            }

            slot.setOnDragListener { view, event ->
                when (event.action){
                    DragEvent.ACTION_DRAG_STARTED -> true
                    DragEvent.ACTION_DROP -> {
                            val item = event.clipData.getItemAt(0)
                    val letter = item.text?.singleOrNull()
                    if (letter != null) {
                        val index = binding.userLetters.indexOfChild(view)
                        viewModel.onLetterDrop(index)
                    }
                    true
                    }
                    else -> true
                }
            }
            binding.userLetters.addView(slot)
        }
    }
}