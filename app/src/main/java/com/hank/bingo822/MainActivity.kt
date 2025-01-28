package com.hank.bingo822

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Group
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
import kotlinx.coroutines.selects.select
import java.util.Arrays

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener, View.OnClickListener {
    private lateinit var groupAvatars: Group
    private lateinit var avatar: ImageView
    private var member: Member? = null
    private val TAG: String? = MainActivity::class.java.simpleName
    private val requestAuth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
        }
    }
    private lateinit var binding: ActivityMainBinding
    val avatarIds = intArrayOf(
        R.drawable.avatar_0,
        R.drawable.avatar_1,
        R.drawable.avatar_2,
        R.drawable.avatar_3,
        R.drawable.avatar_4,
        R.drawable.avatar_5,
        R.drawable.avatar_6,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //
        avatar = binding.avatar
        groupAvatars = binding.groupAvatars
        //
        groupAvatars.visibility = View.GONE
        //
        binding.avatar0.setOnClickListener(this)
        binding.avatar1.setOnClickListener(this)
        binding.avatar2.setOnClickListener(this)
        binding.avatar3.setOnClickListener(this)
        binding.avatar4.setOnClickListener(this)
        binding.avatar5.setOnClickListener(this)
        binding.avatar6.setOnClickListener(this)

    }

    fun setNickname(view: View) {
        FirebaseAuth.getInstance().currentUser?.also {
            showNickDialog(it.uid, binding.tvNickname.text.toString())
        }
    }

    fun setAvatar(view: View) {
        groupAvatars.visibility =
            if (groupAvatars.visibility == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    fun setGroupAvatars(view: View) {

    }

    fun setFab(view: View) {
        val roomText = EditText(this)
        roomText.setText("Welcome")
        AlertDialog.Builder(this)
            .setTitle("Game Room")
            .setMessage("Input Room Tital")
            .setView(roomText).setPositiveButton("OK") { dialog, which ->
                val room = GameRoom(roomText.text.toString(), member)
                FirebaseDatabase.getInstance().getReference("room")
                    .push().setValue(room)
            }.show()
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
                FirebaseDatabase.getInstance().getReference("users").child(it.uid)
                    .child("displayName").setValue(it.displayName).addOnCompleteListener {
                        Log.d(
                            TAG, "bingo-data-displayName- " + "${auth.currentUser!!.displayName} "
                        )
                    }
            }
            //
            FirebaseDatabase.getInstance().getReference("users").child(it.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        member = snapshot.getValue(Member::class.java)
                        member?.nickname?.let { nick ->
                            binding.tvNickname.text = nick
                        } ?: showNickDialog(it)
                        member?.avatarId?.let { img ->
                            avatar.setImageResource(avatarIds[img])
                        }
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

    //
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
            }.show()
    }

    private fun signU() {
        val signIn = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
            Arrays.asList(
                EmailBuilder().build(),
                GoogleBuilder().build(),
            )
        ).setIsSmartLockEnabled(false).build()
        requestAuth.launch(signIn)
    }

    override fun onClick(v: View?) {
        val selectId = when (v!!.id) {
            R.id.avatar_0 -> 0
            R.id.avatar_1 -> 1
            R.id.avatar_2 -> 2
            R.id.avatar_3 -> 3
            R.id.avatar_4 -> 4
            R.id.avatar_5 -> 5
            R.id.avatar_6 -> 6
            else -> 0
        }
        FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("avatarId")
            .setValue(selectId)
        groupAvatars.visibility = View.GONE
    }


}













