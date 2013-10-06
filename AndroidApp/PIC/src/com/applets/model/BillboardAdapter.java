package com.applets.model;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.applets.pic.R;

public class BillboardAdapter extends ArrayAdapter<Post>{

    Context context; 
    int layoutResourceId;    
    Post data[] = null;
    
    public BillboardAdapter(Context context, int layoutResourceId, Post[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PostHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new PostHolder();
            holder.content = (TextView)row.findViewById(R.id.firstLine);
            holder.creator = (TextView)row.findViewById(R.id.secondLine);
            
            row.setTag(holder);
        }
        else
        {
            holder = (PostHolder)row.getTag();
        }
        
        Post post = data[position];
        holder.creator.setText(post.getCreator());
        holder.content.setText(post.getContent());
        
        return row;
    }
    
    static class PostHolder
    {
        TextView creator;
        TextView content;
    }
}