package com.muriithi.dekutcallforhelp

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muriithi.dekutcallforhelp.beans.User
import com.muriithi.dekutcallforhelp.components.Authorizer
import com.muriithi.dekutcallforhelp.components.LocationManager

const val LOCATION_PERMISSION_REQUEST_CODE = 100

class MainActivity : AppCompatActivity() {

    private lateinit var authorizer: Authorizer
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate called")

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        authorizer = Authorizer()
        val currentUser = firebaseAuth.currentUser

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Request location permissions if not granted
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            Log.d("MainActivity", "Location permission already granted")
            startLocationManager()
        }

        // Fetch the currently logged-in user
        if (currentUser != null) {
            val databaseReference = firebaseDatabase.child("users").child(currentUser.uid)
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    val displayName = user?.let { "${it.firstName} ${it.lastName}" }
                    Log.d("MainActivity", "User data fetched: $displayName")
                    val snackBar = Snackbar.make(
                        findViewById(R.id.main),
                        "Welcome back, $displayName!",
                        Snackbar.LENGTH_SHORT
                    )
                    snackBar.setAction("Action", null)
                    snackBar.show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("MainActivity", "Failed to read value.", error.toException())
                }
            })
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    Log.d("MainActivity", "Home navigation item selected")
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {
                        authorizer.authorizeAdmin(currentUser.uid) { isAdmin ->
                            if (isAdmin) {
                                loadFragment(AdminHomeFragment())
                            } else {
                                loadFragment(ClientHomeFragment())
                            }
                        }
                    }
                    true
                }

                R.id.navigation_requests -> {
                    Log.d("MainActivity", "Requests navigation item selected")
                    loadFragment(RequestFragment())
                    true
                }

                R.id.navigation_offices -> {
                    Log.d("MainActivity", "Offices navigation item selected")
                    loadFragment(OfficeFragment())
                    true
                }

                else -> false
            }
        }

        // Load the default fragment
        if (savedInstanceState == null) {
            Log.d("MainActivity", "Loading default fragment")
            bottomNavigationView.selectedItemId = R.id.navigation_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        Log.d("MainActivity", "Loading fragment: ${fragment::class.java.simpleName}")
        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("MainActivity", "Location permission granted")
                startLocationManager()
            } else {
                Log.d("MainActivity", "Location permission denied")
            }
        }
    }

    private fun startLocationManager() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            locationManager = LocationManager(this@MainActivity, currentUser.uid)
            locationManager.startTracking()
        }
    }

}