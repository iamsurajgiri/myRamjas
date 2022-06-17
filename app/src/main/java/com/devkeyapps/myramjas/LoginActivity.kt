package com.devkeyapps.myramjas

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.devkeyapps.myramjas.databinding.ActivityLoginBinding
import com.devkeyapps.myramjas.databinding.ProcessingDialogBinding
import com.devkeyapps.myramjas.utils.compressBitmap
import com.devkeyapps.myramjas.utils.toBitmap
import com.devkeyapps.myramjas.utils.toUri
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
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private var currentStep = 0

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            uriContent?.let { handleSendImage(it) }

        } else {
            binding.saveBtn.isEnabled = true
            binding.profileImageView.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }

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
            setStepsNumber(3)
            go(0,true)
        }.setStepsNumber(3)
        binding.layout1.visibility = View.VISIBLE
        binding.phonenumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (!editable.toString().startsWith("+91")) {
                    binding.phonenumber.setText("+91")
                    binding.phonenumber.text
                        ?.let { Selection.setSelection(binding.phonenumber.text, it.length) }
                }
            }
        })

        binding.submit1.setOnClickListener {
            val phoneNumber = binding.phonenumber.text.toString()
            binding.phonenumberText.text = phoneNumber
            binding.phonenumber.setText("+91")
            binding.phonenumber.text?.let { it1 -> Selection.setSelection(binding.phonenumber.text, it1.length) }
            if (TextUtils.isEmpty(phoneNumber)) {
                binding.phonenumber.error = "Enter a Phone Number"
                binding.phonenumber.requestFocus()
            } else if (phoneNumber.length < 13 || phoneNumber.length > 13) {
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
                    PhoneAuthProvider.getCredential(mVerificationId!!, verificationCode)
                signInWithPhoneAuthCredential(credential)
            }
        }


        //Firebase
        myRef = FirebaseFirestore.getInstance().collection("Users")
        mUserStorage = FirebaseStorage.getInstance().reference.child("Profile")


        mProgress = ProgressDialog(this)

        binding.saveBtn.setOnClickListener {
            binding.saveBtn.isEnabled = false
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    binding.saveBtn.isEnabled = true
                    Toast.makeText(
                        applicationContext,
                        task.exception?.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                startSaving(token)

                // Log and toast
                Log.d(TAG, token)
            })

        }

        binding.profileImageView.setOnClickListener {
            binding.saveBtn.isEnabled = false
            binding.profileImageView.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            if (ContextCompat.checkSelfPermission(
                    this@LoginActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@LoginActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    21
                )
            } else {
                bringImagePicker()
            }

        }

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
                            if (currentStep < binding.stepView.stepCount - 1) {
                                currentStep++
                                binding.stepView.go(currentStep, true)
                            } else {
                                binding.stepView.done(true)
                            }
                            binding.layout1.visibility = View.GONE
                            binding.layout2.visibility = View.GONE
                            mCurrentUser = mAuth!!.currentUser!!
                            mUserId = userId
                            binding.phone.setText(mCurrentUser.phoneNumber)
                            binding.phone.isEnabled = false
                            binding.layout3.visibility = View.VISIBLE
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


    //Profile Creation Methods
    private fun startSaving(deviceId: String) {
        mProgress!!.setMessage("Saving....")
        mProgress!!.setCanceledOnTouchOutside(false)
        mProgress!!.show()
        if (binding.name.text != null && binding.phone.text != null) {
            val names = binding.name.text.toString().trim { it <= ' ' }
            val phones = binding.phone.text.toString().trim { it <= ' ' }
            if (names.isNotEmpty() && phones.isNotEmpty()) {
                val profileMap: MutableMap<String, Any> =
                    HashMap()
                profileMap["name"] = names
                profileMap["phone"] = phones
                profileMap["deviceId"] = deviceId
                if (mImageUri != null) {
                    profileMap["image"] = mImageUri.toString()
                } else {
                    val defaultLink =
                        "https://firebasestorage.googleapis.com/v0/b/newsage-c4ed5.appspot.com/o/facebook.png?alt=media&token=f6f80d2e-f04b-4651-8ea6-db55fd33dff4"
                    profileMap["image"] = defaultLink
                }
                myRef!!.document(mUserId!!).set(profileMap)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Successfully Saved!",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (currentStep < binding.stepView.stepCount - 1) {
                            currentStep++
                            binding.stepView.go(currentStep, true)
                        } else {
                            binding.stepView.done(true)
                        }
                        mProgress!!.dismiss()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    }.addOnFailureListener { e ->
                        Log.i("PROFILETAG : ", e.toString())
                        binding.saveBtn.isEnabled = true
                        val error = e.message
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        mProgress!!.dismiss()
                    }
            } else {
                binding.saveBtn.isEnabled = true
                Toast.makeText(
                    applicationContext,
                    "Please fill up all the fields",
                    Toast.LENGTH_SHORT
                ).show()
                mProgress!!.dismiss()
            }
        } else {
            binding.saveBtn.isEnabled = true
            Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT)
                .show()
            mProgress!!.dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 21) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                bringImagePicker()
            } else {
                binding.saveBtn.isEnabled = false
                binding.profileImageView.isEnabled = true
                binding.progressBar.visibility = View.INVISIBLE
                Toast.makeText(
                    this,
                    "Storage Permission Required!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }




    private fun handleSendImage(resultUri: Uri) {
        binding.profileImageView.setImageURI(resultUri)
        val newUri = resultUri.toBitmap(this).compressBitmap(5).toUri(this)

        startThumbnailing(newUri)

    }


    //Start Reducing size of image by converting it into Thumbnails
    private fun startThumbnailing(
        uri: Uri?
    ) {
        val thumbFilepath = mUserStorage!!.child(mUserId!!).child("$mUserId.jpg")
        val uploadTask: UploadTask = thumbFilepath.putFile(uri!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            Log.i("LOCALLY", thumbFilepath.downloadUrl.toString())
            thumbFilepath.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mImageUri = task.result
            } else {
                Toast.makeText(
                    applicationContext,
                    task.exception?.localizedMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
            binding.saveBtn.isEnabled = true
            binding.profileImageView.isEnabled = true
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    //Opens Gallery to choose image
    private fun bringImagePicker() {
        cropImage.launch(
            options {
                setGuidelines(CropImageView.Guidelines.ON)
                setCropShape(CropImageView.CropShape.OVAL)
                setAspectRatio(1,1)
                setActivityTitle("Profile Picture")
                setMaxZoom(4)

            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    companion object {

        private const val TAG = "FirebasePhoneNumAuth"
    }
}