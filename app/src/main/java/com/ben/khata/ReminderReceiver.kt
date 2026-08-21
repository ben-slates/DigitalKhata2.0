package com.ben.khata

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ben.khata.data.KhataDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminderId", -1)
        if (reminderId == -1L) return

        val action = intent.action
        if (action == "MARK_COMPLETE") {
            CoroutineScope(Dispatchers.IO).launch {
                val db = KhataDatabase.create(context)
                val dao = db.dao()
                val r = dao.getReminderById(reminderId)
                if (r != null) {
                    dao.updateReminderStatus(reminderId, 1) // 1 = Completed
                    val txAmount = if (r.type == 1) r.amount else -r.amount
                    dao.addEntry("", txAmount, "From reminder", r.personId)
                    dao.addToBudget(txAmount)
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(reminderId.toInt())
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val db = KhataDatabase.create(context)
            val reminder = db.dao().getReminderById(reminderId) ?: return@launch
            if (reminder.status == 1) return@launch // Already completed

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel("reminders", "Payment Reminders", NotificationManager.IMPORTANCE_HIGH)
                manager.createNotificationChannel(channel)
            }

            val amountStr = "Rs. ${NumberFormat.getIntegerInstance(Locale.US).format(reminder.amount)}"
            val typeStr = if (reminder.type == 0) "Pay $amountStr to ${reminder.personName}" else "Receive $amountStr from ${reminder.personName}"

            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("reminderId", reminderId)
            }
            val mainPending = PendingIntent.getActivity(context, reminderId.toInt(), mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val completeIntent = Intent(context, ReminderReceiver::class.java).apply {
                this.action = "MARK_COMPLETE"
                putExtra("reminderId", reminderId)
            }
            val completePending = PendingIntent.getBroadcast(context, reminderId.toInt() + 10000, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val notification = NotificationCompat.Builder(context, "reminders")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Payment Reminder")
                .setContentText(typeStr)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(mainPending)
                .setAutoCancel(true)
                .addAction(android.R.drawable.checkbox_on_background, "Mark Complete", completePending)
                .build()

            manager.notify(reminderId.toInt(), notification)
        }
    }
}
