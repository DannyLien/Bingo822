package com.hank.bingo822

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.snapshot.BooleanNode
import com.hank.bingo822.databinding.ActivityBingoBinding
import com.hank.bingo822.databinding.SingleButtonBinding

class BingoActivity : AppCompatActivity() {
    companion object {
        private val TAG: String? = BingoActivity::class.java.simpleName
        val STATUS_INIT = 0
        val STATUS_CREATED = 1
        val STATUS_JOINED = 2
        val STATUS_CREATOR_TURN = 3
        val STATUS_JOINER_TURN = 4
        val STATUS_CREATOR_BINGO = 5
        val STATUS_JOINER_BINGO = 6
    }

    var myTurn: Boolean = false
        set(value) {
            field = value
            binding.tvInfo.setText(if (value) "請選號" else "等對手選號")
        }

    val statusListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val status: Long = snapshot.value as Long
            when (status.toInt()) {
                STATUS_CREATED -> {
                    binding.tvInfo.setText("等對手加入")
                }

                STATUS_JOINED -> {
                    binding.tvInfo.setText("對手已經加入")
                    FirebaseDatabase.getInstance().getReference("rooms")
                        .child(roomId!!)
                        .child("status")
                        .setValue(STATUS_CREATOR_TURN)
                }

                STATUS_CREATOR_TURN -> {
                    myTurn = isCreator
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {}

    }

    private lateinit var ballAdapter: FirebaseRecyclerAdapter<Boolean, NumberHolder>
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
        val buttons = mutableListOf<NumberButton>()
        if (isCreator) {
            for (i in (1..25)) {
                FirebaseDatabase.getInstance().getReference("rooms")
                    .child(roomId!!)
                    .child("numbers")
                    .child(i.toString())
                    .setValue(false)
            }
            FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId!!)
                .child("status")
                .setValue(STATUS_CREATED)
        } else {
            FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId!!)
                .child("status")
                .setValue(STATUS_JOINED)
        }
//
        for (i in (0..24)) {
            val button = NumberButton(this)
            button.number = i + 1
            buttons.add(button)
        }
        buttons.shuffle()
        //
        val recy = binding.recyclerView
        recy.setHasFixedSize(true)
        recy.layoutManager = GridLayoutManager(this, 5)
        //Adapter
        val query = FirebaseDatabase.getInstance().getReference("rooms")
            .child(roomId!!)
            .child("numbers")
            .orderByKey()
        val options = FirebaseRecyclerOptions.Builder<Boolean>()
            .setQuery(query, Boolean::class.java)
            .build()
        ballAdapter = object : FirebaseRecyclerAdapter<Boolean, NumberHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberHolder {
//                val view =
//                    layoutInflater.inflate(R.layout.single_button, parent, false
//                )
                val view = SingleButtonBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                return NumberHolder(view)
            }

            override fun onBindViewHolder(holder: NumberHolder, position: Int, model: Boolean) {
                holder.viewButton.setText(buttons.get(position).number.toString())
                holder.viewButton.picked = !model
            }
        }
        recy.adapter = ballAdapter

    }

    //        class NumberHolder(view: View) : ViewHolder(view) {
//        lateinit var viewButton: NumberButton
//        init {
//            viewButton = view.findViewById(R.id.viewButton)
//        }
//    }
    class NumberHolder(view: SingleButtonBinding) : RecyclerView.ViewHolder(view.root) {
        val viewButton = view.viewButton
    }

    override fun onStart() {
        super.onStart()
        ballAdapter.startListening()
        FirebaseDatabase.getInstance().getReference("rooms")
            .child(roomId!!)
            .child("status")
            .addValueEventListener(statusListener)
    }

    override fun onStop() {
        super.onStop()
        ballAdapter.stopListening()
        FirebaseDatabase.getInstance().getReference("rooms")
            .child(roomId!!)
            .child("status")
            .removeEventListener(statusListener)
    }

}



