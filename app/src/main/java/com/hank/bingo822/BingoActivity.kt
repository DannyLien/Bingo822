package com.hank.bingo822

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.hank.bingo822.databinding.ActivityBingoBinding

class BingoActivity : AppCompatActivity() {
    companion object {
        private val TAG: String? = BingoActivity::class.java.simpleName

    }

    private var isCreator: Boolean = false
    private var roomId: String? = null
    private lateinit var binding: ActivityBingoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBingoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getStringExtra("ROOM_ID")
        isCreator = intent.getBooleanExtra("IS_CREATOR", false)
        Log.d(TAG, "bingo-room-intent-roomId- ${roomId}")

        for (i in (1..25)) {
            FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId!!)
                .child("numbers")
                .child(i.toString())
                .setValue(false)
        }
        val buttons = mutableListOf<NumberButton>()
        for (i in (0..24)) {
            val button = NumberButton(this)
            button.number = i + 1
            buttons.add(button)
        }
        buttons.shuffle()
        //
        val recy = binding.recycler
        recy.setHasFixedSize(true)
        recy.layoutManager=GridLayoutManager(this, 5)
        //Adapter
//
    }

}