package com.example.cruldapp.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.cruldapp.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }
    private var user:HashMap<String,String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.goToLogin.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

        val db = Firebase.firestore

        binding.btnSignup.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val repeatPassword = binding.repeatPassword.text.toString()
            val name = binding.name.text.toString()

            if (email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty() || name.isEmpty()) {
                Toast.makeText(applicationContext, "fill the fields", Toast.LENGTH_SHORT).show()
            } else if (password != repeatPassword) {
                Toast.makeText(applicationContext, "passwords not same!!", Toast.LENGTH_SHORT)
                    .show()
            }
            else {

                var newId:Long? = 1

                db.collection("users")
                    .orderBy("userID",Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener {document->
                        if (!document.isEmpty){
                            val lastID = document.documents[0].getString("userID")?.toLong()
                            Log.e("userId",lastID.toString())
                            if (lastID!=null) {
                                newId = lastID+1
                                Log.e("inside null condn",newId.toString())
                            }
                            else{
                                Log.e("outside null condn",newId.toString())
                                newId = 1
                                user = hashMapOf(
                                    "name" to name,
                                    "userID" to newId.toString()
                                )
                            }
                        }
                        else{
                            Log.e("outside document condn",newId.toString())
                            newId = 1
                        }
                    }


                user?.let { newUser ->
                    db.collection("users")
                        .add(newUser)
                        .addOnCompleteListener {
                            if (it.isSuccessful){
                                Log.e("Main Activity","user added succeed")
                            } else{
                                Log.e("Main Activity","it.result.toString()")
                            }
                        }
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userProfile = userProfileChangeRequest {
                                displayName = name
                            }
                            user?.updateProfile(userProfile)?.addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "user name added",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                Toast.makeText(
                                    applicationContext,
                                    "user created successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        this@SignupActivity,
                                        LoginActivity::class.java
                                    )
                                )
                                finish()
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "something went wrong!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}