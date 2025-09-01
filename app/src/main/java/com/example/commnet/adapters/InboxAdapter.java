package com.example.commnet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.commnet.R;
import com.example.commnet.models.InboxItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.InboxViewHolder> {

    private final List<InboxItem> inboxList;
    private final OnSenderClickListener listener;

    public interface OnSenderClickListener {
        void onSenderClick(String senderEmail);
    }

    public InboxAdapter(List<InboxItem> inboxList, OnSenderClickListener listener) {
        this.inboxList = inboxList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inbox, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        InboxItem item = inboxList.get(position);
        holder.senderText.setText(item.getSenderEmail());
        holder.messageText.setText(item.getMessage());

        String formattedTime = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                .format(item.getTimestamp());
        holder.timeText.setText(formattedTime);

        holder.itemView.setOnClickListener(v -> listener.onSenderClick(item.getSenderEmail()));
    }

    @Override
    public int getItemCount() {
        return inboxList.size();
    }

    static class InboxViewHolder extends RecyclerView.ViewHolder {
        TextView senderText, messageText, timeText;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.textSender);
            messageText = itemView.findViewById(R.id.textMessage);
            timeText = itemView.findViewById(R.id.textTime);
        }
    }
}
