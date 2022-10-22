package com.halo_sf.android.halo_notes.myDetailsLookup

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.halo_sf.android.halo_notes.customAdapter.TrashAdapter

class TrashDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as TrashAdapter.ViewHolder)
                .getItemDetails()
        }
        return null
    }
}