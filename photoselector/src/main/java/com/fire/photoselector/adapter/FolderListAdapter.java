package com.fire.photoselector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fire.photoselector.R;
import com.fire.photoselector.bean.ImageFolderBean;
import com.fire.photoselector.models.PhotoMessage;

import java.util.List;

import static com.fire.photoselector.models.PhotoMessage.SELECTED_PHOTOS;

/**
 * Created by Fire on 2017/4/10.
 */

public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.ViewHolder> implements OnClickListener {
    private static final String TAG = "FolderListAdapter";
    private Context context;
    private List<ImageFolderBean> list;
    private OnRecyclerViewItemClickListener listener;

    public FolderListAdapter(Context context, List<ImageFolderBean> list) {
        this.context = context;
        this.list = list;
    }

    public interface OnRecyclerViewItemClickListener {
        void onRecyclerViewItemClick(View v, int position);
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        List<String> imagePaths = list.get(position).getImagePaths();
        for (String imagePath : imagePaths) {
            if (PhotoMessage.isPhotoSelected(imagePath)) {
                holder.ivPhotoFilterChecked.setVisibility(View.VISIBLE);
                break;
            } else {
                holder.ivPhotoFilterChecked.setVisibility(View.GONE);
            }
        }
        if (list.get(position).isSelected()) {
            holder.rootView.setBackgroundColor(context.getResources().getColor(R.color.dividerColor));
        } else {
            holder.rootView.setBackgroundColor(context.getResources().getColor(R.color.textWriteColor));
        }
        holder.tvAlbumName.setText(list.get(position).getFolderName());
        Glide.with(context).load(list.get(position).getImagePaths().get(0)).into(holder.ivFolderThumb);
        String string = context.getResources().getString(R.string.album_photo_number);
        String format = String.format(string, list.get(position).getImageCounts());
        holder.tvAlbumPhotoNumber.setText(format);
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
        private ImageView ivFolderThumb;
        private TextView tvAlbumName;
        private TextView tvAlbumPhotoNumber;
        private ImageView ivPhotoFilterChecked;

        ViewHolder(View view) {
            super(view);
            rootView = view;
            ivFolderThumb = (ImageView) view.findViewById(R.id.iv_folder_thumb);
            tvAlbumName = (TextView) view.findViewById(R.id.tv_album_name);
            tvAlbumPhotoNumber = (TextView) view.findViewById(R.id.tv_album_photo_number);
            ivPhotoFilterChecked = (ImageView) view.findViewById(R.id.iv_photo_filter_checked);
        }
    }
}
