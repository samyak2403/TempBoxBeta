package com.samyak.tempboxbeta.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.samyak.tempboxbeta.models.Domain;

import java.util.List;

public class DomainAdapter extends ArrayAdapter<Domain> {
    
    private final LayoutInflater inflater;
    private final int resource;
    
    public DomainAdapter(@NonNull Context context, int resource, @NonNull List<Domain> domains) {
        super(context, resource, domains);
        this.inflater = LayoutInflater.from(context);
        this.resource = resource;
    }
    
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }
    
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }
    
    private View createItemView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final View view = (convertView == null) ? inflater.inflate(resource, parent, false) : convertView;
        
        Domain domain = getItem(position);
        if (domain != null) {
            TextView textView = (TextView) view;
            textView.setText(domain.getDomain());
        }
        
        return view;
    }
    
    @Override
    public long getItemId(int position) {
        Domain item = getItem(position);
        return item != null ? item.getId().hashCode() : 0;
    }
    
    @Override
    public boolean hasStableIds() {
        return true;
    }
} 