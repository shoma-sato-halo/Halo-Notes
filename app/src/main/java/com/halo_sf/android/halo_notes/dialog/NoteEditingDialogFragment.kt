package com.halo_sf.android.halo_notes.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.halo_sf.android.halo_notes.NoteLists
import com.halo_sf.android.halo_notes.NoteLists.Companion.dateFormat
import com.halo_sf.android.halo_notes.NoteLists.Companion.strCreated
import com.halo_sf.android.halo_notes.NoteLists.Companion.strEdited
import com.halo_sf.android.halo_notes.NoteLists.Companion.strNote
import com.halo_sf.android.halo_notes.NoteLists.Companion.strTagColor
import com.halo_sf.android.halo_notes.NoteLists.Companion.strTitle
import com.halo_sf.android.halo_notes.Queries
import com.halo_sf.android.halo_notes.R
import com.halo_sf.android.halo_notes.databinding.DialogEditingANoteBinding
import com.halo_sf.android.halo_notes.databinding.FragmentNotesBinding
import com.halo_sf.android.halo_notes.databinding.FragmentTrashBinding

class NoteEditingDialogFragment(private val list: MutableList<MutableMap<String, String?>>, private val recyclerView: RecyclerView, private val position: Int,
                                private val fragmentNotesBinding: FragmentNotesBinding?, private val fragmentTrashBinding: FragmentTrashBinding?) : DialogFragment() {

    private lateinit var binding: DialogEditingANoteBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEditingANoteBinding.inflate(layoutInflater)

        val queries = Queries(recyclerView)

        var item: MutableMap<String, String?> = mutableMapOf()

        var tagColor: Int? = null
        try {
            item = list[position]
            binding.frame.etTitle.setText(item[strTitle])
            binding.frame.etNote.setText(item[strNote])
            tagColor = item[strTagColor]?.toInt()
            if (tagColor != null) { binding.ivColorTag.setColorFilter(ContextCompat.getColor(requireContext(), tagColor)) }
            if (item[strEdited] == null) {
                binding.constraintLayout.removeView(binding.tvsAbove)
                binding.tvBelowHint.text = getString(R.string.tv_created_hint_text)
                binding.tvBelow.text = dateFormat(item[strCreated]!!.toLong(), requireContext())
            } else {
                binding.tvAbove.text = dateFormat(item[strCreated]!!.toLong(), requireContext())
                binding.tvBelow.text = dateFormat(item[strEdited]!!.toLong(), requireContext())
            }
        } catch (e: Exception) {
            dismiss()
        }

        val event = View.OnClickListener {
            tagColor = when(it.id) {
                R.id.ib_red_tag -> R.color.red; R.id.ib_yellow_tag -> R.color.yellow; R.id.ib_green_tag -> R.color.green
                R.id.ib_blue_tag -> R.color.blue; R.id.ib_purple_tag -> R.color.purple; R.id.ib_gray_tag -> R.color.gray; else -> null
            }
            binding.ivColorTag.setColorFilter(ContextCompat.getColor(requireContext(), tagColor!!))
        }
        binding.ibColorTagOutline.setOnClickListener { tagColor = null; binding.ivColorTag.clearColorFilter() }
        binding.colorTags.ibRedTag.setOnClickListener(event); binding.colorTags.ibYellowTag.setOnClickListener(event); binding.colorTags.ibGreenTag.setOnClickListener(event)
        binding.colorTags.ibBlueTag.setOnClickListener(event); binding.colorTags.ibPurpleTag.setOnClickListener(event); binding.colorTags.ibGrayTag.setOnClickListener(event)

        if (fragmentNotesBinding != null) {
            binding.ibDelete.setOnClickListener { queries.delete(list, position, binding.root.context, fragmentNotesBinding); dismiss() }
            binding.constraintLayout.removeView(binding.btnRestore)
        } else {
            binding.ibDelete.setImageResource(R.drawable.ic_outline_delete_forever_40)
            binding.ibDelete.setOnClickListener {
                DeleteConfirmationDialogFragment(recyclerView, mutableListOf(position), this, null, fragmentTrashBinding)
                    .show(parentFragmentManager, "DeleteConfirmationDialogFragment")
            }
            binding.btnRestore.setOnClickListener{ queries.restore(position, binding.root.context, fragmentTrashBinding!!); dismiss() }
        }

        binding.ibDelete.setOnLongClickListener {
            DeleteConfirmationDialogFragment(recyclerView, mutableListOf(position), this, fragmentNotesBinding, fragmentTrashBinding)
                .show(parentFragmentManager, "DeleteConfirmationDialogFragment")
            true
        }

        binding.frame.btnCancel.setOnClickListener { dismiss() }
        binding.frame.btnOk.setOnClickListener {
            if (binding.frame.etTitle.text.toString() != item[strTitle] || binding.frame.etNote.text.toString() != item[strNote]
                || tagColor?.toString() != item[strTagColor]) {
                queries.update(list, if (fragmentNotesBinding != null) { NoteLists.strNotes } else { NoteLists.strTrash },
                    position, binding.frame.etTitle.text.toString(), binding.frame.etNote.text.toString(), tagColor?.toString())
            }
            dismiss()
        }

        val builder = AlertDialog.Builder(activity)
        builder.setView(binding.root)
        return builder.create()
    }
}