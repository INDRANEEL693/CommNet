package com.example.commnet.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.commnet.R;
import com.example.commnet.adapters.ChatAdapter;
import com.example.commnet.models.ChatMessage;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private String senderEmail, userEmail;
    private RecyclerView chatRecyclerView;
    private EditText editMessage;
    private Button btnSend;
    private FirebaseFirestore db;
    private List<ChatMessage> chatList = new ArrayList<>();
    private ChatAdapter adapter;

    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);

        db = FirebaseFirestore.getInstance();

        senderEmail = getIntent().getStringExtra("senderEmail");
        userEmail = getIntent().getStringExtra("userEmail");

        if (senderEmail == null || userEmail == null) {
            Toast.makeText(this, "Missing chat info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new ChatAdapter(chatList, userEmail);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        db.collection("sentMessages")
                .whereArrayContains("users", userEmail)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading chat: " + error.getMessage());
                        return;
                    }

                    chatList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String msgSender = doc.getString("senderEmail");
                        if (msgSender.equals(senderEmail) || msgSender.equals(userEmail)) {
                            String message = doc.getString("message");
                            Date timestamp = doc.getDate("timestamp");

                            chatList.add(new ChatMessage(msgSender, message, timestamp));
                        }
                    }
                    adapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(chatList.size() - 1);
                });
    }

    private void sendMessage() {
        String msg = editMessage.getText().toString().trim();
        if (msg.isEmpty()) return;

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", msg);
        messageData.put("senderEmail", userEmail);
        messageData.put("users", Arrays.asList(userEmail, senderEmail));
        messageData.put("timestamp", Timestamp.now());

        db.collection("sentMessages")
                .add(messageData)
                .addOnSuccessListener(docRef -> editMessage.setText(""))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show());
    }
}
