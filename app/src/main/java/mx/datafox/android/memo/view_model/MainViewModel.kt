package mx.datafox.android.memo.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import mx.datafox.android.memo.R
import mx.datafox.android.memo.model.IconModel
import mx.datafox.android.memo.model.Level
import java.util.*

class MainViewModel : ViewModel() {

  var isReceiverRegistered: Boolean = false
  var isForegroundServiceRunning: Boolean = false
    set(value) {
      field = value
      updateGridVisibility.value = Unit
    }
  var isMusicServiceBound: Boolean = false

  var elapsedTime: Int = 0
  var lastOpenedCard: IconModel? = null
  var pairsSum: Int = 0
  var pairs: Int = 0
    set(value) {
      field = value

      // resetear última carta
      if (value == 0) lastOpenedCard = null
    }

  val closeCards = MutableLiveData<IconModel>()
  val pairMatch = MutableLiveData<IconModel>()
  val updateGridVisibility = MutableLiveData<Unit>()
  val showSuccessDialog = MutableLiveData<Unit>()

  private val iconList = listOf(
    IconModel(R.drawable.ic_tie_fighter),
    IconModel(R.drawable.ic_stormtropper),
    IconModel(R.drawable.ic_chew),
    IconModel(R.drawable.ic_combo),
    IconModel(R.drawable.ic_bb),
    IconModel(R.drawable.ic_rd),
    IconModel(R.drawable.ic_darth_maul),
  )

  fun getNumberOfColumns(level: Level): Int =
    when (level) {
      Level.BEGINNER, Level.INTERMEDIATE -> 2
      else -> 3
    }

  fun getRandomItems(numberOfCards: Int): List<IconModel> {
    val randomItems = iconList.shuffled().take(numberOfCards)
    val duplicates = randomItems.map { it.copy(id = UUID.randomUUID()) }.toMutableList()

    return with(randomItems + duplicates) {
      shuffled()
      map { it.state = mx.datafox.android.memo.model.CardState.CLOSED }
      toMutableList()
    }
  }

  internal fun checkIsMatchFound(clickedItem: IconModel) {
    lastOpenedCard?.let {
      if (it.res == clickedItem.res) {
        pairMatch.value = clickedItem
        twoCardsMatched()
      } else {
        closeCards.value = clickedItem
      }
    } ?: kotlin.run {
      lastOpenedCard = clickedItem
    }
  }

  private fun twoCardsMatched() {
    // notificar que un par ha sido emparejado
    pairs++

    // subir el nivel si todas las cartas son emparejadas
    if (pairs == pairsSum) {
      showSuccessDialog.postValue(Unit)
    }
  }
}