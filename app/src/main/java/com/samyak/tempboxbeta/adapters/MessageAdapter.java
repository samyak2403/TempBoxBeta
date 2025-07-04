package com.samyak.tempboxbeta.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.samyak.tempboxbeta.R;
import com.samyak.tempboxbeta.models.Message;
import com.samyak.tempboxbeta.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    
    private List<Message> messages;
    private Context context;
    private OnMessageClickListener clickListener;
    
    public interface OnMessageClickListener {
        void onMessageClick(Message message);
    }
    
    public MessageAdapter(Context context) {
        this.context = context;
        this.messages = new ArrayList<>();
    }
    
    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.clickListener = listener;
    }
    
    // Optimized update method using DiffUtil for better performance
    public void updateMessages(List<Message> newMessages) {
        if (newMessages == null) {
            newMessages = new ArrayList<>();
        }
        
        // Use DiffUtil for efficient updates
        MessageDiffCallback diffCallback = new MessageDiffCallback(this.messages, newMessages);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        
        this.messages.clear();
        this.messages.addAll(newMessages);
        
        // Apply the changes with animations
        diffResult.dispatchUpdatesTo(this);
    }
    
    // Fast method to add new messages at the beginning
    public void addNewMessages(List<Message> newMessages) {
        if (newMessages == null || newMessages.isEmpty()) return;
        
        int oldSize = messages.size();
        messages.addAll(0, newMessages);
        notifyItemRangeInserted(0, newMessages.size());
        
        // If the list was empty before, notify about the range change
        if (oldSize == 0) {
            notifyItemRangeChanged(newMessages.size(), messages.size() - newMessages.size());
        }
    }
    
    // Method to update a single message (for read/unread status)
    public void updateMessage(Message updatedMessage) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(updatedMessage.getId())) {
                messages.set(i, updatedMessage);
                notifyItemChanged(i);
                break;
            }
        }
    }
    
    public void clearMessages() {
        int size = messages.size();
        messages.clear();
        notifyItemRangeRemoved(0, size);
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    // ViewHolder with optimized binding
    class MessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView senderInitial;
        private final TextView senderName;
        private final TextView subjectText;
        private final TextView timeText;
        private final View unreadIndicator;
        private final Chip attachmentChip;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderInitial = itemView.findViewById(R.id.sender_initial);
            senderName = itemView.findViewById(R.id.sender_name);
            subjectText = itemView.findViewById(R.id.subject_text);
            timeText = itemView.findViewById(R.id.time_text);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            attachmentChip = itemView.findViewById(R.id.attachment_chip);

            // Set click listener once
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onMessageClick(messages.get(position));
                }
            });
        }

        public void bind(Message message) {
            // Set sender info
            if (message.getFrom() != null && message.getFrom().getName() != null && !message.getFrom().getName().isEmpty()) {
                senderName.setText(message.getFrom().getName());
                senderInitial.setText(String.valueOf(message.getFrom().getName().charAt(0)));
            } else {
                senderName.setText(R.string.unknown_sender);
                senderInitial.setText("?");
            }

            // Set subject
            subjectText.setText(!TextUtils.isEmpty(message.getSubject()) ? message.getSubject() : context.getString(R.string.no_subject));

            // Set date
            timeText.setText(DateUtils.formatRelative(message.getCreatedAt()));

            // Set read/unread state
            unreadIndicator.setVisibility(message.isSeen() ? View.GONE : View.VISIBLE);
            senderName.setTypeface(null, message.isSeen() ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);
            subjectText.setTypeface(null, message.isSeen() ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);

            // Set attachment indicator
            attachmentChip.setVisibility(message.hasAttachments() ? View.VISIBLE : View.GONE);
        }
    }
    
    // DiffUtil callback for efficient updates
    private static class MessageDiffCallback extends DiffUtil.Callback {
        
        private final List<Message> oldList;
        private final List<Message> newList;
        
        public MessageDiffCallback(List<Message> oldList, List<Message> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }
        
        @Override
        public int getOldListSize() {
            return oldList.size();
        }
        
        @Override
        public int getNewListSize() {
            return newList.size();
        }
        
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            String oldId = oldList.get(oldItemPosition).getId();
            String newId = newList.get(newItemPosition).getId();
            return oldId != null && oldId.equals(newId);
        }
        
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Message oldMessage = oldList.get(oldItemPosition);
            Message newMessage = newList.get(newItemPosition);
            
            // Compare key fields that affect display
            return oldMessage.isSeen() == newMessage.isSeen() &&
                    oldMessage.hasAttachments() == newMessage.hasAttachments() &&
                    TextUtils.equals(oldMessage.getSubject(), newMessage.getSubject()) &&
                    TextUtils.equals(oldMessage.getCreatedAt(), newMessage.getCreatedAt());
        }
    }
} 