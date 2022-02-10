package mx.datafox.android.memo.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import mx.datafox.android.memo.R
import mx.datafox.android.memo.ui.MainActivity

private const val CHANNEL_ID = "StarWarsChannel"
private const val CHANNEL_NAME = "StarWarsChannelName"
private const val CHANNEL_DESCRIPTION = "StarWarsChannelDescription"

class NotificationHelper(private val context: Context) {

  private val notificationManager by lazy {
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  }

  private val notificationBuilder: NotificationCompat.Builder by lazy {
    NotificationCompat.Builder(context, CHANNEL_ID)
      .setContentTitle("Memo")
      .setSound(null)
      .setContentIntent(contentIntent)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      // 3
      .setAutoCancel(true)
  }

  // TODO: Añadir Notification Builder
  @RequiresApi(Build.VERSION_CODES.O)
  private fun createChannel() =
    // 1
    NotificationChannel(
      CHANNEL_ID,
      CHANNEL_NAME,
      NotificationManager.IMPORTANCE_DEFAULT
    ).apply {

      // 2
      description = CHANNEL_DESCRIPTION
      setSound(null,null)
    }

  // TODO: Añadir notificationManager

  // TODO: Añadir contentIntent
  private val contentIntent by lazy{
    PendingIntent.getActivity(
      context,
      0,
      Intent(context, MainActivity::class.java),
      PendingIntent.FLAG_UPDATE_CURRENT
    )
  }

  // TODO: Definir getNotification()
  fun getNotification(): Notification{
    // 1
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
      notificationManager.createNotificationChannel(createChannel())
    }

    // 2
    return notificationBuilder.build()
  }

  // TODO: Definir updateNotification()
  fun updateNotification(notificationText: String? = null){
    notificationText?.let{ notificationBuilder.setContentText(it)}
    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
  }

  // TODO: Definir createChannel()

  companion object {
    const val NOTIFICATION_ID = 99
  }
}