package mx.datafox.android.memo.services

import android.media.MediaPlayer
import mx.datafox.android.memo.R
import mx.datafox.android.memo.model.MusicState

// TODO: Inherit Service()
class MusicService {

  private var musicState = MusicState.STOP
  private var musicMediaPlayer: MediaPlayer? = null

  private val songs: List<Int> = listOf(
      R.raw.driving_ambition,
      R.raw.beautiful_dream
  )
  private var randomSongs = mutableListOf<Int>()

  // TODO: Definir variable MusicBinder()

  // TODO: Añadir onBind()

  fun runAction(state: MusicState) {
    musicState = state
    when (state) {
      MusicState.PLAY -> startMusic()
      MusicState.PAUSE -> pauseMusic()
      MusicState.STOP -> stopMusic()
      MusicState.SHUFFLE_SONGS -> shuffleSongs()
    }
  }

  // TODO: Añadir getNameOfSong()

  private fun initializeMediaPlayer() {
    if (randomSongs.isEmpty()) {
      randomizeSongs()
    }
    // TODO: Inicializar Media Player
  }

  private fun startMusic() {
    initializeMediaPlayer()
    musicMediaPlayer?.start()
  }

  private fun pauseMusic() {
    musicMediaPlayer?.pause()
  }

  private fun stopMusic() {
    musicMediaPlayer?.run {
      stop()
      release()
    }
  }

  private fun shuffleSongs() {
    musicMediaPlayer?.run {
      stop()
      release()
    }
    randomizeSongs()
    startMusic()
  }

  private fun randomizeSongs() {
    randomSongs.clear()
    randomSongs.addAll(songs.shuffled())
  }

  // TODO: Crear binder - MusicBinder
}