package com.hank.bingo822

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.firebase.ui.common.ChangeEventType
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

class BingoActivity : AppCompatActivity(), View.OnClickListener {
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

                STATUS_JOINER_TURN -> {
                    myTurn = !isCreator
                }

                STATUS_CREATOR_BINGO -> {
                    AlertDialog.Builder(this@BingoActivity)
                        .setTitle("BINGO!")
                        .setMessage(if (isCreator) "恭喜你, 賓果了" else "對方賓果了")
                        .setPositiveButton("OK") { dialog, which ->
                            endGame()
                        }
                        .show()
                }

                STATUS_JOINER_BINGO -> {
                    AlertDialog.Builder(this@BingoActivity)
                        .setTitle("BINGO!")
                        .setMessage(if (!isCreator) "恭喜你, 賓果了" else "對方賓果了")
                        .setPositiveButton("OK") { dialog, which ->
                            endGame()
                        }
                        .show()
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    private fun endGame() {
        FirebaseDatabase.getInstance().getReference("rooms")
            .child(roomId!!)
            .child("status")
            .removeEventListener(statusListener)
        if (isCreator) {
            FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId!!)
                .removeValue()
        }
        finish()
    }

    private lateinit var recy: RecyclerView
//    private lateinit var buttons: MutableList<NumberButton>
    lateinit var numberMap: MutableMap<Int, Int>
    lateinit private var ballAdapter: FirebaseRecyclerAdapter<Boolean, NumberHolder>
    private var isCreator: Boolean = false
    private var roomId: String? = null
    private lateinit var binding: ActivityBingoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBingoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //
        roomId = intent.getStringExtra("ROOM_ID")
        isCreator = intent.getBooleanExtra("IS_CREATOR", false)
        Log.d(TAG, "bingo-room-intent-roomId- ${roomId}")
        val buttons = mutableListOf<NumberButton>()
        //
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
        numberMap = mutableMapOf<Int, Int>()
        for (i in (0..24)) {
//            numberMap.put(buttons.get(i).number , i)
            numberMap.put(buttons.get(i).number, i)
        }
        //
        recy = binding.recyclerView
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
                val view =
                    layoutInflater.inflate(
                        R.layout.single_button, parent, false
                    )
//                val view = SingleButtonBinding.inflate(
//                    LayoutInflater.from(parent.context), parent, false
//                )
                return NumberHolder(view)
            }

            override fun onBindViewHolder(holder: NumberHolder, position: Int, model: Boolean) {
                holder.viewButton.setText(buttons.get(position).number.toString())
//                holder.viewButton.picked = !model
                holder.viewButton.number = buttons.get(position).number
                holder.viewButton.isEnabled = !buttons.get(position).picked
                holder.viewButton.setOnClickListener(this@BingoActivity)
            }

            override fun onChildChanged(
                type: ChangeEventType,
                snapshot: DataSnapshot,
                newIndex: Int,
                oldIndex: Int
            ) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex)
                if (type == ChangeEventType.CHANGED) {
                    val number = snapshot.key?.toInt()
                    val pos = numberMap.get(number)
                    val picked = snapshot.value as Boolean
                    buttons.get(pos!!).picked = picked
                    val holder: NumberHolder =
                        recy.findViewHolderForAdapterPosition(pos!!) as NumberHolder
                    holder.viewButton.picked = true
                    //
                    Log.d(TAG, "bingo-onclick- ${number} , ${pos} ")
                    //
                    val nums = IntArray(25)
                    for (i in (0..24)) {
                        nums[i] = if (buttons.get(i).picked) 1 else 0
                    }
                    var bingo = 0
                    for (i in (0..4)) {
                        var sum = 0
                        for (j in (0..4)) {
                            sum += nums[i * 5 + j]
                        }
                        bingo += if (sum == 5) 1 else 0
                        sum = 0
                        for (j in 0..4) {
                            sum += nums[j * 5 + i]
                        }
                        bingo += if (sum == 5) 1 else 0
                    }
                    Log.d(TAG, "bingo-bingo- ${bingo}")
                    if (bingo > 0) {
                        FirebaseDatabase.getInstance().getReference("rooms")
                            .child(roomId!!)
                            .child("status")
                            .setValue(if (isCreator) STATUS_CREATOR_BINGO else STATUS_JOINER_BINGO)
                    }
                }
            }
        }
        recy.adapter = ballAdapter

    }

    class NumberHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var viewButton: NumberButton

        init {
            viewButton = view.findViewById(R.id.viewButton)
        }
    }
//    class NumberHolder(view: SingleButtonBinding) : RecyclerView.ViewHolder(view.root) {
//        val viewButton = view.viewButton
//    }

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

    override fun onClick(v: View?) {
        if (myTurn) {
            val number = (v as NumberButton).number
            FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId!!)
                .child("numbers")
                .child(number.toString())
                .setValue(true)
            FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId!!)
                .child("status")
                .setValue(if (isCreator) STATUS_JOINER_TURN else STATUS_CREATOR_TURN)
        }
    }

}



