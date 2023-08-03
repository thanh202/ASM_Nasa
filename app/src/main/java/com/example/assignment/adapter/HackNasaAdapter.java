package com.example.assignment.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.databinding.LayoutItemDataBinding;
import com.example.assignment.models.HackNasa;

import java.util.Base64;
import java.util.List;

public class HackNasaAdapter extends RecyclerView.Adapter<HackNasaAdapter.HackNasaViewHolder> {

    private Context context;
    private List<HackNasa> list;

    public HackNasaAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<HackNasa> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HackNasaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HackNasaViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_data, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HackNasaViewHolder holder, int position) {
        HackNasa obj = list.get(position);
        if (obj == null){
            return;
        }

        String url  = decodeBase64(obj.getUrl());
        Log.d("url", url);
        Glide.with(context).load(url).error(R.drawable.baseline_error_24).into(holder.img);
        holder.tvTitle.setText(obj.getTitle());
        holder.tvContent.setEllipsize(TextUtils.TruncateAt.END);
        holder.tvContent.setText(obj.getExplanation());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class HackNasaViewHolder extends RecyclerView.ViewHolder{
        
        private ImageView img;
        private TextView tvTitle, tvContent;

        public HackNasaViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }

    private String decodeBase64(String url) {
        String newUrl = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Base64.Decoder base64Decoder = Base64.getUrlDecoder();
            byte[] bytes = base64Decoder.decode(url);
            newUrl = new String(bytes);
            return newUrl;
        }
        return newUrl;
    }

}
