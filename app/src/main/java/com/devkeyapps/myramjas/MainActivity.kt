package com.devkeyapps.myramjas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.devkeyapps.myramjas.databinding.ActivityMainBinding
import com.devkeyapps.myramjas.prefs.DarkModePrefManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    //View Binding
    private lateinit var binding: ActivityMainBinding

    //Firebase
    private var mAuth: FirebaseAuth? = null
    private var mCurrentUser: FirebaseUser? = null
    private var mUserCollection: CollectionReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (DarkModePrefManager(this).isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Firebase
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = mAuth?.currentUser
        mUserCollection = FirebaseFirestore.getInstance().collection("Users")
        if (mCurrentUser == null) {
            mAuth?.signOut()
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }
}