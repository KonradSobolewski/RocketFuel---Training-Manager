package com.example.konrad.rocketfuel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_upload_exercise.*
import kotlin.collections.ArrayList


class UploadExercise : AppCompatActivity() {

    private var mStorageReference: StorageReference? = null
    private var mDatabaseReference: DatabaseReference? = null
    private val GALLERY_REQESST = 1
    private var imgUrl: Uri? = null

    private var spinnerData:ArrayList<String>? = ArrayList()

    private var categoryIdSelect : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_exercise)

        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child("Exercises")

        imageUploadView.setOnClickListener({
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_REQESST)
        })

        uploadImageBtn.setOnClickListener({
            startPosting()
        })
        loadCategoryToSpinner()
    }

    private fun startPosting() {
        val title_val = uploadTitle.text.toString().trim()
        val desc_val = uploadDescription.text.toString().trim()
        val prompts_val = coachPromptUpload.text.toString().trim()

        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && !TextUtils.isEmpty(prompts_val) && imgUrl != null) {
            val filePath = mStorageReference!!.child("Exercises_img").child(imgUrl!!.lastPathSegment)
            filePath.putFile(imgUrl!!).addOnSuccessListener { taskSnapshot ->
                val downloadUrl = taskSnapshot.downloadUrl
                val newPost = mDatabaseReference!!.child(categoryIdSelect).child(title_val)

                newPost.child("desc").setValue(desc_val)
                newPost.child("prompt").setValue(prompts_val)
                newPost.child("image").setValue(downloadUrl!!.toString())
                newPost.push()

                startActivity(Intent(this, ExerciseDetailsActivity::class.java))
                finish()
            }

        }

    }

    private fun loadCategoryToSpinner(){
        FirebaseDatabase.getInstance().reference.child("Exercises").addListenerForSingleValueEvent(listener)
    }

    private val listener = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError?) {

        }

        override fun onDataChange(p0: DataSnapshot?) {
            p0!!.children
                    .map { it.key.toString() }
                    .forEach { spinnerData!!.add(it) }

            val valueList: ArrayList<String> = ArrayList()
            valueList.add(0,"Category")
            valueList.addAll(spinnerData!!)
            spinner.setItems(valueList)
            spinner.setOnItemSelectedListener { view, position, id, item ->
                if(position != 0)
                    categoryIdSelect = spinnerData!![position-1]
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK)
            return
        if (requestCode == GALLERY_REQESST) {
            imgUrl = data.data
            try {
                imageUploadView.setImageURI(imgUrl)
            } catch (e: OutOfMemoryError) {
                e.fillInStackTrace()
            }

        }
    }
}
