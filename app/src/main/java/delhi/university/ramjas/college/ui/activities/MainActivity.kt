package delhi.university.ramjas.college.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import delhi.university.ramjas.college.R
import delhi.university.ramjas.college.databinding.ActivityMainBinding
import delhi.university.ramjas.college.prefs.DarkModePrefManager
import delhi.university.ramjas.college.ui.fragments.AcademicsFragment
import delhi.university.ramjas.college.ui.fragments.EventsFragment
import delhi.university.ramjas.college.ui.fragments.HomeFragment
import delhi.university.ramjas.college.ui.fragments.OthersFragment

class MainActivity : AppCompatActivity() {

    //View Binding
    private lateinit var binding: ActivityMainBinding

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mCurrentUser: FirebaseUser? = null
    private lateinit var mUserCollection: CollectionReference


    //Fragments
    private lateinit var homeFragment: HomeFragment
    private lateinit var eventsFragment: EventsFragment
    private lateinit var academicsFragment: AcademicsFragment
    private lateinit var othersFragment: OthersFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        //Firebase
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = mAuth.currentUser
        mUserCollection = FirebaseFirestore.getInstance().collection("College").document("RamjasCollege")
            .collection("Users")
        if (mCurrentUser == null) {
            mAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }else{
            checkSetup()
        }

        if (DarkModePrefManager(this).isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.mainBottomNav.background = null
        binding.mainBottomNav.menu.getItem(2).isEnabled = false
        homeFragment = HomeFragment()
        eventsFragment = EventsFragment()
        academicsFragment = AcademicsFragment()
        othersFragment = OthersFragment()

        binding.mainBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homes -> {
                    replaceFragment(homeFragment)
                    true
                }
                R.id.events -> {
                    replaceFragment(eventsFragment)
                    true
                }
                R.id.academics -> {
                    replaceFragment(academicsFragment)
                    true
                }
                R.id.others -> {
                    replaceFragment(othersFragment)
                    true
                }
                else -> false
            }
        }
        binding.mainBottomNav.selectedItemId = R.id.homes

    }

    //Sets fragment
    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction =
            supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_container, fragment)
        fragmentTransaction.commit()
    }

    //checking is user is setup or not
    private fun checkSetup() {
        val userId = mCurrentUser!!.uid
        mUserCollection.document(userId).get()
            .addOnCompleteListener { task ->
                val documentSnapshot = task.result
                if (task.isSuccessful) {
                    if (documentSnapshot != null) {
                        if (!documentSnapshot.exists()) {
                            val intent = Intent(
                                applicationContext,
                                SetupActivity::class.java
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
    }

}