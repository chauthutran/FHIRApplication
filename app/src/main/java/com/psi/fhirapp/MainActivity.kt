package com.psi.fhirapp

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.psi.fhirapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val viewModel: MainActivityViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
//        /* When switching the theme to dark mode. */
//        if (savedInstanceState != null) {
//            this.setTheme(R.style.AppTheme);
//        }

        super.onCreate(savedInstanceState)
//        /* When starting the Activity. */
//        if (savedInstanceState == null) {
//            SplashScreen.installSplashScreen(this);
//        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActionBar()
        initNavigationDrawer()

        viewModel.updateLastSyncTimestamp()

    }


    private fun initActionBar() {
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
    }

    private fun initNavigationDrawer() {
        drawerToggle = ActionBarDrawerToggle(this, binding.activityDrawer, R.string.open, R.string.close)
        binding.activityDrawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }


    fun setDrawerEnabled(enabled: Boolean) {
        val lockMode =
            if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        binding.activityDrawer.setDrawerLockMode(lockMode)

        drawerToggle.isDrawerIndicatorEnabled = enabled
//        drawerToggle.setToolbarNavigationClickListener(view : View.OnClickListener) {
//            override fun onClick(View v) {
//                if (mDrawerIndicatorEnabled) {
//                    toggle();
//                } else if (mToolbarNavigationClickListener != null) {
//                    mToolbarNavigationClickListener.onClick(v);
//                }
//            }
//        }
    }


    fun openNavigationDrawer() {
        binding.activityDrawer.openDrawer(GravityCompat.START)
        viewModel.updateLastSyncTimestamp()
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//
//        val id = item.itemId
//        println("--- Activity - onOptionsItemSelected")
//        //noinspection SimplifiableIfStatement
//        if (id == android.R.id.home) {
//            if (supportFragmentManager.backStackEntryCount > 0) {
//                onBackPressed()
//            } else {
//                drawerLayout.openDrawer(
//                    navigationView
//                )
//            }
//
//            return true
//        }
//
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            android.R.id.home -> {
//                finish()
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        println("=== onOptionsItemSelected 1 ${item.itemId} -  android.R.id.home: ${android.R.id.home}")
//        when (item.itemId) {
//            android.R.id.home -> onBackPressed()
//        }
//        println("=== onOptionsItemSelected 2")
//        return super.onOptionsItemSelected(item)
//    }


//    override fun onBackPressed() {
//        println("=== onBackPressed 1")
//        // Left menus
//        if (binding.activityDrawer.isDrawerOpen(GravityCompat.START)) {
//            binding.activityDrawer.closeDrawer(GravityCompat.START)
//            println("=== onBackPressed 2")
//            return
//        }
//        super.onBackPressed()
////        supportNavigateUp
//    }


}