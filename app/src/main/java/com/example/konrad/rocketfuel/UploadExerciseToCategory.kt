package com.example.konrad.rocketfuel

import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.activity_upload_exercise_to_category.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.text.SimpleDateFormat
import java.util.*

class UploadExerciseToCategory : AppCompatActivity() {

    private var mStorageReference: StorageReference? = null
    private var mDatabaseReference: DatabaseReference? = null
    private val GALLERY_REQESST = 1
    private var imgUrl: Uri? = null

    private var title : String? = null
    private var spotsDialog: SpotsDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_exercise_to_category)
        title = intent.extras.getString("title")

        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child("Exercises")

        categoryName.text = title

        imageUploadViewCategory.setOnClickListener({
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_REQESST)
        })

        uploadImageBtnCategory.setOnClickListener({
            startPosting()
        })
        spotsDialog = SpotsDialog(this,R.style.DialogStyle)
    }

    @SuppressLint("SimpleDateFormat")
    private fun startPosting() {
        val title_val = uploadTitleCategory.text.toString().trim()
        val desc_val = uploadDescriptionCategory.text.toString().trim()
        val prompts_val = coachPromptUploadCategory.text.toString().trim()
        spotsDialog?.show()
        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && !TextUtils.isEmpty(prompts_val) && imgUrl != null) {
            val filePath = mStorageReference!!.child("Exercises_img").child(imgUrl!!.lastPathSegment)
            filePath.putFile(imgUrl!!).addOnSuccessListener { taskSnapshot ->
                val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val date = Date()
                val downloadUrl = taskSnapshot.downloadUrl
                val newPost = mDatabaseReference!!.child(title).child(title_val)
                newPost.child("title").setValue(title_val)
                newPost.child("description").setValue(desc_val)
                newPost.child("prompts").setValue(prompts_val)
                newPost.child("timestamp").setValue( dateFormat.format(date).toString())
                newPost.child("image").setValue(downloadUrl!!.toString())
                newPost.push()
                spotsDialog?.dismiss()
                startActivity(Intent(this, ExerciseDetailsActivity::class.java)
                        .putExtra("title", title_val))
                finish()
            }
        }else{
            spotsDialog?.dismiss()
            Toast.makeText(this,"Complete all fields",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK)
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

    //change font
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
