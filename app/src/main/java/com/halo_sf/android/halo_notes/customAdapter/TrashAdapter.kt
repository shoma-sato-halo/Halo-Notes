package com.halo_sf.android.halo_notes.customAdapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.halo_sf.android.halo_notes.NoteLists.Companion.dateFormat
import com.halo_sf.android.halo_notes.NoteLists.Companion.strCreated
import com.halo_sf.android.halo_notes.NoteLists.Companion.strDelete
import com.halo_sf.android.halo_notes.NoteLists.Companion.strEdited
import com.halo_sf.android.halo_notes.NoteLists.Companion.strNote
import com.halo_sf.android.halo_notes.NoteLists.Companion.strTagColor
import com.halo_sf.android.halo_notes.NoteLists.Companion.strTitle
import com.halo_sf.android.halo_notes.Queries
import com.halo_sf.android.halo_notes.R
import com.halo_sf.android.halo_notes.databinding.FragmentTrashBinding
import com.halo_sf.android.halo_notes.databinding.TrashRowBinding
import com.halo_sf.android.halo_notes.dialog.DeleteConfirmationDialogFragment
import com.halo_sf.android.halo_notes.dialog.NoteEditingDialogFragment

class TrashAdapter(private val list: MutableList<MutableMap<String, String?>>, private val recyclerView: RecyclerView, private val supportFragmentManager: FragmentManager,
                   private val fragmentTrashBinding: FragmentTrashBinding) : RecyclerView.Adapter<TrashAdapter.ViewHolder>() {

    var tracker: SelectionTracker<Long>? = null

    init { setHasStableIds(true) }

    companion object { val itemViews: ArrayList<View> = arrayListOf() }

    class ViewHolder(val binding: TrashRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(isActivated: Boolean = false) {
            itemView.isActivated = isActivated
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long = itemId
            }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TrashRowBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding

        val item = list[position]

        itemViews.add(binding.constraintLayout)

        var dialog: NoteEditingDialogFragment? = null
        val event = View.OnClickListener {
            dialog = NoteEditingDialogFragment(list, recyclerView, holder.layoutPosition, null, fragmentTrashBinding)
            dialog!!.show(supportFragmentManager, "NoteEditingDialog")
        }

        val title = item[strTitle]; val note = item[strNote]
        binding.frame.constraintLayout.removeView(binding.frame.tvsAbove)
        binding.frame.constraintLayout.removeView(binding.frame.tvsBelow)
        if (title == "" || note == "") {
            val text = if (title != "") { title } else if (note != "") { binding.frame.tvAboveHint.text = binding.root.context.getString(R.string.tv_note_hint_text); note } else { null }
            binding.frame.constraintLayout.addView(binding.frame.tvsAbove)
            binding.frame.tvAbove.text = text
            binding.frame.tvAbove.setOnClickListener(event)
        } else {
            binding.frame.tvAboveHint.text = binding.root.context.getString(R.string.tv_title_hint_text)
            binding.frame.constraintLayout.addView(binding.frame.tvsAbove)
            binding.frame.tvAbove.text = title
            binding.frame.tvAbove.setOnClickListener(event)
            binding.frame.tvBelowHint.text = binding.root.context.getString(R.string.tv_note_hint_text)
            binding.frame.constraintLayout.addView(binding.frame.tvsBelow)
            binding.frame.tvBelow.text = note
            binding.frame.tvBelow.setOnClickListener(event)
        }

        if (item[strTagColor] != null) { binding.frame.ivColorTag.setColorFilter(ContextCompat.getColor(binding.root.context, item[strTagColor]!!.toInt())) }
        else { binding.frame.ivColorTag.clearColorFilter() }

        if (item[strEdited] == null) {
            binding.frame.tvCreatedHint.text = null; binding.frame.tvCreated.text = null
            binding.frame.tvCreatedHintHidden.text = binding.root.context.getString(R.string.tv_created_hint_text); binding.frame.tvCreatedHidden.text = dateFormat(item[strCreated]!!.toLong(), binding.root.context)
            binding.frame.tvEditedHint.text = null ;binding.frame.tvEdited.text = null
        } else {
            binding.frame.tvCreatedHint.text = binding.root.context.getString(R.string.tv_created_hint_text); binding.frame.tvCreated.text = dateFormat(item[strCreated]!!.toLong(), binding.root.context)
            binding.frame.tvCreatedHintHidden.text = null; binding.frame.tvCreatedHidden.text = null
            binding.frame.tvEditedHint.text = binding.root.context.getString(R.string.tv_edited_hint_text); binding.frame.tvEdited.text = dateFormat(item[strEdited]!!.toLong(), binding.root.context)
        }

        binding.root.setOnClickListener(event)

        binding.ibDeleteForever.setOnClickListener{
            DeleteConfirmationDialogFragment(recyclerView, mutableListOf(holder.layoutPosition), null, null, fragmentTrashBinding)
                .show(supportFragmentManager, "DeleteConfirmationDialogFragment")
        }

        binding.frame.ibShare.setOnClickListener {
            binding.root.context.startActivity(
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            if (title == "" || note == "") {
                                if (title != "") { binding.root.context.getString(R.string.tv_title_hint_text) + ": $title" } else if (note != "") { binding.root.context.getString(R.string.tv_note_hint_text) + ": $note" } else { binding.root.context.getString(R.string.tv_title_hint_text) + ": " + binding.root.context.getString(R.string.et_title_and_note_hint) + binding.root.context.getString(R.string.tv_note_hint_text) + ": " + binding.root.context.getString(R.string.et_title_and_note_hint)}
                            } else {
                                binding.root.context.getString(R.string.tv_title_hint_text) + ": $title\n" + binding.root.context.getString(R.string.tv_note_hint_text) + ": $note"
                            })
                    }, null))
        }

        binding.tvDelete.text = dateFormat(item[strDelete]!!.toLong(), binding.root.context)

        binding.btnRestore.setOnClickListener {
            dialog?.dismiss()
            Queries(recyclerView).restore(list, holder.layoutPosition, binding.root.context, fragmentTrashBinding) }

        tracker?.let { holder.bind(it.isSelected(getItemId(position))) }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long = list[position]["id"]!!.toLong()
}