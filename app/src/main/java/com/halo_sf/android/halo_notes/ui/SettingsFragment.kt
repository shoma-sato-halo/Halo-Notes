package com.halo_sf.android.halo_notes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.halo_sf.android.halo_notes.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.spReceiveSettings.setSelection(
            requireContext().getSharedPreferences("holo_notes", AppCompatActivity.MODE_PRIVATE).getInt("receive_settings", 0))
        binding.spReceiveSettings.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {  }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                requireContext().getSharedPreferences("holo_notes", AppCompatActivity.MODE_PRIVATE).edit().putInt("receive_settings", id.toInt())
                    .apply()
            }
        }

        return binding.root
    }
}