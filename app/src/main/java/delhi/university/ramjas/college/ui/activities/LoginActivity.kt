package delhi.university.ramjas.college.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import delhi.university.ramjas.college.databinding.ActivityLoginBinding
import delhi.university.ramjas.college.databinding.ProcessingDialogBinding
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private var currentStep = 0


    var dialogVerifying: AlertDialog? = null
    private var mCallbacks: OnVerificationStateChangedCallbacks? = null
    private var mUserCollection: CollectionReference? = null
    private var mVerificationId: String? = null
    private var mResendToken: ForceResendingToken? = null
    private var mAuth: FirebaseAuth? = null
    private lateinit var mCurrentUser: FirebaseUser
    private var firebaseAuth: FirebaseAuth? = null

    //Profile Creation
    private var mImageUri: Uri? = null

    private var mUserId: String? = null
    private var myRef: CollectionReference? = null
    private var mUserStorage: StorageReference? = null

    private var mProgress: ProgressDialog? = null

    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Toolbar

        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.title =null
        }
        mAuth = FirebaseAuth.getInstance()


        //Firebase
        mUserCollection = FirebaseFirestore.getInstance().collection("Users")

        firebaseAuth = FirebaseAuth.getInstance()

        binding.stepView.apply {
            setStepsNumber(2)
            go(0,true)
        }.setStepsNumber(2)
        binding.layout1.visibility = View.VISIBLE

        binding.submit1.setOnClickListener {
            val phoneNumber = "+91${binding.phonenumber.text.toString()}"
            if (TextUtils.isEmpty(phoneNumber)) {
                binding.phonenumber.error = "Enter a Phone Number"
                binding.phonenumber.requestFocus()
            } else if (phoneNumber.length !=13) {
                binding.phonenumber.error = "Please enter a valid phone"
                binding.phonenumber.requestFocus()
            } else {
                if (currentStep < binding.stepView.stepCount - 1) {
                    currentStep++
                    binding.stepView.go(currentStep, true)
                } else {
                    binding.stepView.done(true)
                }
                binding.layout1.visibility = View.GONE
                binding.layout2.visibility = View.VISIBLE
                binding.pinView.requestFocus()
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,  // Phone number to verify
                    60,  // Timeout duration
                    TimeUnit.SECONDS,  // Unit of timeout
                    this@LoginActivity,  // Activity (for callback binding)
                    mCallbacks!!
                ) // OnVerificationStateChangedCallbacks
            }
        }
        mCallbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val alertLayout =
                    ProcessingDialogBinding.inflate(layoutInflater).root
                val show =
                    AlertDialog.Builder(this@LoginActivity)
                show.setView(alertLayout)
                show.setCancelable(false)
                dialogVerifying = show.create()
                dialogVerifying!!.show()
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: ForceResendingToken
            ) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

                // ...
            }
        }
        binding.submit2.setOnClickListener {
            binding.submit2.isEnabled = false
            val verificationCode = binding.pinView.text.toString()
            if (verificationCode.isEmpty() || verificationCode.length != 6) {
                Toast.makeText(
                    this@LoginActivity,
                    "Enter verification code",
                    Toast.LENGTH_SHORT
                ).show()
                binding.submit2.isEnabled = true
            } else {
                val alertLayout =
                    ProcessingDialogBinding.inflate(layoutInflater).root
                val show =
                    AlertDialog.Builder(this@LoginActivity)
                show.setView(alertLayout)
                show.setCancelable(false)
                dialogVerifying = show.create()
                dialogVerifying!!.show()
                binding.submit2.isEnabled = false
                val credential =
                    mVerificationId?.let { it1 -> PhoneAuthProvider.getCredential(it1, verificationCode) }
                if (credential != null) {
                    signInWithPhoneAuthCredential(credential)
                }else{
                    dialogVerifying?.dismiss()
                    Toast.makeText(this, "Something went wrong! Retry", Toast.LENGTH_SHORT).show()
                }
            }
        }


        //Firebase
        myRef = FirebaseFirestore.getInstance().collection("Users")
        mUserStorage = FirebaseStorage.getInstance().reference.child("Profile")


        mProgress = ProgressDialog(this)



    }

    //Check if user exist in database or not
    private fun checkUserExist() {
        if (mAuth!!.currentUser != null) {
            val userId = mAuth!!.currentUser!!.uid

            mUserCollection!!.document(userId).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documentSnapshot = task.result
                        if (documentSnapshot!!.exists()) {
                            dialogVerifying!!.dismiss()
                            val uploadbat =
                                Intent(applicationContext, MainActivity::class.java)
                            uploadbat.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(uploadbat)
                            finish()
                        } else {
                            dialogVerifying!!.dismiss()
                            startActivity(Intent(applicationContext, SetupActivity::class.java))
                            finish()
                        }
                    } else {
                        dialogVerifying!!.dismiss()
                        Toast.makeText(
                            this@LoginActivity,
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }


        } else {
            dialogVerifying!!.dismiss()
            Toast.makeText(this@LoginActivity, "Something went wrong", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(
                        TAG,
                        "signInWithCredential:success"
                    )
                    checkUserExist()
                } else {
                    dialogVerifying!!.dismiss()
                    binding.submit2.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                    Log.w(
                        TAG,
                        "signInWithCredential:failure",
                        task.exception
                    )
                    if (task.exception !is FirebaseAuthInvalidCredentialsException) {
                        Log.e(TAG, task.exception.toString())
                    }
                }
            }
    }








    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    companion object {

        private const val TAG = "FirebasePhoneNumAuth"
    }
}