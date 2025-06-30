package com.example.galdanaja.ui.chat

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.galdanaja.R
import com.example.galdanaja.adapter.MessageAdapter
import com.example.galdanaja.data.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()

    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton

    private var chatId: String? = null
    private val db = Firebase.firestore
    private val currentUser = Firebase.auth.currentUser

    private lateinit var toolbarUserName: TextView
    private lateinit var toolbarProfileImage: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 1. Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 2. Sembunyikan judul default agar tidak tumpang tindih
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 3. Inisialisasi view dari layout kustom kita
        toolbarUserName = findViewById(R.id.toolbar_user_name)
        toolbarProfileImage = findViewById(R.id.toolbar_profile_image)

        // 4. Ambil data dari Intent
        chatId = intent.getStringExtra("CHAT_ID")
        val chatName = intent.getStringExtra("OTHER_USER_NAME")
        val chatImageUrl = intent.getStringExtra("OTHER_USER_IMAGE_URL") // Pastikan ini dikirim dari adapter/activity sebelumnya

        // 5. Set data ke view kustom di toolbar
        toolbarUserName.text = chatName

        Glide.with(this)
            .load(chatImageUrl)
            .placeholder(R.drawable.nunu) // Siapkan gambar default
            .error(R.drawable.nunu)
            .into(toolbarProfileImage)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = chatName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        messageAdapter = MessageAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = messageAdapter

        buttonSend.setOnClickListener { sendMessage() }

        loadMessages()
    }

    private fun loadMessages() {
        if (chatId == null) return

        db.collection("chats").document(chatId!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                messageList.clear()
                for (doc in snapshots!!) {
                    messageList.add(doc.toObject(Message::class.java))
                }
                messageAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messageList.size - 1) // Auto-scroll ke pesan terbaru
            }
    }

    private fun sendMessage() {
        val text = editTextMessage.text.toString()
        if (text.isNotBlank() && chatId != null && currentUser != null) {
            val message = Message(text, currentUser.uid)
            db.collection("chats").document(chatId!!)
                .collection("messages")
                .add(message)
            val otherUserId = intent.getStringExtra("OTHER_USER_ID")

            if (otherUserId != null) {
                val chatUpdate = mapOf(
                    "lastMessage" to text,
                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "unreadCount.${otherUserId}" to com.google.firebase.firestore.FieldValue.increment(1)
                )

                db.collection("chats").document(chatId!!)
                    .update(chatUpdate)
            }

            editTextMessage.text.clear()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // Di dalam ChatActivity.kt

    override fun onResume() {
        super.onResume()
        markMessagesAsRead()
    }

    private fun markMessagesAsRead() {
        if (chatId != null && currentUser != null) {
            val currentUserId = currentUser.uid
            db.collection("chats").document(chatId!!)
                .update("unreadCount.${currentUserId}", 0)
                .addOnFailureListener { e ->
                    Log.w("ChatActivity", "Gagal mereset unread count", e)
                }
        }
    }
}