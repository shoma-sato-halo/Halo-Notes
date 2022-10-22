package com.halo_sf.android.halo_notes

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.halo_sf.android.halo_notes.room.NoteDatabase

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals("android.intent.action.BOOT_COMPLETED")) {
            val noteDao = Room.databaseBuilder(context, NoteDatabase::class.java, "NoteDatabase").allowMainThreadQueries().build().noteDao()
            val deletingTimesId = noteDao.getDeleteIds()
            for (id in deletingTimesId) {
                val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val newIntent = Intent(context, AlarmReceiver::class.java)
                newIntent.putExtra("ID", id)
                val alarmIntent = PendingIntent.getBroadcast(context, id, newIntent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
                alarmMgr.set(AlarmManager.RTC, noteDao.getDelete(id)!!, alarmIntent)
            }
        } else {
            val noteDao = Room.databaseBuilder(context, NoteDatabase::class.java, "NoteDatabase").allowMainThreadQueries().build().noteDao()
            val id = intent.extras!!.getInt("ID")
            val previousID = noteDao.getPreviousId(id)
            val nextId = noteDao.getNextId(id)
            if (previousID != null) { noteDao.updateNextId(previousID, nextId) }
            else { nextId?.let { noteDao.updateFirstRecord(nextId, true) } }
            noteDao.deleteItem(id)
        }
    }
}