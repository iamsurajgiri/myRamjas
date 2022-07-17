package delhi.university.ramjas.college.ui.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import delhi.university.ramjas.college.R
import delhi.university.ramjas.college.customviews.ProgressDialog
import delhi.university.ramjas.college.databinding.FragmentSelfBinding
import delhi.university.ramjas.college.parcels.SetupParcel
import delhi.university.ramjas.college.ui.activities.MainActivity
import delhi.university.ramjas.college.ui.activities.SetupActivity
import delhi.university.ramjas.college.utils.snack


class SelfFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var setupParcel: SetupParcel
    private lateinit var mAuth: FirebaseAuth
    private var mCurrentUser: FirebaseUser? = null

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            binding.profileImageView.setImageURI(uriContent)
            uriContent?.let { startThumbnailing(it) }

        } else {
            binding.saveBtn.isEnabled = true
            binding.profileImageView.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }

    private val multiPermissionCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isEnabled ->
            if (isEnabled) {
                bringImagePicker()
            } else {
                Toast.makeText(mContext, "Please provide the permission", Toast.LENGTH_SHORT)
                    .show()
                requestAllPermissions()
            }

        }
    private var _binding: FragmentSelfBinding? = null
    private var mUserCollection: CollectionReference? = null


    //Profile Creation
    private var mImageUri: Uri? = null

    private var mUserId: String? = null
    private var mUserStorage: StorageReference? = null

    private lateinit var mProgress: ProgressDialog
    // mContext property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Firebase
        mUserCollection =
            FirebaseFirestore.getInstance().collection("College").document("RamjasCollege")
                .collection("Users")
        mUserStorage = FirebaseStorage.getInstance().reference.child("Profile")
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = mAuth.currentUser!!


        val temp: SetupParcel? = arguments?.getParcelable("setupParcel")
        if (temp != null) setupParcel = temp
        else Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show()


        Log.i("SelfFragment", "onCreate: $setupParcel")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for mContext fragment
        _binding = FragmentSelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mProgress = ProgressDialog(mContext, false)
        if (mCurrentUser!=null)
        binding.phone.setText(mCurrentUser?.phoneNumber)
        binding.phone.isEnabled = false
        binding.profileImageView.setOnClickListener {
            binding.saveBtn.isEnabled = false
            binding.profileImageView.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            if (ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                multiPermissionCallback.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } else {
                bringImagePicker()
            }

        }

        binding.saveBtn.setOnClickListener {
            binding.saveBtn.isEnabled = false
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(SelfFragment::javaClass.name, "Fetching FCM registration token failed", task.exception)
                    binding.saveBtn.isEnabled = true
                    Toast.makeText(
                        mContext,
                        task.exception?.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                startSaving(token)

                // Log and toast
                Log.d(SelfFragment::javaClass.name, token)
            })

        }
    }
    //Start Reducing size of image by converting it into Thumbnails
    private fun startThumbnailing(
        uri: Uri?
    ) {
        if (mAuth.currentUser != null) {
            val userId = mAuth.currentUser!!.uid
            mUserId = userId
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
                        mContext,
                        task.exception?.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
                binding.saveBtn.isEnabled = true
                binding.profileImageView.isEnabled = true
                binding.progressBar.visibility = View.INVISIBLE
            }
        }else{
            binding.root.snack("Something went wrong!", mContext)
        }
    }

    private fun requestAllPermissions() {
        multiPermissionCallback.launch(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
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
                setOutputCompressQuality(10)

            }
        )
    }

    //Profile Creation Methods
    private fun startSaving(deviceId: String) {
        mProgress.setMessage("Saving....")
        mProgress.setCanceledOnTouchOutside(false)
        mProgress.show()
        if (binding.name.text != null && binding.phone.text != null) {
            val names = binding.name.text.toString().trim { it <= ' ' }
            val phones = binding.phone.text.toString().trim { it <= ' ' }
            if (names.isNotEmpty() && phones.isNotEmpty()) {
                setupParcel.name = names
                setupParcel.phone = phones
                setupParcel.deviceId = deviceId

                if (mImageUri != null) {
                    setupParcel.image = mImageUri.toString()
                } else {
                    val defaultLink =
                        "https://firebasestorage.googleapis.com/v0/b/newsage-c4ed5.appspot.com/o/facebook.png?alt=media&token=f6f80d2e-f04b-4651-8ea6-db55fd33dff4"
                    setupParcel.image = defaultLink
                }
                mProgress.dismiss()
                val bundle = bundleOf(
                    "setupParcel" to setupParcel
                )
                findNavController().navigate(R.id.action_selfFragment_to_setOneFragment, bundle)

            } else {
                binding.saveBtn.isEnabled = true
                Toast.makeText(
                    mContext,
                    "Please fill up all the fields",
                    Toast.LENGTH_SHORT
                ).show()
                mProgress.dismiss()
            }
        } else {
            binding.saveBtn.isEnabled = true
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT)
                .show()
            mProgress.dismiss()
        }
    }

    //Check if user exist in database or not
    private fun checkUserExist() {
        if (mCurrentUser != null) {
            val userId = mCurrentUser?.uid
            if (userId != null) {
                mUserCollection?.document(userId)?.get()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (documentSnapshot!!.exists()) {
                                binding.phone.setText(mCurrentUser?.phoneNumber)
                                binding.phone.isEnabled = false
                                binding.layout3.visibility = View.VISIBLE
                            }
                        }
                    }
            }


        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}