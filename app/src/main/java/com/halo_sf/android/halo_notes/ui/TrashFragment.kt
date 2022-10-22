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
import com.halo_sf.android.halo_notes.MainActivity.Companion.noteDao
import com.halo_sf.android.halo_notes.NoteLists.Companion.trash
import com.halo_sf.android.halo_notes.customAdapter.TrashAdapter
import com.halo_sf.android.halo_notes.customAdapter.TrashAdapter.Companion.itemViews
import com.halo_sf.android.halo_notes.databinding.FragmentTrashBinding
import com.halo_sf.android.halo_notes.dialog.DeleteConfirmationDialogFragment
import com.halo_sf.android.halo_notes.myDetailsLookup.TrashDetailsLookup

class TrashFragment : Fragment() {

    private var _binding: FragmentTrashBinding? = null
    private val binding get() = _binding!!
    private var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding =  FragmentTrashBinding.inflate(inflater, container, false)

        binding.root.removeView(binding.frameMenuActionModeFragmentTrash)
        if (trash.isNotEmpty()) { binding.root.removeView(binding.tvNoNotesInTrash) }

        binding.ibSearch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.nav_host_fragment, SearchFragment(null, binding))
                .commit()
        }

        if (requireActivity().resources.configuration.smallestScreenWidthDp < 600) {
            binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        } else {
            binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
        val adapter = TrashAdapter(trash, binding.recyclerView, parentFragmentManager, binding)
        binding.recyclerView.adapter = adapter

        val tracker = SelectionTracker.Builder(
            "trash-selection", binding.recyclerView, MyStableIdKeyProvider(binding.recyclerView), TrashDetailsLookup(binding.recyclerView),
            StorageStrategy.createLongStorage()).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                if (tracker.selection.size() >= 1 && actionMode == null) {
                    actionMode = requireActivity().startActionMode(object : ActionMode.Callback {
                        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            binding.root.removeView(binding.frameMenuActionModeFragmentTrash)
                            binding.root.addView(binding.frameMenuActionModeFragmentTrash)

                            binding.ibClear.setOnClickListener {
                                if (tracker.hasSelection()) { tracker.clearSelection() }
                                for (itemView in itemViews) { itemView.isActivated = false }
                            }

                            binding.btnSelectAll.setOnClickListener {
                                for (item in trash) { tracker.select(item["id"]!!.toLong()) }
                                for (itemView in itemViews) { itemView.isActivated = true }
                            }

                            binding.ibShare.setOnClickListener {
                                var text = ""
                                for (id in tracker.selection.toList()) {
                                    val item = noteDao.getItem(id.toInt())
                                    text = if (text != "") { "$text \n\n\n" } else { "" } + if (item.title == "" || item.note == "") {
                                        if (item.title != "") { binding.root.context.getString(R.string.tv_title_hint_text) + ": " + item.title } else if (item.note != "") { binding.root.context.getString(R.string.tv_note_hint_text) + ": " + item.note }
                                        else { binding.root.context.getString(R.string.tv_title_hint_text) + ": " + binding.root.context.getString(R.string.et_title_and_note_hint) + "\n" + binding.root.context.getString(R.string.tv_note_hint_text) + ": " + binding.root.context.getString(R.string.et_title_and_note_hint)}
                                    } else {
                                        binding.root.context.getString(R.string.tv_title_hint_text) + ": " + item.title + "\n" + binding.root.context.getString(R.string.tv_note_hint_text) + ": " + item.note
                                    }
                                }
                                startActivity(
                                    Intent.createChooser(Intent().apply { action = Intent.ACTION_SEND; type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, null))
                            }

                            binding.ibRestore.setOnClickListener {
                                for (id in tracker.selection.toList()) {
                                    val noteItem = noteDao.getItem(id.toInt())
                                    Queries(binding.recyclerView).restore(trash, trash.indexOf(NoteLists.mutableMapOf(id.toString(), noteItem.title, noteItem.note,
                                        noteItem.tagColor, noteItem.created, noteItem.edited, noteItem.delete?.toString())), requireContext(), binding)
                                }
                                mode?.finish()
                            }

                            binding.ibDeleteForever.setOnClickListener {
                                val positions: MutableList<Int> = mutableListOf()
                                for (id in tracker.selection.toList()) {
                                    val trashItem = noteDao.getItem(id.toInt())
                                    positions.add(trash.indexOf(NoteLists.mutableMapOf(
                                        id.toString(), trashItem.title, trashItem.note, trashItem.tagColor,
                                        trashItem.created, trashItem.edited, trashItem.delete?.toString())))
                                }
                                DeleteConfirmationDialogFragment(binding.recyclerView, positions, null, null, binding, mode)
                                    .show(parentFragmentManager, "DeleteConfirmationDialogFragment")
                            }

                            return false
                        }

                        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean = false

                        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

                        override fun onDestroyActionMode(mode: ActionMode?) {
                            actionMode = null
                            if (tracker.hasSelection()) { tracker.clearSelection() }
                            for (itemView in itemViews) { itemView.isActivated = false }
                        }
                    })
                } else if (!tracker.hasSelection()) {
                    binding.root.removeView(binding.frameMenuActionModeFragmentTrash)
                    actionMode?.finish()
                }
            }
        })
        adapter.tracker = tracker

        return binding.root
    }
}