package delhi.university.ramjas.college.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import delhi.university.ramjas.college.databinding.ActivityMainBinding
import delhi.university.ramjas.college.prefs.DarkModePrefManager

class MainActivity : AppCompatActivity() {

    //View Binding
    private lateinit var binding: ActivityMainBinding

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mCurrentUser: FirebaseUser? = null
    private lateinit var mUserCollection: CollectionReference

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