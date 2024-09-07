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
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
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

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout,
            binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.hamburgerMenu.setNavigationItemSelectedListener(this)

        binding.bottomnavigationview.background = null
        binding.bottomnavigationview.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_lists -> openFragment(Lists(), "Tasks")
                R.id.ic_analytics -> openFragment(Analytics(),"Analytics")
                R.id.ic_timeline -> openFragment(Timeline(),"Timeline")
                R.id.ic_profile -> openFragment(Profile(),"")
            }
            true
        }

        fragmentManager = supportFragmentManager
        openFragment(Lists(),"Tasks")

        binding.btnAdd.setOnClickListener {
            Toast.makeText(this, "Add New Task", Toast.LENGTH_SHORT).show()
        }

        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.ic_lists -> openFragment(Lists(),"Tasks")
            R.id.ic_timeline -> openFragment(Timeline(), "Timeline")
            R.id.ic_analytics -> openFragment(Analytics(), "Analytics")
            R.id.ic_profile -> openFragment(Profile(), "")
            R.id.ic_contactsupport -> openFragment(ContactSupport(), "Contact Support")
            R.id.ic_logout -> logout() // Handle logout click
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
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
