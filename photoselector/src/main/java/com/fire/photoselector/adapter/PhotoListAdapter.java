package com.fire.photoselector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.fire.photoselector.R;
import com.fire.photoselector.view.SquareImageView;

import java.util.List;

import static com.fire.photoselector.models.PhotoMessage.SELECTED_PHOTOS;


/**
 * Created by Fire on 2017/4/8.
 */

public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> implements View.OnClickListener {
    private static final String TAG = "PhotoListAdapter";
    private List<String> list;
    private Context context;
    private OnRecyclerViewItemClickListener listener;

    public PhotoListAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    public interface OnRecyclerViewItemClickListener {
        void onRecyclerViewItemClick(View v, int position);
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(context).load(list.get(position)).into(holder.ivPhotoThumb);
        if (list.get(position).toLowerCase().endsWith("gif")) {
            holder.ivGifImage.setVisibility(View.VISIBLE);
        } else {
            holder.ivGifImage.setVisibility(View.GONE);
        }
        if (SELECTED_PHOTOS.contains(list.get(position))) {
            holder.ivPhotoChecked.setImageResource(R.drawable.compose_photo_preview_right);
        } else {
            holder.ivPhotoChecked.setImageResource(R.drawable.compose_photo_preview_default);
        }
        holder.ivPhotoChecked.setOnClickListener(this);
        holder.ivPhotoChecked.setTag(position);
        holder.rootView.setOnClickListener(this);
        holder.rootView.setTag(position);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onRecyclerViewItemClick(v, (int) v.getTag());
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View rootView;
        private SquareImageView ivPhotoThumb;
        private ImageView ivPhotoChecked;
        private ImageView ivGifImage;

        ViewHolder(View view) {
            super(view);
            rootView = view;
            ivPhotoThumb = (SquareImageView) view.findViewById(R.id.iv_photo_thumb);
            ivPhotoChecked = (ImageView) view.findViewById(R.id.iv_photo_checked);
            ivGifImage = (ImageView) view.findViewById(R.id.iv_gif_image);
        }
    }
}
