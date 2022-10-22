package com.halo_sf.android.halo_notes

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.halo_sf.android.halo_notes.MainActivity.Companion.noteDao
import com.halo_sf.android.halo_notes.room.NoteEntity
import java.text.SimpleDateFormat
import java.util.*

class InsertingInitialData(val context: Context) {
    private val version = context.getSharedPreferences("holo_notes", AppCompatActivity.MODE_PRIVATE).getInt("version", 0)
    private val receiveSettings = context.getSharedPreferences("holo_notes", AppCompatActivity.MODE_PRIVATE).getInt("receive_settings", 0)

    fun insertInitialData() {
        if (version == 0) {
            noteDao.insertItem(
                NoteEntity(0, context.getString(R.string.initial_data_2_title), context.getString(R.string.initial_data_2_note),
                    null, System.currentTimeMillis().toString(), null, null, false, null)
            )
            noteDao.insertItem(
                NoteEntity(0, context.getString(R.string.initial_data_1_title), context.getString(R.string.initial_data_1_note),
                    null, System.currentTimeMillis().toString(), null, 1, true, null)
            )
            context.getSharedPreferences("holo_notes", AppCompatActivity.MODE_PRIVATE).edit().putInt("version", 4)
                .apply()
        } else {
            if (version <= 2) {
                if (receiveSettings == 0) {
                    val firstId = noteDao.getFirstRecordId()
                    noteDao.insertItem(
                        NoteEntity(0, context.getString(R.string.initial_data_4_title), context.getString(R.string.initial_data_4_note),
                            null, getTimeMillis("2022/10/05"), null, firstId, true, null)
                    )
                    firstId?.let { noteDao.updateFirstRecord(firstId, false) }
                    context.getSharedPreferences("holo_notes", AppCompatActivity.MODE_PRIVATE).edit().putInt("version", 4)
                        .apply()
                }
            }

            if (version <= 3) {
                if (receiveSettings <= 1) {
                    val firstId = noteDao.getFirstRecordId()
                    noteDao.insertItem(
                        NoteEntity(0, context.getString(R.string.initial_data_2_title), context.getString(R.string.initial_data_2_note),
                            null, System.currentTimeMillis().toString(), null, firstId, false, null)
                    )
                    noteDao.insertItem(
                        NoteEntity(0, context.getString(R.string.initial_data_5_title), context.getString(R.string.initial_data_5_note),
                            null, getTimeMillis("2022/10/07"), null, firstId?.plus(1), true, null)
                    )
                    firstId?.let { noteDao.updateFirstRecord(firstId, false) }
                    context.getSharedPreferences("holo_notes", AppCompatActivity.MODE_PRIVATE).edit().putInt("version", 4)
                        .apply()
                }
            }
        }
    }

    private fun getTimeMillis(time: String) = SimpleDateFormat("yyyy/mm/dd", Locale.JAPANESE).parse(time)!!.time.toString()
}