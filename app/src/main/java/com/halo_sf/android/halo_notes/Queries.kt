package com.halo_sf.android.halo_notes

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import com.halo_sf.android.halo_notes.MainActivity.Companion.noteDao
import com.halo_sf.android.halo_notes.NoteLists.Companion.notes
import com.halo_sf.android.halo_notes.NoteLists.Companion.strCreated
import com.halo_sf.android.halo_notes.NoteLists.Companion.strEdited
import com.halo_sf.android.halo_notes.NoteLists.Companion.strId
import com.halo_sf.android.halo_notes.NoteLists.Companion.strNote
import com.halo_sf.android.halo_notes.NoteLists.Companion.strNotes
import com.halo_sf.android.halo_notes.NoteLists.Companion.strTagColor
import com.halo_sf.android.halo_notes.NoteLists.Companion.strTitle
import com.halo_sf.android.halo_notes.NoteLists.Companion.trash
import com.halo_sf.android.halo_notes.databinding.FragmentNotesBinding
import com.halo_sf.android.halo_notes.databinding.FragmentTrashBinding
import com.halo_sf.android.halo_notes.room.NoteEntity

class Queries(private val recyclerView: RecyclerView) {

    private val adapter = recyclerView.adapter!!

    fun insert(title: String, note: String, tagColor: String?, binding: FragmentNotesBinding) {
        val created = System.currentTimeMillis().toString()
        val firstId = noteDao.getFirstRecordId()
        firstId?.let { noteDao.updateFirstRecord(firstId, false) }
        val id = noteDao.insertItem(NoteEntity(0, title, note, tagColor, created, null, firstId, true, null))
        notes.add(0, NoteLists.mutableMapOf(id.toString(), title, note, tagColor, created, null, null))
        adapter.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)
        binding.root.removeView(binding.tvNoNotes)
    }

    fun update(list: MutableList<MutableMap<String, String?>>, judgement: String, position: Int, title: String, note: String, tagColor: String?) {
        val item = list[position]; val id = item["id"]!!
        val edited = System.currentTimeMillis().toString()
        noteDao.updateItem(id.toInt(), title, note, tagColor, edited)
        val data = NoteLists.mutableMapOf(id, title, note, tagColor, item["created"], edited, item["delete"])
        list[position] = data
        adapter.notifyItemChanged(position)
    }

    fun delete(list: MutableList<MutableMap<String, String?>>, position: Int, context: Context, binding: FragmentNotesBinding) {
        try {
            val delete = System.currentTimeMillis() + 604800000
            val item = list[position]
            val id = item[strId]!!.toInt()
            noteDao.updateDelete(id, delete)
            val index = if (trash.isEmpty() || noteDao.getFirstRecord(id)) { 0 }
            else {
                var index: Int? = null
                var previousId = noteDao.getPreviousId(id)
                while (previousId != null) {
                    if (noteDao.getDelete(previousId) != null) {
                        val previousItem = noteDao.getItem(previousId)
                        index = trash.indexOf(NoteLists.mutableMapOf(previousId.toString(), previousItem.title, previousItem.note,
                            previousItem.tagColor, previousItem.created, previousItem.edited, previousItem.delete?.toString())) + 1
                        break
                    }
                    previousId = noteDao.getPreviousId(previousId)
                }
                if (index == null) {
                    var nextId: Int? = noteDao.getNextId(id)
                    while (nextId != null) {
                        if (noteDao.getDelete(nextId) != null) {
                            val nextItem = noteDao.getItem(nextId)
                            index = trash.indexOf(NoteLists.mutableMapOf(nextId.toString(), nextItem.title, nextItem.note,
                                nextItem.tagColor, nextItem.created, nextItem.edited, nextItem.delete?.toString()))
                            break
                        }
                        nextId = noteDao.getNextId(nextId)
                    }
                }
                index!!
            }
            trash.add(index, NoteLists.mutableMapOf(id.toString(), item[strTitle], item[strNote], item[strTagColor], item[strCreated], item[strEdited], delete.toString()))
            if (list != notes) { list.removeAt(position); notes.remove(item) }
            else { notes.removeAt(position); if (notes.size == 0) { binding.root.addView(binding.tvNoNotes) } }
            adapter.notifyItemRemoved(position)
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
                .putExtra("ID", id)
            alarmMgr.set(AlarmManager.RTC, delete, PendingIntent.getBroadcast(context, id, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT))
        } catch (_: ArrayIndexOutOfBoundsException) {
        } catch (_: IndexOutOfBoundsException) {
        }
    }

    fun restore(list: MutableList<MutableMap<String, String?>>, position: Int, context: Context, binding: FragmentTrashBinding) {
        try {
            val item = list[position]
            val id = item[strId]!!.toInt()
            noteDao.updateDelete(id, null)
            val index = if (notes.isEmpty() || noteDao.getFirstRecord(id)) { 0 }
            else {
                var index: Int? = null
                var previousId = noteDao.getPreviousId(id)
                while (previousId != null) {
                    if (noteDao.getDelete(previousId) == null) {
                        val previousItem = noteDao.getItem(previousId)
                        index = notes.indexOf(NoteLists.mutableMapOf(previousId.toString(), previousItem.title, previousItem.note,
                            previousItem.tagColor, previousItem.created, previousItem.edited, previousItem.delete?.toString())) + 1
                        break
                    }
                    previousId = noteDao.getPreviousId(previousId)
                }
                if (index == null) {
                    var nextId: Int? = noteDao.getNextId(id)
                    while (nextId != null) {
                        if (noteDao.getDelete(nextId) == null) {
                            val nextItem = noteDao.getItem(nextId)
                            index = notes.indexOf(NoteLists.mutableMapOf(nextId.toString(), nextItem.title, nextItem.note,
                                nextItem.tagColor, nextItem.created, nextItem.edited, nextItem.delete?.toString()))
                            break
                        }
                        nextId = noteDao.getNextId(nextId)
                    }
                }
                index!!
            }
            notes.add(index, NoteLists.mutableMapOf(id.toString(), item[strTitle], item[strNote], item[strTagColor], item[strCreated], item[strEdited], null))
            if (list != trash) { list.removeAt(position); trash.remove(item) }
            else { notes.removeAt(position); if (notes.size == 0) { binding.root.addView(binding.tvNoNotesInTrash) } }
            adapter.notifyItemRemoved(position)
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
                .putExtra("ID", id)
            alarmMgr.cancel(PendingIntent.getBroadcast(context, id, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT))
        } catch (_: ArrayIndexOutOfBoundsException) {
        } catch (_: IndexOutOfBoundsException) {
        }
    }

    fun deleteForever(position: Int, fragmentNotesBinding: FragmentNotesBinding?, fragmentTrashBinding: FragmentTrashBinding?) {
        val list: MutableList<MutableMap<String, String?>>; val id: Int
        if (fragmentNotesBinding != null) { list = notes; id = notes[position][strId]!!.toInt() } else { list = trash; id = trash[position][strId]!!.toInt() }
        val previousID = noteDao.getPreviousId(id)
        val nextId = noteDao.getNextId(id)
        if (previousID != null) { noteDao.updateNextId(previousID, nextId) } else { nextId?.let { noteDao.updateFirstRecord(nextId, true) } }
        noteDao.deleteItem(id)
        list.removeAt(position)
        adapter.notifyItemRemoved(position)
        if (fragmentNotesBinding != null) {
            if (notes.size == 0) { fragmentNotesBinding.root.addView(fragmentNotesBinding.tvNoNotes) }
        } else {
            if (trash.size == 0) { fragmentTrashBinding!!.root.addView(fragmentTrashBinding.tvNoNotesInTrash) }
        }
    }
}