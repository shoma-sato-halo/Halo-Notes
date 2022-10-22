package com.halo_sf.android.halo_notes.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.halo_sf.android.halo_notes.*
import com.halo_sf.android.halo_notes.NoteLists.Companion.notes
import com.halo_sf.android.halo_notes.customAdapter.CustomAdapter
import com.halo_sf.android.halo_notes.customAdapter.CustomAdapter.Companion.itemViews
import com.halo_sf.android.halo_notes.customAdapter.TrashAdapter
import com.halo_sf.android.halo_notes.databinding.FragmentNotesBinding
import com.halo_sf.android.halo_notes.dialog.DeleteConfirmationDialogFragment
import com.halo_sf.android.halo_notes.dialog.NoteTakingDialogFragment
import com.halo_sf.android.halo_notes.myDetailsLookup.NotesDetailsLookup

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding =  FragmentNotesBinding.inflate(inflater, container, false)

        binding.root.removeView(binding.frameMenuActionModeFragmentNotes)
        if (notes.isNotEmpty()) { binding.root.removeView(binding.tvNoNotes) }

        binding.ibSearch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.nav_host_fragment, SearchFragment(binding, null))
                .commit()
        }

        if (requireActivity().resources.configuration.smallestScreenWidthDp < 600) {
            binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        } else {
            binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        }
        val adapter = CustomAdapter(notes, binding.recyclerView, parentFragmentManager, binding)
        binding.recyclerView.adapter = adapter

        val tracker = SelectionTracker.Builder(
            "notes-selection", binding.recyclerView, MyStableIdKeyProvider(binding.recyclerView), NotesDetailsLookup(binding.recyclerView),
            StorageStrategy.createLongStorage()).withSelectionPredicate(SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                if (tracker.selection.size() >= 1 && actionMode == null) {
                    actionMode = requireActivity().startActionMode(object : ActionMode.Callback {
                        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            binding.root.removeView(binding.frameMenuActionModeFragmentNotes)
                            binding.root.addView(binding.frameMenuActionModeFragmentNotes)

                            binding.ibClear.setOnClickListener {
                                for (itemView in itemViews) { itemView.isActivated = false }
                                if (tracker.hasSelection()) { tracker.clearSelection() }
                            }

                            binding.btnSelectAll.setOnClickListener {
                                for (note in notes) { tracker.select(note["id"]!!.toLong()) }
                                for (itemView in itemViews) { itemView.isActivated = true }
                            }

                            binding.ibDelete.setOnClickListener {
                                for (id in tracker.selection.toList()) {
                                    val noteItem = MainActivity.noteDao.getItem(id.toInt())
                                    Queries(binding.recyclerView).delete(notes, notes.indexOf(NoteLists.mutableMapOf(id.toString(), noteItem.title, noteItem.note,
                                        noteItem.tagColor, noteItem.created, noteItem.edited, noteItem.delete?.toString())), requireContext(), binding)
                                }
                                mode?.finish()
                            }

                            binding.ibDeleteForever.setOnClickListener {
                                val positions: MutableList<Int> = mutableListOf()
                                for (id in tracker.selection.toList()) {
                                    val noteItem = MainActivity.noteDao.getItem(id.toInt())
                                    positions.add(notes.indexOf(NoteLists.mutableMapOf(id.toString(),
                                        noteItem.title, noteItem.note, noteItem.tagColor, noteItem.created, noteItem.edited, noteItem.delete?.toString())))
                                }
                                DeleteConfirmationDialogFragment(binding.recyclerView, positions, null, binding, null, mode)
                                    .show(parentFragmentManager, "DeleteConfirmationDialogFragment")
                            }

                            binding.ibShare.setOnClickListener {
                                var text = ""
                                for (id in tracker.selection.toList()) {
                                    val noteItem = MainActivity.noteDao.getItem(id.toInt())
                                    text = if (text != "") { "$text \n\n\n" } else { "" } + if (noteItem.title == "" || noteItem.note == "") {
                                        if (noteItem.title != "") { binding.root.context.getString(R.string.tv_title_hint_text) + ": " + noteItem.title } else if (noteItem.note != "") { binding.root.context.getString(R.string.tv_note_hint_text) + ": " + noteItem.note }
                                        else { binding.root.context.getString(R.string.tv_title_hint_text) + ": " + binding.root.context.getString(R.string.et_title_and_note_hint) + "\n" + binding.root.context.getString(R.string.tv_note_hint_text) + ": " + binding.root.context.getString(R.string.et_title_and_note_hint)}
                                    } else {
                                        binding.root.context.getString(R.string.tv_title_hint_text) + ": " + noteItem.title + "\n" + binding.root.context.getString(R.string.tv_note_hint_text) + ": " + noteItem.note
                                    }
                                }
                                startActivity(
                                    Intent.createChooser(Intent().apply { action = Intent.ACTION_SEND; type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, null))
                            }

                            return false
                        }

                        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean = false

                        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

                        override fun onDestroyActionMode(mode: ActionMode?) {
                            actionMode = null
                            if (tracker.hasSelection()) { tracker.clearSelection() }
                            for (itemView in TrashAdapter.itemViews) { itemView.isActivated = false }
                        }
                    })
                } else if (!tracker.hasSelection()) {
                    binding.root.removeView(binding.frameMenuActionModeFragmentNotes)
                    actionMode?.finish()
                }
            }
        })
        adapter.tracker = tracker

        binding.fabAdd.setOnClickListener { NoteTakingDialogFragment(binding.recyclerView, binding).show(parentFragmentManager, "NoteTakingDialog") }

        return binding.root
    }
}