package com.halo_sf.android.halo_notes.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.halo_sf.android.halo_notes.Queries
import com.halo_sf.android.halo_notes.R
import com.halo_sf.android.halo_notes.databinding.DialogTakingANoteBinding
import com.halo_sf.android.halo_notes.databinding.FragmentNotesBinding


class NoteTakingDialogFragment(private val recyclerView: RecyclerView, private val fragmentNotesBinding: FragmentNotesBinding): DialogFragment() {

    private lateinit var binding: DialogTakingANoteBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogTakingANoteBinding.inflate(layoutInflater)

        val queries = Queries(recyclerView)

        var tagColor: Int? = null
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

        binding.frame.btnCancel.setOnClickListener { dismiss() }
        binding.frame.btnOk.setOnClickListener{
            queries.insert(binding.frame.etTitle.text.toString(), binding.frame.etNote.text.toString(), tagColor?.toString(), fragmentNotesBinding); dismiss()
        }

        val builder = AlertDialog.Builder(activity)
        builder.setView(binding.root)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        binding.frame.etTitle.requestFocus()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }
}