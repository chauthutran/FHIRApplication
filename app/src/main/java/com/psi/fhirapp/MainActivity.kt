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
        viewModel.updateLastSyncTimestamp()

    }

    override fun onBackPressed() {
        if (binding.activityDrawer.isDrawerOpen(GravityCompat.START)) {
            binding.activityDrawer.closeDrawer(GravityCompat.START)
            return
        }
        super.onBackPressed()
    }

}