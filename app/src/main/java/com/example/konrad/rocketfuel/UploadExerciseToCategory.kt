package com.example.konrad.rocketfuel

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_upload_exercise_to_category.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*


class UploadExerciseToCategory : AppCompatActivity() {

    private val mStorageReference: StorageReference = FirebaseStorage.getInstance().reference
    private val mDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val GALLERY_REQUEST = 1
    private var imgUrl: Uri = Uri.EMPTY

    private lateinit var title: String
    private lateinit var spotsDialog: SpotsDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_exercise_to_category)

        title = intent.extras.getString("title") ?: ""
        categoryName.text = title

        imageUploadViewCategory.setOnClickListener({
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(galleryIntent, GALLERY_REQUEST)
        })

        uploadImageBtnCategory.setOnClickListener({
            startPosting()
        })
        spotsDialog = SpotsDialog(this,R.style.DialogStyle)
    }

    private fun startPosting() {
        val exerciseReference: DatabaseReference = mDatabaseReference.child("Exercises")
        val titleVal = uploadTitleCategory.text.toString().trim()
        val descVal = uploadDescriptionCategory.text.toString().trim()
        val promptsVal = coachPromptUploadCategory.text.toString().trim()

        spotsDialog.show()

        if (
                TextUtils.isEmpty(titleVal) ||
                TextUtils.isEmpty(descVal) ||
                TextUtils.isEmpty(promptsVal) ||
                imgUrl == Uri.EMPTY
        ) {
            spotsDialog.dismiss()
            Toast.makeText(this,getString(R.string.complete_fields),Toast.LENGTH_SHORT).show()
        }
        else {
            val filePath = mStorageReference.child("Exercises_img").child(imgUrl.lastPathSegment)
            filePath.putFile(imgUrl).addOnSuccessListener { taskSnapshot ->
                val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH)
                val date = Date()
                val downloadUrl = taskSnapshot.downloadUrl
                val newPost: DatabaseReference = exerciseReference.child(title).child(titleVal)
                newPost.run{
                    child("title").setValue(titleVal)
                    newPost.child("description").setValue(descVal)
                    newPost.child("prompts").setValue(promptsVal)
                    newPost.child("timestamp").setValue( dateFormat.format(date).toString())
                    newPost.child("image").setValue(downloadUrl.toString())
                    push()
                }
                spotsDialog.dismiss()
                startActivity(Intent(this, ExerciseDetailsActivity::class.java)
                        .putExtra("title", titleVal))
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
            if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
                try {
                    imgUrl = data.data
                    val imageStream = contentResolver.openInputStream(imgUrl)
                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                    imageUploadViewCategory.setImageBitmap(selectedImage)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(this, getString(R.string.went_wrong), Toast.LENGTH_LONG)
                            .show()
                }
            }
            else {
                Toast.makeText(this, getString(R.string.no_image), Toast.LENGTH_LONG)
                        .show()
            }
    }

    //change font
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
