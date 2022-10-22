package com.halo_sf.android.halo_notes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.google.android.gms.ads.AdRequest
import com.halo_sf.android.halo_notes.NoteLists.Companion.notes
import com.halo_sf.android.halo_notes.NoteLists.Companion.strCreated
import com.halo_sf.android.halo_notes.NoteLists.Companion.strDelete
import com.halo_sf.android.halo_notes.NoteLists.Companion.strEdited
import com.halo_sf.android.halo_notes.NoteLists.Companion.strId
import com.halo_sf.android.halo_notes.NoteLists.Companion.strNote
import com.halo_sf.android.halo_notes.NoteLists.Companion.strTagColor
import com.halo_sf.android.halo_notes.NoteLists.Companion.strTitle
import com.halo_sf.android.halo_notes.NoteLists.Companion.trash
import com.halo_sf.android.halo_notes.databinding.ActivityMainBinding
import com.halo_sf.android.halo_notes.room.NoteDao
import com.halo_sf.android.halo_notes.room.NoteDatabase
import com.halo_sf.android.halo_notes.ui.NotesFragment
import com.halo_sf.android.halo_notes.ui.SettingsFragment
import com.halo_sf.android.halo_notes.ui.TrashFragment

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object { lateinit var noteDao: NoteDao }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.adView.loadAd(AdRequest.Builder().build())

        noteDao = Room.databaseBuilder(this, NoteDatabase::class.java, "NoteDatabase").allowMainThreadQueries().build().noteDao()

        InsertingInitialData(this).insertInitialData()

        var id = noteDao.getFirstRecordId()
        notes.clear(); trash.clear()
        while (id != null) {
            val note = noteDao.getItem(id)
            if (note.delete == null ) {
                notes.add(mutableMapOf(Pair(strId, id.toString()), Pair(strTitle, note.title), Pair(strNote, note.note),
                    Pair(strTagColor, note.tagColor), Pair(strCreated, note.created), Pair(strEdited, note.edited), Pair(strDelete, null)))
            } else {
                trash.add(mutableMapOf(Pair(strId, id.toString()), Pair(strTitle, note.title), Pair(strNote, note.note),
                    Pair(strTagColor, note.tagColor), Pair(strCreated, note.created), Pair(strEdited, note.edited), Pair(strDelete, note.delete.toString())))
            }
            id = noteDao.getNextId(id)
        }

        binding.ibPrivacyPolicy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shoma-sato-halo.github.io/halo-notes-privacy-policy/")))
        }

        binding.ibTwitter.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.twitter_url)))) }

        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!.findNavController()
        binding.ibMenu.setOnClickListener { binding.drawerLayout.open() }
        binding.navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener {_, destination, _ ->
            when (destination.id) {
                R.id.nav_notes -> {
                    binding.toolbar.setTitleTextAppearance(this, R.style.ToolbarTextAppearanceForNotesFragment); binding.toolbar.title = getString(R.string.app_name)
                }
                R.id.nav_trash -> {
                    binding.toolbar.setTitleTextAppearance(this, R.style.ToolbarTextAppearanceForOtherThanNotesFragment); binding.toolbar.title = getString(R.string.menu_trash)
                }
                else -> {
                    binding.toolbar.setTitleTextAppearance(this, R.style.ToolbarTextAppearanceForOtherThanNotesFragment); binding.toolbar.title = getString(R.string.menu_settings)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}