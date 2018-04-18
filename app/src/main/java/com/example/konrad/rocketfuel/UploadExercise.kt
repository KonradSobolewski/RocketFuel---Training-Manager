package com.example.konrad.rocketfuel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_upload_exercise.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UploadExercise : AppCompatActivity() {

    private val mStorageReference: StorageReference = FirebaseStorage.getInstance().reference
    private val mDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val GALLERY_REQUEST = 1
    private var imgUrl: Uri = Uri.EMPTY

    private var spinnerData: ArrayList<String> = ArrayList()

    private lateinit var categoryIdSelect : String
    private lateinit var spotsDialog: SpotsDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_exercise)

        imageUploadView.setOnClickListener({
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_REQUEST)
        })

        uploadImageBtn.setOnClickListener({
            startPosting()
        })
        loadCategoryToSpinner()

        spotsDialog = SpotsDialog(this,R.style.DialogStyle)
    }

    private fun startPosting() {
        val exerciseReference: DatabaseReference = mDatabaseReference.child("Exercises")
        val titleVal = uploadTitle.text.toString().trim()
        val descVal = uploadDescription.text.toString().trim()
        val promptsVal = coachPromptUpload.text.toString().trim()
        categoryIdSelect = spinner.text.toString()

        val dbRefCategory: DatabaseReference = exerciseReference.child(categoryIdSelect)
        dbRefCategory.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                println(error?.message)
            }
            override fun onDataChange(snapshot: DataSnapshot?) {
                if(snapshot?.hasChild(titleVal) == true) {
                    Toast.makeText(
                            this@UploadExercise, "Exercise exists", Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    postData(titleVal, descVal, promptsVal, dbRefCategory.child(titleVal))
                }
            }
        })
    }

    private fun postData(title: String, description: String, hints: String,
                         dbRef: DatabaseReference) {
        spotsDialog.show()
        if (
                TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(hints) || imgUrl == Uri.EMPTY
        ) {
            spotsDialog.dismiss()
            Toast.makeText(this,getString(R.string.complete_fields), Toast.LENGTH_SHORT).show()
        }
        else {
            val filePath = mStorageReference.child("Exercises_img").child(imgUrl.lastPathSegment)
            filePath.putFile(imgUrl).addOnSuccessListener { taskSnapshot ->
                val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH)
                val date = Date()
                val downloadUrl = taskSnapshot.downloadUrl
                dbRef.child("title").setValue(title)
                dbRef.child("description").setValue(description)
                dbRef.child("prompts").setValue(hints)
                dbRef.child("timestamp").setValue(dateFormat.format(date).toString())
                dbRef.child("image").setValue(downloadUrl.toString())
                dbRef.push()

                spotsDialog.dismiss()
                finish()
            }
        }
    }

    private fun loadCategoryToSpinner(){
        FirebaseDatabase.getInstance().reference.child("Exercises")
                .addListenerForSingleValueEvent(listener)
    }

    private val listener = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError?) {}

        override fun onDataChange(p0: DataSnapshot?) {
            p0?.children
                    ?.map { it.key.toString() }
                    ?.forEach { spinnerData.add(it) }

            spinner.setItems(spinnerData)
            spinner.setOnItemSelectedListener { _, position, _, _ ->
                categoryIdSelect = spinnerData[position]
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK)
            return
        if (requestCode == GALLERY_REQUEST) {
            imgUrl = data.data
            try {
                imageUploadView.setImageURI(imgUrl)
            } catch (e: OutOfMemoryError) {
                e.fillInStackTrace()
            }
        }
    }

    //change font
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
