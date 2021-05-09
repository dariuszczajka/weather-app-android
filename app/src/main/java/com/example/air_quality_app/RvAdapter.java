package com.example.air_quality_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {
    private Hashtable<String,String> Data;
    private LayoutInflater Inflater;
    private ItemClickListener ClickListener;

    RvAdapter(Context context, Hashtable<String,String> data) {
        this.Inflater = LayoutInflater.from(context);
        this.Data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = Inflater.inflate(R.layout.cards_bottom, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RvAdapter.ViewHolder holder, int position) {
        Enumeration<String> k = Data.keys();
        ArrayList<String > temp = new ArrayList<>();
        while(k.hasMoreElements()){
            temp.add(String.valueOf(k.nextElement()));
        }
        String name = temp.get(position);
        String value = Data.get(name);

        holder.polutantValue.setText(value);
        if(value != null){
            holder.polutantBar.setProgress((int) Math.ceil(Double.parseDouble(value)));
        }else{
            holder.polutantBar.setProgress(0);
        }
        holder.polutantName.setText(name);

    }

    @Override
    public int getItemCount() {
        return Data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView polutantName;
        TextView polutantValue;
        ProgressBar polutantBar;

        ViewHolder(View itemView){
            super(itemView);
            polutantName = itemView.findViewById(R.id.polutantName);
            polutantValue = itemView.findViewById(R.id.polutantValue);
            polutantBar = itemView.findViewById(R.id.polutantBar);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(ClickListener != null) ClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    String getItem(int id){
        return Data.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener){
        this.ClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void onItemClick(View view, int position);
    }
}
