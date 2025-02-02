package com.hank.bingo822

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.snapshot.BooleanNode
import com.hank.bingo822.databinding.ActivityBingoBinding

class BingoActivity : AppCompatActivity() {
    companion object {
        private val TAG: String? = BingoActivity::class.java.simpleName

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
                val view = layoutInflater.inflate(R.layout.single_button, parent, false)
                return NumberHolder(view)
            }

            override fun onBindViewHolder(holder: NumberHolder, position: Int, model: Boolean) {
                holder.button.setText(buttons.get(position).number.toString())
                holder.button.picked = !model
            }
        }
        recy.adapter = ballAdapter

    }

    override fun onStart() {
        ballAdapter.startListening()
        super.onStart()
    }

    override fun onStop() {
        ballAdapter.stopListening()
        super.onStop()
    }

}

class NumberHolder(view: View) : RecyclerView.ViewHolder(view) {
    lateinit var button: NumberButton

    init {
        button = itemView.findViewById(R.id.viewButton)
    }

}

