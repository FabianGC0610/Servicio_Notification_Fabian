package mx.datafox.android.memo.ui

import android.content.*
import android.os.*
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import mx.datafox.android.memo.R
import mx.datafox.android.memo.databinding.ActivityMainBinding
import mx.datafox.android.memo.helper.SharedPrefs
import mx.datafox.android.memo.helper.onClick
import mx.datafox.android.memo.helper.secondsToTime
import mx.datafox.android.memo.model.Level
import mx.datafox.android.memo.model.Level.Companion.getLevel
import mx.datafox.android.memo.model.MusicState
import mx.datafox.android.memo.model.TimerState
import mx.datafox.android.memo.view_model.MainViewModel
import mx.datafox.android.memo.services.NOTIFICATION_TEXT
import mx.datafox.android.memo.services.SERVICE_COMMAND
import mx.datafox.android.memo.services.TimerService


/**
 * Main Screen
 */

const val TIMER_ACTION = "TimerAction"

class MainActivity : AppCompatActivity() {

  private val binding: ActivityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }
  private val mainViewModel: MainViewModel by lazy {
    ViewModelProvider(this).get(MainViewModel::class.java)
  }
  private val sharedPrefs by lazy { SharedPrefs(this) }
  private val adapter by lazy { IconAdapter(this, mainViewModel::checkIsMatchFound) }
  private val alertBuilder by lazy { AlertDialog.Builder(this) }
  private val handler by lazy { Handler(Looper.getMainLooper()) }

  // Foreground receiver
  private val timerReceiver: TimerReceiver by lazy { TimerReceiver() }

  // Bound Service
  // TODO: Definir variable musicService

  // TODO: Definir boundServiceConnection

  override fun onCreate(savedInstanceState: Bundle?) {
    // Cambiar el AppTheme para mostrar la actividad
    setTheme(R.style.AppTheme)

    super.onCreate(savedInstanceState)
    setContentView(binding.root)
    observe()
    setUpUi()
  }

  private fun observe() {
    mainViewModel.apply {
      updateGridVisibility.observe(this@MainActivity) {
        updateVisibility()
      }
      closeCards.observe(this@MainActivity) { clickedItem ->
        handler.postDelayed({
          adapter.revertVisibility(mainViewModel.lastOpenedCard, clickedItem)
          mainViewModel.lastOpenedCard = null
        }, 300)
      }
      pairMatch.observe(this@MainActivity) { clickedItem ->
        handler.postDelayed({
          adapter.pairMatch(mainViewModel.lastOpenedCard, clickedItem)
          mainViewModel.lastOpenedCard = null
        }, 300)
      }
      showSuccessDialog.observe(this@MainActivity) {
        storeLevel()
        sendCommandToForegroundService(TimerState.STOP)
        showSuccessDialog()
      }
    }
  }

  override fun onResume() {
    super.onResume()

    // registrar si se necesita foreground service receiver
    if (!mainViewModel.isReceiverRegistered) {
      registerReceiver(timerReceiver, IntentFilter(TIMER_ACTION))
      mainViewModel.isReceiverRegistered = true
    }
  }

  override fun onPause() {
    super.onPause()
    // resetear foreground service receiver si está registrado
    if (mainViewModel.isReceiverRegistered) {
      unregisterReceiver(timerReceiver)
      mainViewModel.isReceiverRegistered = false
    }
  }

  override fun onStart() {
    super.onStart()
    // ligar al servicio si no está ligado
    // TODO: Bind al servicio de música
  }

  override fun onDestroy() {
    super.onDestroy()
    unbindMusicService()

    // si el timer se está ejecutando, pausarlo
    if (isFinishing && mainViewModel.isForegroundServiceRunning) {
      sendCommandToForegroundService(TimerState.PAUSE)
    }
  }

  // Métodos Bound Service

  // TODO: Crear bindToMusicService()

  private fun unbindMusicService() {
    if (mainViewModel.isMusicServiceBound) {
      // detener el audio
      // TODO: Llamar runAction() de MusicService

      // desconectar el servicio y guardar el estado
      // TODO: Llamar unbindService()

      mainViewModel.isMusicServiceBound = false
    }
  }

  private fun sendCommandToBoundService(state: MusicState) {
    if (mainViewModel.isMusicServiceBound) {

      // TODO: Llamar runAction() del MusicService

      informUser(state)
      enableButtons(state)
    } else {
      Toast.makeText(this, R.string.service_is_not_bound, Toast.LENGTH_SHORT).show()
    }
  }

  private fun getNameOfSong() {
    val message = if (mainViewModel.isMusicServiceBound) {
      // TODO: Obtener el nombre de la canción de Music Service
      getString(R.string.unknown)
    } else {
      getString(R.string.service_is_not_bound)
    }

    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }

  // Métodos Foreground Service

  private fun sendCommandToForegroundService(timerState: TimerState) {
    // TODO: Llamada para iniciar un foreground service (timer service)
    ContextCompat.startForegroundService(this, getServiceIntent(timerState))

    mainViewModel.isForegroundServiceRunning = timerState != TimerState.STOP
  }

  private fun getServiceIntent(command: TimerState) =
    Intent(this, TimerService::class.java).apply {
      putExtra(SERVICE_COMMAND, command as Parcelable)
    }

  // UI Methods

  private fun setUpUi() {
    with(binding) {
      gridview.adapter = adapter
      btnPlay.onClick {
        prepareCardView()
        sendCommandToForegroundService(TimerState.START)
      }
      btnPlayMusic.onClick {
        sendCommandToBoundService(MusicState.PLAY)
      }
      btnPauseMusic.onClick {
        sendCommandToBoundService(MusicState.PAUSE)
      }
      btnStopMusic.onClick {
        sendCommandToBoundService(MusicState.STOP)
      }
      btnShuffleMusic.onClick {
        sendCommandToBoundService(MusicState.SHUFFLE_SONGS)
      }
      btnSongName.onClick {
        getNameOfSong()
      }
      btnQuit.onClick {
        binding.tvTime.clearComposingText()
        sendCommandToForegroundService(TimerState.STOP)
      }
    }
    updateVisibility()
  }

  private fun updateVisibility() {
    with(binding) {
      val btnPlayVisible = if (mainViewModel.isForegroundServiceRunning) {
        View.INVISIBLE
      } else {
        View.VISIBLE
      }

      val gridVisible = if (mainViewModel.isForegroundServiceRunning) {
        View.VISIBLE
      } else {
        View.INVISIBLE
      }
      btnPlay.apply {
        visibility = btnPlayVisible
        text = String.format(
          getString(R.string.play_p_level),
          getLevel(sharedPrefs.getStoredLevel())?.name
        )
      }
      gridview.visibility = gridVisible
      tvTime.visibility = gridVisible
      btnQuit.visibility = gridVisible
    }
  }

  private fun prepareCardView() {
    val currentLevel = getLevel(sharedPrefs.getStoredLevel()) ?: Level.BEGINNER

    // resetear número de pares
    mainViewModel.pairs = 0
    mainViewModel.pairsSum = currentLevel.numberOfCards

    // definir de nuevo las columnas
    binding.gridview.numColumns = mainViewModel.getNumberOfColumns(currentLevel)

    // actualizar el adaptador
    adapter.updateData(mainViewModel.getRandomItems(sharedPrefs.getStoredLevel()))
  }

  private fun enableButtons(state: MusicState) {
    val songPlays = state == MusicState.PLAY || state == MusicState.SHUFFLE_SONGS
    with(binding) {
      btnPlayMusic.isEnabled = !songPlays
      btnPauseMusic.isEnabled = songPlays
      btnStopMusic.isEnabled = songPlays
      btnShuffleMusic.isEnabled = songPlays
      btnSongName.apply {
        isEnabled = songPlays
        btnSongName.visibility = if (songPlays) {
          View.VISIBLE
        } else {
          View.INVISIBLE
        }
      }
    }
  }

  private fun updateUi(elapsedTime: Int) {
    mainViewModel.elapsedTime = elapsedTime
    binding.tvTime.text = elapsedTime.secondsToTime()
  }

  private fun checkProgress() {
    val currentLevel = getLevel(sharedPrefs.getStoredLevel())
    if (currentLevel == Level.NONE) {
      showResetProgressDialog()
    }
  }

  // incrementar nivel
  private fun storeLevel() {
    val currentLevel = getLevel(sharedPrefs.getStoredLevel())
    currentLevel?.let {
      sharedPrefs.storeLevel(
        when (it) {
          Level.BEGINNER -> Level.INTERMEDIATE
          Level.INTERMEDIATE -> Level.ADVANCED
          Level.ADVANCED -> Level.EXPERT
          Level.EXPERT -> Level.NONE
          Level.NONE -> Level.BEGINNER
        }
      )
    }
  }

  private fun informUser(state: MusicState) {
    @StringRes val res = when (state) {
      MusicState.PLAY -> R.string.music_started
      MusicState.PAUSE -> R.string.music_paused
      MusicState.STOP -> R.string.music_stopped
      MusicState.SHUFFLE_SONGS -> R.string.songs_shuffled
    }

    Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
  }

  private fun showSuccessDialog() {
    showDialog(
      String.format(getString(R.string.well_done_your_time_is_p), mainViewModel.elapsedTime),
      getString(R.string.click_ok_for_proceeding_to_the_next_level),
    ) {
      checkProgress()
    }
  }

  private fun showResetProgressDialog() {
    showDialog(
      getString(R.string.you_have_finished_all_levels),
      getString(R.string.click_ok_to_reset_progress),
    ) {
      sharedPrefs.storeLevel(Level.BEGINNER)
    }
  }

  private fun showDialog(
    title: String,
    message: String,
    action: () -> Unit
  ) {
    with(alertBuilder)
    {
      setCancelable(false)
      setTitle(title)
      setMessage(message)
      setPositiveButton(getString(R.string.ok)) { _, _ ->
        action()
      }
      show()
    }
  }

  inner class TimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
      if (intent.action == TIMER_ACTION) updateUi(intent.getIntExtra(NOTIFICATION_TEXT, 0))
    }
  }
}