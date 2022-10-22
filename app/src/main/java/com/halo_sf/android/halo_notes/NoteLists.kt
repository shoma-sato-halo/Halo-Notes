package com.halo_sf.android.halo_notes

import android.content.Context
import android.text.format.DateFormat.getDateFormat
import android.text.format.DateFormat.getTimeFormat
import java.util.*

class NoteLists {
    companion object {
        val notes: MutableList<MutableMap<String, String?>> = mutableListOf()
        val trash: MutableList<MutableMap<String, String?>> = mutableListOf()
        const val strNotes = "notes"
        const val strTrash = "trash"
        const val strId = "id"
        const val strTitle = "title"
        const val strNote = "note"
        const val strTagColor= "tagColor"
        const val strCreated = "created"
        const val strEdited = "edited"
        const val strDelete = "delete"

        fun dateFormat(date: Long, context: Context): String {
            return getDateFormat(context).format(Date(date)) + " " + getTimeFormat(context). format(Date(date))
        }

        fun mutableMapOf(id: String?, title: String?, note: String?, tagColor: String?, created: String?, edited: String?, delete: String?): MutableMap<String, String?> {
            return mutableMapOf(Pair(strId, id), Pair(strTitle, title), Pair(strNote, note),
                Pair(strTagColor, tagColor), Pair(strCreated, created), Pair(strEdited, edited), Pair(strDelete, delete))
        }
    }
}