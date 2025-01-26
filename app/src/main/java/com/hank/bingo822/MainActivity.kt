package com.hank.bingo822

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hank.bingo822.databinding.ActivityMainBinding
import java.util.Arrays

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {
    private val TAG: String? = MainActivity::class.java.simpleName
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
        // displayName

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
        auth.currentUser?.also { it ->
            it.displayName?.run {
                FirebaseDatabase.getInstance().getReference("users")
                    .child(it.uid)
                    .child("displayName")
                    .setValue(it.displayName)
                    .addOnCompleteListener {
                        Log.d(
                            TAG, "bingo-data-displayName- " +
                                    "${auth.currentUser!!.displayName} "
                        )
                    }
            }
            //
            FirebaseDatabase.getInstance().getReference("users")
                .child(it.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val member = snapshot.getValue(Member::class.java)
                        member?.nickname?.let { nick ->
                            binding.tvNickname.text = nick
                        } ?: showNickDialog(it)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

        } ?: signU()

    }

    private fun showNickDialog(user: FirebaseUser) {
        val uid = user.uid
        val nick = user.displayName
        showNickDialog(uid, nick)
    }

    private fun showNickDialog(uid: String, nick: String?) {
        val editText = EditText(this)
        editText.setText("${nick}")
        AlertDialog.Builder(this)
            .setTitle("Nickname")
            .setMessage("Input Nickname")
            .setView(editText)
            .setPositiveButton("OK") { dialog, which ->
                FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .child("nickname")
                    .setValue(editText.text.toString())
            }
            .show()
    }
    //
    fun setNickname(view: View) {
        FirebaseAuth.getInstance().currentUser?.also {
            showNickDialog(it.uid, binding.tvNickname.text.toString())
        }
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













