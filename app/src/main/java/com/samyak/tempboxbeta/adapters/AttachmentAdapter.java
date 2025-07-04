package com.samyak.tempboxbeta.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.samyak.tempboxbeta.R;
import com.samyak.tempboxbeta.models.Attachment;

import java.util.ArrayList;
import java.util.List;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder> {
    
    private List<Attachment> attachments;
    private final Context context;
    
    public AttachmentAdapter(Context context, List<Attachment> attachments) {
        this.context = context;
        this.attachments = attachments;
    }
    
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attachment, parent, false);
        return new AttachmentViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        Attachment attachment = attachments.get(position);
        holder.bind(attachment);
    }
    
    @Override
    public int getItemCount() {
        return attachments.size();
    }
    
    class AttachmentViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconImageView;
        private TextView filenameTextView;
        private TextView sizeTextView;
        private TextView typeTextView;
        
        public AttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            
            iconImageView = itemView.findViewById(R.id.attachment_icon);
            filenameTextView = itemView.findViewById(R.id.filename_text);
            sizeTextView = itemView.findViewById(R.id.size_text);
            typeTextView = itemView.findViewById(R.id.type_text);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    downloadAttachment(attachments.get(position));
                }
            });
        }
        
        public void bind(Attachment attachment) {
            filenameTextView.setText(attachment.getFilename());
            
            // Format file size
            String sizeText = formatFileSize(attachment.getSize());
            sizeTextView.setText(sizeText);
            
            // Set content type
            String contentType = attachment.getContentType();
            if (contentType != null && !contentType.isEmpty()) {
                typeTextView.setText(contentType);
                typeTextView.setVisibility(View.VISIBLE);
            } else {
                typeTextView.setVisibility(View.GONE);
            }
            
            // Set appropriate icon based on file type
            setFileIcon(attachment.getContentType());
        }
        
        private void setFileIcon(String contentType) {
            int iconRes = R.drawable.ic_attachment;
            
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    iconRes = R.drawable.ic_image;
                } else if (contentType.startsWith("text/") || contentType.contains("pdf")) {
                    iconRes = R.drawable.ic_document;
                } else if (contentType.startsWith("audio/")) {
                    iconRes = R.drawable.ic_audio;
                } else if (contentType.startsWith("video/")) {
                    iconRes = R.drawable.ic_video;
                }
            }
            
            iconImageView.setImageResource(iconRes);
        }
        
        private String formatFileSize(int bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            } else if (bytes < 1024 * 1024) {
                return String.format("%.1f KB", bytes / 1024.0);
            } else {
                return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            }
        }
        
        private void downloadAttachment(Attachment attachment) {
            if (attachment.getDownloadUrl() != null && !attachment.getDownloadUrl().isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(attachment.getDownloadUrl()));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Cannot open attachment", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Download URL not available", Toast.LENGTH_SHORT).show();
            }
        }
    }
}