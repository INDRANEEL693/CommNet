package com.example.commnet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.commnet.R;
import com.example.commnet.adapters.InboxAdapter;
import com.example.commnet.models.InboxItem;
import com.google.firebase.firestore.*;

import java.util.*;

public class InboxActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InboxAdapter adapter;
    private List<InboxItem> inboxList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserEmail;
    private static final String TAG = "InboxActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        recyclerView = findViewById(R.id.inboxRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InboxAdapter(inboxList, this::openChatWithSender);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserEmail = getIntent().getStringExtra("userEmail");

        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadLatestMessages();
    }

    private void loadLatestMessages() {
        db.collection("sentMessages")
                .whereArrayContains("users", currentUserEmail)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, InboxItem> latestMessagesMap = new LinkedHashMap<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String senderEmail = doc.getString("senderEmail");
                        String message = doc.getString("message");
                        Date timestamp = doc.getDate("timestamp");

                        if (!latestMessagesMap.containsKey(senderEmail)) {
                            latestMessagesMap.put(senderEmail, new InboxItem(senderEmail, message, timestamp));
                        }
                    }

                    inboxList.clear();
                    inboxList.addAll(latestMessagesMap.values());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load messages: " + e.getMessage());
                    Toast.makeText(this, "Error loading inbox", Toast.LENGTH_SHORT).show();
                });
    }

    private void openChatWithSender(String senderEmail) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("senderEmail", senderEmail);
        intent.putExtra("userEmail", currentUserEmail);
        startActivity(intent);
    }
}
