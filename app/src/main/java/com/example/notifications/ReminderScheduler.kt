package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.model.ReminderItem
import java.util.Calendar

object ReminderScheduler {

    fun syncReminders(context: Context, reminders: List<ReminderItem>, isArabic: Boolean) {
        ReminderReceiver.ensureChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        reminders.forEach { reminder ->
            val requestCode = reminder.id.hashCode()
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(
                    ReminderReceiver.EXTRA_TITLE,
                    if (isArabic) reminder.titleAr else reminder.titleEn
                )
                putExtra(
                    ReminderReceiver.EXTRA_BODY,
                    if (isArabic) {
                        "حان موعد هدفك اليومي في NutriPulse! حافظ على استمراريتك."
                    } else {
                        "Time for your NutriPulse daily goal! Keep your streak alive."
                    }
                )
                putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, requestCode)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (reminder.enabled) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, reminder.hour.toInt().coerceIn(0, 23))
                    set(Calendar.MINUTE, reminder.minute.toInt().coerceIn(0, 59))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } else {
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    fun sendTestNotification(context: Context, isArabic: Boolean) {
        val title = if (isArabic) "تذكير NutriPulse الذكي" else "NutriPulse Smart Alert"
        val body = if (isArabic) {
            "تذكير تجريبي: اشرب كوب ماء وسجّل وجبتك القادمة لتحقق هدف البروتين والكارب اليوم!"
        } else {
            "Test reminder: Hydrate now and log your next meal to hit today's protein & carb goals!"
        }
        ReminderReceiver.showNotificationNow(context, title, body, 9999)
    }
}
