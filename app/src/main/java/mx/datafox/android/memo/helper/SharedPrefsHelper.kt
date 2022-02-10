package mx.datafox.android.memo.helper

import android.content.Context
import android.content.SharedPreferences
import mx.datafox.android.memo.model.Level

const val PREFS_NAME = "MemoAppSharedPrefs"
const val LEVEL = "StoredLevel"

class SharedPrefs(context: Context) {

  private val sharedPreferences: SharedPreferences by lazy {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun storeLevel(level: Level) {
    sharedPreferences.edit().putInt(LEVEL, level.numberOfCards).apply()
  }

  fun getStoredLevel() = sharedPreferences.getInt(LEVEL, Level.BEGINNER.numberOfCards)
}