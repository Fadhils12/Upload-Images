package com.example.uploadimages;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Context mContext;
    List<DetailUpload> mainImageUploadInfoList;
    OnItemClick mListener;

    public RecyclerViewAdapter(Context context, List<DetailUpload> list) {

        this.mainImageUploadInfoList = list;

        this.mContext = context;

        this.mListener = (DisplayImages) context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DetailUpload UploadInfo = mainImageUploadInfoList.get(position);
        holder.imageNameTextView.setText(UploadInfo.getImageName());
        holder.imgDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CustomAlertDialog);
                    builder.setMessage("This image will be deleted");
                    builder.setPositiveButton("Delete image", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.onDeleteClick(position);
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        //Loading image from Glide library.
        Glide.with(mContext).load(UploadInfo.getImageURL()).into(holder.imageView);
//        Picasso.with(context)
//                .load(UploadInfo.getImageURL())
//                .fit()
//                .centerCrop()
//                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mainImageUploadInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView, imgDel;
        public TextView imageNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            imgDel = (ImageView) itemView.findViewById(R.id.img_del);

            imageNameTextView = (TextView) itemView.findViewById(R.id.image_name_textView);

        }
    }

    public interface OnItemClick{
        void onItemClick(int position);

        void onWhatEverClick(int position);

        void onDeleteClick(int position);
    }
    public void setOnItemClickListener(OnItemClick listener) {
        mListener = listener;
    }

}
