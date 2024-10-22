package com.example.puttask

import com.example.puttask.fragments.Profile
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.puttask.api.DataManager
import com.example.puttask.authentications.LoginSignin
import com.example.puttask.databinding.ActivityMainBinding
import com.example.puttask.fragments.*
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dataManager: DataManager
    private lateinit var pulseAnimation: Animation

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                // You can add additional back pressed behavior here
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize DataManager
        dataManager = DataManager(this)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Setup hamburger menu button
        binding.hamburgerIcon.setOnClickListener {
            with(binding.drawerLayout) {
                if (isDrawerOpen(GravityCompat.END)) closeDrawer(GravityCompat.END) else openDrawer(GravityCompat.END)
            }
        }

        // Setup NavigationView listener
        binding.hamburgerMenu.setNavigationItemSelectedListener(this)

        // Setup Bottom Navigation
        setupBottomNavigation()
        openFragment(Lists(), "Tasks")

        // Load and apply the pulse animation to the FAB
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation_fab).apply {
            repeatCount = Animation.INFINITE // Loop the animation
        }
        binding.btnAdd.startAnimation(pulseAnimation) // Start the animation

        // Handle FAB click for adding a task
        binding.btnAdd.setOnClickListener {
            // Stop the pulse animation once the FAB is clicked (assuming the user is adding a task)
            binding.btnAdd.clearAnimation()

            // Start the AddTask2 activity
            startActivity(Intent(this, AddTask2::class.java))
        }

        // Add back press callback
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    private fun setupBottomNavigation() {
        binding.bottomnavigationview.background = null
        binding.bottomnavigationview.setOnItemSelectedListener { item ->
            val fragmentData = when (item.itemId) {
                R.id.ic_lists -> Lists() to "Tasks"
                R.id.ic_analytics -> Analytics() to "Analytics"
                R.id.ic_timeline -> Timeline() to "Timeline"
                R.id.ic_profile -> Profile() to "Profile"
                else -> null
            }
            fragmentData?.let { openFragment(it.first, it.second) }
            true
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragmentData = when (item.itemId) {
            R.id.ic_lists -> Lists() to "Tasks"
            R.id.ic_timeline -> Timeline() to "Timeline"
            R.id.ic_analytics -> Analytics() to "Analytics"
            R.id.ic_profile -> Profile() to "Profile"
            R.id.ic_contactsupport -> ContactSupport() to "Contact Support"
            R.id.ic_logout -> {
                logout()
                return true
            }
            else -> null
        }
        fragmentData?.let { openFragment(it.first, it.second) }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    private fun logout() {
        dataManager.clearLoginData() // Clear login data
        startActivity(Intent(this, LoginSignin::class.java))
        finish()
    }

    private fun openFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.flfragment, fragment)
            .commit()

        binding.toolbarTitle.text = title
        binding.dateTextView.text = SimpleDateFormat("d MMMM", Locale.getDefault()).format(Date())
    }
}
