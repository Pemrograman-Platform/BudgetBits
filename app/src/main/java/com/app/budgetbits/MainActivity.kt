package com.app.budgetbits

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.budgetbits.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), false)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment(), false)
                R.id.nav_history -> replaceFragment(HistoryFragment(), false)
                R.id.nav_profile -> replaceFragment(ProfileFragment(), false)
            }
            true
        }
    }

    fun replaceFragment(fragment: Fragment, isAppended: Boolean = false) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        
        // Menggunakan ID yang benar dari activity_main.xml
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        
        if (isAppended) {
            fragmentTransaction.addToBackStack(null)
        }
        
        fragmentTransaction.commit()
    }
}