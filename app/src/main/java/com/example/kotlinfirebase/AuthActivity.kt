package com.example.kotlinfirebase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinfirebase.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val GOOGLE_SIGN_IN = 100
    var TAG = "login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Analytics Event
        binding.btCreateUser.setOnClickListener {
            var email: String = binding.emailLog.text.toString()
            var pass: String = binding.passwordLog.text.toString()
            createUser(email, pass)
        }
        binding.btLogin.setOnClickListener {
            var email: String = binding.emailLog.text.toString()
            var pass: String = binding.passwordLog.text.toString()
            logIn(email, pass)
        }

        notification()
        session()

        binding.btnGoogle.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        FirebaseMessaging.getInstance().subscribeToTopic("PruebaKotlin")
    }

    @SuppressLint("StringFormatInvalid")
    private fun notification() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and toast
                println(token)
                Toast.makeText(this@AuthActivity, "token: $token", Toast.LENGTH_SHORT).show()
                binding.tokenId.setText(token)
            })
    }

    override fun onStart(){
        super.onStart()

        binding.authLayout.visibility = View.VISIBLE
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if( email != null && provider != null){
            binding.authLayout.visibility = View.INVISIBLE
            showProfile(email, ProviderType.valueOf(provider))
        }
    }

    private fun createUser(email: String, pass: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,pass).addOnCompleteListener() {
            if( it.isSuccessful){
                showProfile(it.result?.user?.email ?: "", ProviderType.BASIC)
            }else{
                showAlert()
            }
        }
    }
    private fun logIn(email: String, pass: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,pass).addOnCompleteListener() {
            if( it.isSuccessful){
                showProfile(it.result?.user?.email ?: "", ProviderType.BASIC)
            }else{
                showAlert()
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando el usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    private fun showProfile(email: String, provider: ProviderType){
        val profileIntent = Intent(this, UserProfile::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(profileIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener() {
                            if (it.isSuccessful) {
                                showProfile(account.email ?: "", ProviderType.GOOGLE)
                            } else {
                                showAlert()
                            }
                        }
                }
            }catch (e: ApiException){
                showAlert()
            }

        }
    }
}