package com.example.kotlinfirebase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kotlinfirebase.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth

enum class ProviderType{
    BASIC,
    GOOGLE
}

class UserProfile : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        var email = bundle?.getString("email")
        var provider = bundle?.getString("provider")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Glide.with(this).load(currentUser.getPhotoUrl()).into(binding.imageUser)
        }


        init(email ?: "", provider ?: "")

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

        binding.imageUser.setOnClickListener{
            choosePicture()
        }
    }
    private fun init(email: String, provider: String){
        title = "Inicio"
        binding.textEmail.text = email
        binding.textProvider.text = provider

        binding.btLogOut.setOnClickListener {

            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }
    private fun choosePicture() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 1)
    }
}