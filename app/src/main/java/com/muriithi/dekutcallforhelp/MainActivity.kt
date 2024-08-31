// app/src/main/java/com/muriithi/dekutcallforhelp/MainActivity.kt
package com.muriithi.dekutcallforhelp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muriithi.dekutcallforhelp.beans.User
import com.muriithi.dekutcallforhelp.components.Authorizer

class MainActivity : AppCompatActivity() {

    private lateinit var authorizer: Authorizer
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Fetch the currently logged-in user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val ref = database.child("users").child(currentUser.uid)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    val displayName = user?.let { "${it.firstName} ${it.lastName}" }
                    val snackBar = Snackbar.make(
                        findViewById(R.id.main),
                        "Welcome back, $displayName!",
                        Snackbar.LENGTH_SHORT
                    )
                    snackBar.setAction("Action", null)
                    snackBar.show()

                    if (user != null) {
                        authorizer = Authorizer()

                        // Hide Requests menu item if the user is not a superuser
                        if (!authorizer.authorizeAdmin(user)) {
                            bottomNavigationView.menu.findItem(R.id.navigation_requests).isVisible = false
                            loadFragment(ClientHomeFragment())
                        } else {
                            loadFragment(AdminHomeFragment())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("MainActivity", "Failed to read value.", error.toException())
                }
            })
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(AdminHomeFragment())
                    true
                }

                R.id.navigation_offices -> {
                    loadFragment(OfficeFragment())
                    true
                }

                R.id.navigation_requests -> {
                    loadFragment(RequestFragment())
                    true
                }

                R.id.navigation_search -> {
                    loadFragment(SearchFragment())
                    true
                }

                else -> false
            }
        }

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}