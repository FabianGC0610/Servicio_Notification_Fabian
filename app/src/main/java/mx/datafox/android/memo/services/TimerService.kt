package mx.datafox.android.memo.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.health.TimerStat
import mx.datafox.android.memo.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mx.datafox.android.memo.R
import mx.datafox.android.memo.helper.NotificationHelper
import mx.datafox.android.memo.helper.secondsToTime
import mx.datafox.android.memo.ui.TIMER_ACTION
import kotlin.coroutines.CoroutineContext

const val SERVICE_COMMAND = "Command"
const val NOTIFICATION_TEXT = "NotificationText"

// TODO: Inherit Service()
class TimerService : Service(), CoroutineScope {

  private val helper by lazy { NotificationHelper(this) }

  var serviceState: TimerState = TimerState.INITIALIZED

  // TODO: Usar NotificationHelper con lazy delegate
  private var currentTime: Int = 0
  private var startedAtTimestamp: Int = 0
    set(value) {
      currentTime = value
      field = value
    }

  private val handler = Handler(Looper.getMainLooper())
  private var runnable: Runnable = object : Runnable {
    override fun run() {
      currentTime++
      broadcastUpdate()
      // Repeat every 1 second
      handler.postDelayed(this, 1000)
    }
  }
  private val job = Job()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  // TODO: Definir onBind(), onStartCommand() y onDestroy()

  private fun startTimer(elapsedTime: Int? = null) {
    serviceState = TimerState.START

    startedAtTimestamp = elapsedTime ?: 0

    // Publicar notification
    // TODO: Llamar startForeground() para publicar la notificación
    startForeground(NotificationHelper.NOTIFICATION_ID, helper.getNotification())

    broadcastUpdate()

    startCoroutineTimer()
  }

  private fun broadcastUpdate() {
    // actualizar notificación
    if (serviceState == TimerState.START) {
      // count elapsed time
      val elapsedTime = (currentTime - startedAtTimestamp)

      // enviar tiempo para actualizar la UI
      // TODO: Enviar broadcast y llamar updateNotification
      sendBroadcast(
        Intent(TIMER_ACTION)
          .putExtra(NOTIFICATION_TEXT, elapsedTime)
      )
      helper.updateNotification(
        getString(R.string.time_is_running, elapsedTime.secondsToTime())
      )

    } else if (serviceState == TimerState.PAUSE) {
      // TODO: Llamar updateNotification si el timer está pausado
      helper.updateNotification("Oye! Regresa! :]")

    }
  }

  private fun pauseTimerService() {
    serviceState = TimerState.PAUSE
    handler.removeCallbacks(runnable)
    broadcastUpdate()
  }

  private fun endTimerService() {
    serviceState = TimerState.STOP
    handler.removeCallbacks(runnable)
    job.cancel()
    broadcastUpdate()
    stopService()
  }

  private fun stopService() {
    // TODO: Llamada para detener el servicio

    stopForeground(true)
    stopSelf()
  }

  private fun startCoroutineTimer() {
    launch(coroutineContext) {
      handler.post(runnable)
    }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    intent?.extras?.run{
      when(getSerializable(SERVICE_COMMAND) as TimerState){
        TimerState.START -> startTimer()
        TimerState.PAUSE -> pauseTimerService()
        TimerState.STOP -> endTimerService()
        else -> return START_NOT_STICKY
      }
    }
    return START_NOT_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    handler.removeCallbacks(runnable)
    job.cancel()
  }
}