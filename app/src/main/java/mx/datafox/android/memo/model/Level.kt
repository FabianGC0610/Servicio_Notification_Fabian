package mx.datafox.android.memo.model

enum class Level(val numberOfCards: Int) {
  BEGINNER(2),
  INTERMEDIATE(3),
  ADVANCED(4),
  EXPERT(5),
  NONE(0);

  companion object {

    fun getLevel(num: Int): Level? =
      Level.values().find { it.numberOfCards == num }
  }
}