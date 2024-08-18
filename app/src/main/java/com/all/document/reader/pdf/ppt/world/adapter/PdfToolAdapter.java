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

public class PdfToolAdapter extends RecyclerView.Adapter<PdfToolAdapter.FolderViewHolder> {

    private Context context;
    private List<Folder> folderList;
    private PdfToolAdapterListener listener;

    public class FolderViewHolder extends RecyclerView.ViewHolder {

        public TextView folderTitle2;
        public ImageView folderThumbnail2;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);

            folderTitle2 = (TextView) itemView.findViewById(R.id.folder_title_2);
            folderThumbnail2 = (ImageView) itemView.findViewById(R.id.folder_thumbnail_2);

           itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   listener.onFolderClick(folderList.get(getAdapterPosition()));
               }
           });

            folderThumbnail2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onFolderClick(folderList.get(getAdapterPosition()));
                }
            });
        }
    }

    public PdfToolAdapter(Context context, List<Folder> folderList, PdfToolAdapterListener listener) {
        this.context = context;
        this.folderList = folderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_pdf_tool, viewGroup, false);

        return new FolderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folderList.get(position);
        holder.folderTitle2.setText(folder.getTitle());
        Glide.with(context).load(folder.getThumbnail()).into(holder.folderThumbnail2);
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public interface PdfToolAdapterListener {
        void onFolderClick(Folder folder);
    }
}
