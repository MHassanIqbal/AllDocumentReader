package com.all.document.reader.pdf.ppt.world.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.all.document.reader.pdf.ppt.world.R;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.MyViewHolder> {

    public Context context;
    private List<Bitmap> bitmapList;

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImg;
        ImageButton ibRemove;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImg = (ImageView) itemView.findViewById(R.id.iv_img);
            ibRemove = (ImageButton) itemView.findViewById(R.id.ib_remove);
        }
    }

    public ImageRecyclerAdapter(Context context, List<Bitmap> bitmapList) {
        this.context = context;
        this.bitmapList = bitmapList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_image, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Bitmap bitmap = bitmapList.get(position);

        holder.ivImg.setImageBitmap(bitmap);

        holder.ibRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapList.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bitmapList.size();
    }
}
