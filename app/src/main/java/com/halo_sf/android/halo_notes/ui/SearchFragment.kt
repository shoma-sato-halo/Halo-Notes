package com.halo_sf.android.halo_notes.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.halo_sf.android.halo_notes.NoteLists.Companion.notes
import com.halo_sf.android.halo_notes.NoteLists.Companion.trash
import com.halo_sf.android.halo_notes.customAdapter.CustomAdapter
import com.halo_sf.android.halo_notes.customAdapter.TrashAdapter
import com.halo_sf.android.halo_notes.databinding.FragmentNotesBinding
import com.halo_sf.android.halo_notes.databinding.FragmentSearchBinding
import com.halo_sf.android.halo_notes.databinding.FragmentTrashBinding

class SearchFragment(private val fragmentNotesBinding: FragmentNotesBinding?, private val fragmentTrashBinding: FragmentTrashBinding?) : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.ibBack.setOnClickListener { parentFragmentManager.popBackStack() }

        val list = if (fragmentNotesBinding != null) { notes } else { trash }

        fun search() {
            val keyword = binding.etSearch.text.toString()
            if (keyword != "") {
                val filteredList = list.filter{ it.values.toString().contains(keyword) } as MutableList<MutableMap<String, String?>>
                if (requireActivity().resources.configuration.smallestScreenWidthDp < 600) {
                    binding.recyclerView.layoutManager = LinearLayoutManager(activity)
                } else {
                    binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                }
                binding.recyclerView.adapter = if (fragmentNotesBinding != null) {
                    CustomAdapter(filteredList, binding.recyclerView, parentFragmentManager, fragmentNotesBinding)
                } else {
                    TrashAdapter(filteredList, binding.recyclerView, parentFragmentManager, fragmentTrashBinding!!)
                }
            }
        }

        binding.ibSearch.setOnClickListener { search() }
        binding.etSearch.setOnEditorActionListener { _, _, _ -> search(); true }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.etSearch.requestFocus()
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(binding.etSearch, 0)
    }
}