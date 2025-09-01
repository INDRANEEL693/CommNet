package com.example.commnet.adapters;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.commnet.R;
import com.example.commnet.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.*;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> chatList;
    private final String currentUser;

    public ChatAdapter(List<ChatMessage> chatList, String currentUser) {
        this.chatList = chatList;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = chatList.get(position);

        boolean isSent = msg.getSender().equals(currentUser);

        holder.messageText.setText(msg.getMessage());
        holder.timestampText.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(msg.getTimestamp()));
        holder.messageText.setBackgroundResource(isSent ? R.drawable.bg_msg_sent : R.drawable.bg_msg_received);
        holder.messageText.setTextAlignment(isSent ? View.TEXT_ALIGNMENT_VIEW_END : View.TEXT_ALIGNMENT_VIEW_START);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestampText;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessageBody);
            timestampText = itemView.findViewById(R.id.textMessageTime);
        }
    }
}
