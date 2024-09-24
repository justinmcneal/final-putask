package com.example.puttask

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.puttask.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) //Disable the default app title to display a custom title depending on the clicked activity


        // Manually control the inputted customized hamburger icon click
        binding.hamburgerIcon.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END) // Close the drawer if it's open
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.END) // Open the drawer when clicked
            }
        }

        binding.hamburgerMenu.setNavigationItemSelectedListener(this)

        binding.bottomnavigationview.background = null
        binding.bottomnavigationview.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_lists -> openFragment(Lists(), "Lists")
                R.id.ic_timeline -> openFragment(Timeline(),"Timeline")
                R.id.ic_analytics -> openFragment(Analytics(),"Analytics")
                R.id.ic_profile -> openFragment(Profile(),"Profile")
            }
            true
        }

        fragmentManager = supportFragmentManager
        openFragment(Lists(),"Lists")

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddTask2::class.java)//plain activity gamit here
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.ic_lists -> openFragment(Lists(),"Lists")
            R.id.ic_timeline -> openFragment(Timeline(), "Timeline")
            R.id.ic_analytics -> openFragment(Analytics(), "Analytics")
            R.id.ic_profile -> openFragment(Profile(), "Profile")
            R.id.ic_contactsupport -> openFragment(ContactSupport(), "Contact Support")
            R.id.ic_logout -> logout() // Handle logout click
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    private fun logout() {
        // Clear user session or perform any logout logic here
        // Then redirect to the LogInSignIn activity
        val intent = Intent(this, LoginSignin::class.java)
        startActivity(intent)
        finish() // Optional: finish the current activity so the user cannot return to it
    }

    private fun openFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flfragment, fragment)
            commit()
        }
        // Set the fragment title based on the clicked activity
        binding.toolbarTitle.text = title

        // Set the accurate date on the toolbar on top of the title
        val dateFormat = SimpleDateFormat("d MMMM", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        binding.dateTextView.text = currentDate
    }
}
