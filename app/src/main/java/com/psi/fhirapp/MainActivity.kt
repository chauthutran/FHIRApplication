package com.psi.fhirapp

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.psi.fhirapp.databinding.ActivityMainBinding

class MainActivity :AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val viewModel: MainActivityViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActionBar()
        initNavigationDrawer()


        viewModel.updateLastSyncTimestamp()

    }

    override fun onBackPressed() {
        if (binding.activityDrawer.isDrawerOpen(GravityCompat.START)) {
            binding.activityDrawer.closeDrawer(GravityCompat.START)
            return
        }
        super.onBackPressed()
    }

    private fun initActionBar() {
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
    }

    private fun initNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected)
        drawerToggle = ActionBarDrawerToggle(this, binding.activityDrawer, R.string.open, R.string.close)
        binding.activityDrawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }


    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sync -> {
                viewModel.triggerOneTimeSync()
                binding.activityDrawer.closeDrawer(GravityCompat.START)
                return false
            }
        }
        return false
    }

}