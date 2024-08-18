package com.all.document.reader.pdf.ppt.world.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.all.document.reader.pdf.ppt.world.R;
import com.all.document.reader.pdf.ppt.world.model.Document;
import com.all.document.reader.pdf.ppt.world.utils.FlipAnimator;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> implements Filterable {

    private Context mContext;
    private List<Document> documentList;
    private List<Document> documentListFiltered;
    private DocumentAdapterListener listener;
    private SparseBooleanArray selectedItems;

    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    private static int currentSelectedIndex = -1;

    private String TAG;

    final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};

    public class DocumentViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        TextView filename, filePath, iconText, fileSize;
        ImageView iconImp, imgProfile;
        LinearLayout mainContainer;
        RelativeLayout iconContainer, iconBack, iconFront;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);

            filename = (TextView) itemView.findViewById(R.id.filename);
            filePath = (TextView) itemView.findViewById(R.id.file_path);
            iconText = (TextView) itemView.findViewById(R.id.icon_text);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);

            iconImp = (ImageView) itemView.findViewById(R.id.icon_star);
            imgProfile = (ImageView) itemView.findViewById(R.id.icon_profile);

            mainContainer = (LinearLayout) itemView.findViewById(R.id.main_container);

            iconContainer = (RelativeLayout) itemView.findViewById(R.id.icon_container);
            iconBack = (RelativeLayout) itemView.findViewById(R.id.icon_back);
            iconFront = (RelativeLayout) itemView.findViewById(R.id.icon_front);

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            listener.onRowLongClicked(getAdapterPosition());
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }
    }

    public DocumentAdapter(Context mContext, List<Document> documentList, DocumentAdapterListener listener, String TAG) {
        this.mContext = mContext;
        this.documentList = documentList;
        this.documentListFiltered = documentList;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
        this.TAG = TAG;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);

        return new DocumentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position) {
        Document document = documentListFiltered.get(position);

        holder.filename.setText(document.getFile().getName());
        holder.filePath.setText(document.getFile().getParentFile().getPath());
        holder.fileSize.setText(setFileSize(document));
        holder.iconText.setText(setIconText(document));
        holder.imgProfile.setImageResource(setImageProfile(document));

        holder.itemView.setActivated(selectedItems.get(position, false));

        applyImportant(holder, document);
        applyIconAnimation(holder, position);
        applyClickEvents(holder, position);

    }

    private String setIconText(Document document) {
        switch (TAG) {
            case "all":
                return allFilesIconText(document);

            case "pdf":
                return "P";

            case "doc":
                return "W";

            case "ppt":
                return "p";

            case "xls":
                return "X";

            case "txt":
                return "T";

            default:
                return "";
        }
    }

    private String allFilesIconText(Document document) {
        if (document.getFile().getName().endsWith(".pdf")) {
            return "P";
        } else if (document.getFile().getName().endsWith("doc") || document.getFile().getName().endsWith("docx")) {
            return "W";
        } else if (document.getFile().getName().endsWith("ppt") || document.getFile().getName().endsWith("pptx")) {
            return "p";
        } else if (document.getFile().getName().endsWith("xls") || document.getFile().getName().endsWith("xlsx")) {
            return "X";
        } else if (document.getFile().getName().endsWith("txt")) {
            return "T";
        } else {
            return "";
        }
    }

    private int setImageProfile(Document document) {
        switch (TAG) {
            case "all":
                return allFilesIconProfile(document);

            case "pdf":
                return R.drawable.bg_circle_front_pdf;

            case "doc":
                return R.drawable.bg_circle_front_doc;

            case "ppt":
                return R.drawable.bg_circle_front_ppt;

            case "xls":
                return R.drawable.bg_circle_front_xls;

            case "txt":
                return R.drawable.bg_circle_front_txt;

            default:
                return R.drawable.bg_circle_front;

        }
    }

    private int allFilesIconProfile(Document document) {
        if (document.getFile().getName().endsWith(".pdf")) {
            return R.drawable.bg_circle_front_pdf;
        } else if (document.getFile().getName().endsWith("doc") || document.getFile().getName().endsWith("docx")) {
            return R.drawable.bg_circle_front_doc;
        } else if (document.getFile().getName().endsWith("ppt") || document.getFile().getName().endsWith("pptx")) {
            return R.drawable.bg_circle_front_ppt;
        } else if (document.getFile().getName().endsWith("xls") || document.getFile().getName().endsWith("xlsx")) {
            return R.drawable.bg_circle_front_xls;
        } else if (document.getFile().getName().endsWith("txt")) {
            return R.drawable.bg_circle_front_txt;
        } else {

            return R.drawable.bg_circle_front;
        }

    }

    private String setFileSize(Document document) {
        int digitGroup = (int) (Math.log10(document.getFile().length()) / Math.log10(1024));
        try {
            return new DecimalFormat("#,##0.#").format(document.getFile().length() / Math.pow(1024, digitGroup))
                    + " " + units[digitGroup];
        } catch (Exception e){
            return String.valueOf(Integer.parseInt(String.valueOf(document.getFile().length() / 1024)) + " KB");
        }
    }

    @Override
    public int getItemCount() {
        return documentListFiltered.size();
    }

    private void applyClickEvents(DocumentViewHolder holder, final int position) {
        holder.iconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onIconClicked(position);
            }
        });

        holder.iconImp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onIconImportantClicked(position);
            }
        });

        holder.mainContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRowClicked(position);
            }
        });

        holder.mainContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onRowLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
    }


    private void applyImportant(DocumentViewHolder holder, Document document) {
        if (document.isImportant()) {
            holder.iconImp.setImageDrawable(ContextCompat.getDrawable(mContext, android.R.drawable.star_on));
            holder.iconImp.setColorFilter(ContextCompat.getColor(mContext, R.color.icon_tint_selected));
        } else {
            holder.iconImp.setImageDrawable(ContextCompat.getDrawable(mContext, android.R.drawable.star_off));
            holder.iconImp.setColorFilter(ContextCompat.getColor(mContext, R.color.icon_tint_normal));
        }
    }

    private void applyIconAnimation(DocumentViewHolder holder, int position) {
        if (selectedItems.get(position, false)) {
            holder.iconFront.setVisibility(View.GONE);
            resetIconYAxis(holder.iconBack);
            holder.iconBack.setVisibility(View.VISIBLE);
            holder.iconBack.setAlpha(1);
            if (currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, true);
                resetCurrentIndex();
            }
        } else {
            holder.iconBack.setVisibility(View.GONE);
            resetIconYAxis(holder.iconFront);
            holder.iconFront.setVisibility(View.VISIBLE);
            holder.iconFront.setAlpha(1);
            if ((reverseAllAnimations && animationItemsIndex.get(position, false)) || currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, false);
                resetCurrentIndex();
            }
        }
    }

    private void resetIconYAxis(View view) {
        if (view.getRotationY() != 0) {
            view.setRotationY(0);
        }
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    public void toggleSelection(int pos) {
        currentSelectedIndex = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            animationItemsIndex.delete(pos);
        } else {
            selectedItems.put(pos, true);
            animationItemsIndex.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        reverseAllAnimations = true;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void removeFile(int position) {
        Document document = documentListFiltered.get(position);
        document.getFile().delete();
        documentListFiltered.remove(position);
        resetCurrentIndex();
    }

    public File getFile(int position) {
        Document document = documentListFiltered.get(position);
        resetCurrentIndex();
        return document.getFile();
    }

    public void resetAnimationIndex() {
        reverseAllAnimations = false;
        animationItemsIndex.clear();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    documentListFiltered = documentList;
                } else {
                    List<Document> filteredList = new ArrayList<>();
                    for (Document doc : documentList) {

                        // here we are looking for title or type match
                        if (doc.getFile().getName().substring(0, doc.getFile().getName().lastIndexOf("."))
                                .toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(doc);
                        }
                    }

                    documentListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = documentListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                documentListFiltered = (ArrayList<Document>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface DocumentAdapterListener {
        void onIconClicked(int position);

        void onIconImportantClicked(int position);

        void onRowClicked(int position);

        void onRowLongClicked(int position);
    }
}
