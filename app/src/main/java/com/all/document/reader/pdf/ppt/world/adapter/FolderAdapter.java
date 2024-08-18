package com.all.document.reader.pdf.ppt.world.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.all.document.reader.pdf.ppt.world.R;
import com.all.document.reader.pdf.ppt.world.model.Folder;
import com.bumptech.glide.Glide;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private Context context;
    private List<Folder> folderList;
    private FolderAdapterListener listener;

    public class FolderViewHolder extends RecyclerView.ViewHolder {

        public TextView folderTitle;
        public ImageView folderThumbnail;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);

            folderTitle = (TextView) itemView.findViewById(R.id.folder_title);
            folderThumbnail = (ImageView) itemView.findViewById(R.id.folder_thumbnail);

           itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   listener.onFolderClick(folderList.get(getAdapterPosition()));
               }
           });

            folderThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onFolderClick(folderList.get(getAdapterPosition()));
                }
            });
        }
    }

    public FolderAdapter(Context context, List<Folder> folderList, FolderAdapterListener listener) {
        this.context = context;
        this.folderList = folderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_folder, viewGroup, false);

        return new FolderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folderList.get(position);
        holder.folderTitle.setText(folder.getTitle());
        Glide.with(context).load(folder.getThumbnail()).into(holder.folderThumbnail);
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public interface FolderAdapterListener {
        void onFolderClick(Folder folder);
    }
}
