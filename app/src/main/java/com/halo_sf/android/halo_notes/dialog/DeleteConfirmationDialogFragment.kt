package com.halo_sf.android.halo_notes.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ActionMode
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.halo_sf.android.halo_notes.Queries
import com.halo_sf.android.halo_notes.R
import com.halo_sf.android.halo_notes.databinding.FragmentNotesBinding
import com.halo_sf.android.halo_notes.databinding.FragmentTrashBinding

class DeleteConfirmationDialogFragment(
    private val recyclerView: RecyclerView, private val positions: MutableList<Int>, private val noteEditingDialogFragment: NoteEditingDialogFragment?,
    private val fragmentNotesBinding: FragmentNotesBinding?, private val fragmentTrashBinding: FragmentTrashBinding?, private val mode: ActionMode? = null
    ) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.title_delete_confirmation_dialog)
            .setPositiveButton(R.string.btn_ok_text) { _, _ ->
                for (position in positions.sortedDescending()) {
                    Queries(recyclerView).deleteForever(position, fragmentNotesBinding, fragmentTrashBinding); dismiss()
                    noteEditingDialogFragment?.dismiss(); mode?.finish()
                }
            }
            .setNegativeButton(R.string.btn_cancel_text) { _, _ ->}
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        val alertDialog = dialog as AlertDialog
        val array = requireActivity().theme.obtainStyledAttributes(R.style.Theme_HaloNotes, intArrayOf(com.google.android.material.R.attr.colorOnPrimary))
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(array.getColor(0, 0))
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(array.getColor(0, 0))
    }
}