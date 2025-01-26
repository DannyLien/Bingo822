package com.hank.bingo822

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.google.firebase.auth.FirebaseAuth
import com.hank.bingo822.databinding.ActivityMainBinding
import java.util.Arrays

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {
    private val requestAuth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
        }
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // menu, Activity, signUp, signOut;

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_signout -> {
                FirebaseAuth.getInstance().signOut()
                true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        FirebaseAuth.getInstance().addAuthStateListener(this)
        super.onStart()
    }

    override fun onStop() {
        FirebaseAuth.getInstance().removeAuthStateListener(this)
        super.onStop()
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        auth.currentUser?.also {

        } ?: signU()

    }

    private fun signU() {
        val signIn = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(
                Arrays.asList(
                    EmailBuilder().build(),
                    GoogleBuilder().build(),
                )
            )
            .setIsSmartLockEnabled(false)
            .build()
        requestAuth.launch(signIn)
    }


}













