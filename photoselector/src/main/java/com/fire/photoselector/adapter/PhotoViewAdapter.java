package com.fire.photoselector.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.fire.photoselector.R;
import com.fire.photoselector.models.PhotoSelectorSetting;

import java.util.List;

/**
 * Created by Fire on 2017/4/11.
 */

public class PhotoViewAdapter extends PagerAdapter {
    private static final String TAG = "PhotoViewAdapter";
    private Context context;
    private List<String> list;
    private PhotoView photoView;
    private BitmapFactory.Options options;
    /**
     * 照片宽高比例
     */
    private float photoRatio;

    public PhotoViewAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        photoView = new PhotoView(context);
        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(list.get(position), options);
        photoRatio = (float) options.outWidth / options.outHeight;
        // 如果当前照片宽高比小于PhotoView宽高比,说明图片的高度超出了屏幕范围,需要从图片最上方显示,设置ScaleType.FIT_START
        // 其余情况设置ScaleType.FIT_CENTER
        if (photoRatio < PhotoSelectorSetting.SCREEN_RATIO) {
            photoView.setScaleType(ImageView.ScaleType.FIT_START);
        } else {
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        photoView.enable();
        Glide.with(context).load(list.get(position)).into(photoView);
        container.addView(photoView);
        return photoView;
//        View view = LayoutInflater.from(context).inflate(R.layout.photo_view_layout, container, false);
//        ImageView ivPhotoView = (ImageView) view.findViewById(R.id.iv_photo_view);
//        Glide.with(context).load(list.get(position)).into(ivPhotoView);
//        container.addView(view);
//        return view;
    }
}