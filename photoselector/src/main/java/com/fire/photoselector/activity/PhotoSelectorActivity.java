package com.fire.photoselector.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fire.photoselector.R;
import com.fire.photoselector.adapter.FolderListAdapter;
import com.fire.photoselector.adapter.PhotoListAdapter;
import com.fire.photoselector.bean.ImageFolderBean;
import com.fire.photoselector.models.PhotoMessage;
import com.fire.photoselector.models.PhotoSelectorSetting;
import com.fire.photoselector.utils.GetFileSize;
import com.fire.photoselector.utils.ScreenUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fire.photoselector.models.PhotoMessage.SELECTED_PHOTOS;
import static com.fire.photoselector.models.PhotoSelectorSetting.COLUMN_COUNT;
import static com.fire.photoselector.models.PhotoSelectorSetting.IS_SELECTED_FULL_IMAGE;
import static com.fire.photoselector.models.PhotoSelectorSetting.SELECTED_FULL_IMAGE;
import static com.fire.photoselector.models.PhotoSelectorSetting.LAST_MODIFIED_LIST;
import static com.fire.photoselector.models.PhotoSelectorSetting.MAX_PHOTO_SUM;

/**
 * Created by Fire on 2017/4/8.
 */

public class PhotoSelectorActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "PhotoSelectorActivity";
    private static final int REQUEST_PREVIEW_PHOTO = 100;
    /**
     * 保存相册目录名和相册所有照片路径
     */
    private HashMap<String, List<String>> photoGroupMap = new HashMap<>();
    /**
     * 保存相册目录名
     */
    private List<ImageFolderBean> photoFolders = new ArrayList<>();
    /**
     * 照片列表
     */
    private PhotoListAdapter photoListAdapter;
    /**
     * 目录列表
     */
    private FolderListAdapter folderListAdapter;
    private TextView tvCancel;
    private Button btSelectOK;
    private Button btPreviewImage;
    private Button btSelectFullImage;
    private TextView tvAlbumName;
    private ImageView ivAlbumArrow;
    private RecyclerView rvFolderList;
    private View vAlpha;
    private List<String> chileList;
    private List<String> value;
    private List<String> photoFolder;
    private ArrayList<String> photoList;
    private RecyclerView rvPhotoList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_selector);
        getImages();
        rvPhotoList = (RecyclerView) findViewById(R.id.rv_photo_list);
        tvCancel = (TextView) findViewById(R.id.tv_select_cancel);
        tvAlbumName = (TextView) findViewById(R.id.tv_album_name);
        ivAlbumArrow = (ImageView) findViewById(R.id.iv_album_arrow);
        btSelectOK = (Button) findViewById(R.id.bt_select_ok);
        btPreviewImage = (Button) findViewById(R.id.bt_preview_image);
        btSelectFullImage = (Button) findViewById(R.id.bt_select_full_image);
        rvFolderList = (RecyclerView) findViewById(R.id.rv_folder_list);
        vAlpha = findViewById(R.id.v_alpha);
        tvCancel.setOnClickListener(this);
        tvAlbumName.setOnClickListener(this);
        ivAlbumArrow.setOnClickListener(this);
        btSelectOK.setOnClickListener(this);
        btPreviewImage.setOnClickListener(this);
        btSelectFullImage.setOnClickListener(this);
        vAlpha.setOnClickListener(this);
        Intent intent = getIntent();
        SELECTED_PHOTOS = intent.getStringArrayListExtra(LAST_MODIFIED_LIST);
        if (SELECTED_PHOTOS.size() == 0) {
            IS_SELECTED_FULL_IMAGE = false;
        }
        photoListAdapter = new PhotoListAdapter(this, photoGroupMap.get("相机胶卷"));
        if (COLUMN_COUNT <= 1) {
            rvPhotoList.setLayoutManager(new LinearLayoutManager(this));
        } else {
            rvPhotoList.setLayoutManager(new GridLayoutManager(this, COLUMN_COUNT));
        }
        rvPhotoList.setAdapter(photoListAdapter);
        photoListAdapter.setOnRecyclerViewItemClickListener(new OnPhotoListClick());
        rvFolderList.setLayoutManager(new LinearLayoutManager(this));
        ViewGroup.LayoutParams lp = rvFolderList.getLayoutParams();
        lp.height = (int) (ScreenUtil.getScreenHeight(this) * 0.618);
        rvFolderList.setLayoutParams(lp);
        folderListAdapter = new FolderListAdapter(this, photoFolders);
        folderListAdapter.setOnRecyclerViewItemClickListener(new OnFolderListClick());
        rvFolderList.setAdapter(folderListAdapter);
        changeOKButtonStatus();
    }

    /**
     * 扫描手机中所有图片
     */
    private void getImages() {
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(imageUri, null, null, null, sortOrder);
        if (cursor == null) {
            return;
        }
        photoFolder = new ArrayList<>();
        photoGroupMap.put(getString(R.string.all_photos), photoFolder);
        while (cursor.moveToNext()) {
            //获取图片的路径
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            photoGroupMap.get(getString(R.string.all_photos)).add(path);
            //获取该图片的父路径名
            String parentName = new File(path).getParentFile().getName();
            //根据父路径名将图片放入到mGroupMap中
            if (photoGroupMap.containsKey(parentName)) {
                photoGroupMap.get(parentName).add(path);
            } else {
                chileList = new ArrayList<>();
                chileList.add(path);
                photoGroupMap.put(parentName, chileList);
            }
        }
        //扫描图片完成
        cursor.close();
        photoFolders.addAll(subGroupOfImage(photoGroupMap));
    }

    private List<ImageFolderBean> subGroupOfImage(HashMap<String, List<String>> mGroupMap) {
        List<ImageFolderBean> list = new ArrayList<>();
        ImageFolderBean imageFolderBean;
        for (Map.Entry<String, List<String>> entry : mGroupMap.entrySet()) {
            imageFolderBean = new ImageFolderBean();
            String key = entry.getKey();
            if (key.equals(getString(R.string.all_photos))) {
                imageFolderBean.setSelected(true);
            } else {
                imageFolderBean.setSelected(false);
            }
            value = entry.getValue();
            imageFolderBean.setFolderName(key);
            imageFolderBean.setImageCounts(value.size());
            imageFolderBean.setImagePaths(value);
            if (key.equals(getString(R.string.all_photos))) {
                list.add(0, imageFolderBean);
            } else {
                list.add(imageFolderBean);
            }
        }
        return list;
    }

    @Override
    public void onClick(View v) {
        if (v == tvCancel) {// 取消
            setResult(RESULT_CANCELED);
            finish();
        } else if (v == tvAlbumName) {// 选择相册
            toggleFolderList();
        } else if (v == btSelectOK) {// 确定按钮
            if (SELECTED_PHOTOS.size() != 0) {
                ArrayList<String> image = new ArrayList<>();
                image.addAll(SELECTED_PHOTOS);
                Intent intent = new Intent();
                intent.putExtra(LAST_MODIFIED_LIST, image);
                intent.putExtra(SELECTED_FULL_IMAGE, PhotoSelectorSetting.IS_SELECTED_FULL_IMAGE);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if (v == btPreviewImage) {// 预览照片
            if (SELECTED_PHOTOS.size() != 0) {
                Intent intent = new Intent(this, PhotoViewActivity.class);
                photoList = new ArrayList<>();
                photoList.addAll(SELECTED_PHOTOS);
                intent.putExtra("PhotoList", photoList);
                startActivityForResult(intent, REQUEST_PREVIEW_PHOTO);
            }
        } else if (v == btSelectFullImage) {// 选择全图
            PhotoSelectorSetting.IS_SELECTED_FULL_IMAGE = !PhotoSelectorSetting.IS_SELECTED_FULL_IMAGE;
            changeOKButtonStatus();
        } else if (v == vAlpha) {// 点击相册列表外部
            toggleFolderList();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            PhotoSelectorSetting.SCREEN_RATIO = (float) vAlpha.getWidth() / vAlpha.getHeight();
        }
    }

    private class OnFolderListClick implements FolderListAdapter.OnRecyclerViewItemClickListener {

        @Override
        public void onRecyclerViewItemClick(View v, int position) {
            toggleFolderSelected(position);
            photoFolder = photoGroupMap.get(photoFolders.get(position).getFolderName());
            photoListAdapter.setData(photoFolder);
            folderListAdapter.notifyDataSetChanged();
            tvAlbumName.setText(photoFolders.get(position).getFolderName());
            toggleFolderList();
            rvPhotoList.smoothScrollToPosition(0);
        }
    }

    private class OnPhotoListClick implements PhotoListAdapter.OnRecyclerViewItemClickListener {

        @Override
        public void onRecyclerViewItemClick(View v, int position) {
            if (v.getId() == R.id.iv_photo_checked) {
                boolean photoSelected = PhotoMessage.togglePhotoSelected(photoFolder.get(position));
                if (photoSelected) {
                    changeOKButtonStatus();
                } else {
                    String string = getString(R.string.photo_sum_max);
                    String format = String.format(string, MAX_PHOTO_SUM);
                    Toast.makeText(PhotoSelectorActivity.this, format, Toast.LENGTH_SHORT).show();
                }
                photoListAdapter.notifyDataSetChanged();
            } else {
                Intent intent = new Intent(PhotoSelectorActivity.this, PhotoViewActivity.class);
                photoList = new ArrayList<>();
                photoList.addAll(photoFolder);
                intent.putExtra("PhotoList", photoList);
                intent.putExtra("Index", position);
                startActivityForResult(intent, REQUEST_PREVIEW_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PREVIEW_PHOTO:
                if (resultCode == RESULT_OK) {
                    photoList = new ArrayList<>();
                    photoList.addAll(SELECTED_PHOTOS);
                    Intent intent = new Intent();
                    intent.putExtra(LAST_MODIFIED_LIST, photoList);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                photoListAdapter.notifyDataSetChanged();
                changeOKButtonStatus();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (rvFolderList.isShown()) {
            toggleFolderList();
        } else {
            super.onBackPressed();
        }
    }

    private void toggleFolderList() {
        Animation animation;
        if (rvFolderList.isShown()) {
            rvFolderList.setVisibility(View.GONE);
            vAlpha.setVisibility(View.INVISIBLE);
            ivAlbumArrow.setImageResource(R.drawable.ic_arrow_down_yellow);
            animation = AnimationUtils.loadAnimation(this, R.anim.popup_hidden_anim);
        } else {
            rvFolderList.setVisibility(View.VISIBLE);
            vAlpha.setVisibility(View.VISIBLE);
            ivAlbumArrow.setImageResource(R.drawable.ic_arrow_up_yellow);
            animation = AnimationUtils.loadAnimation(this, R.anim.popup_show_anim);
        }
        rvFolderList.setAnimation(animation);
        folderListAdapter.notifyDataSetChanged();
    }

    private void toggleFolderSelected(int position) {
        for (ImageFolderBean photoFolder : photoFolders) {
            photoFolder.setSelected(false);
        }
        photoFolders.get(position).setSelected(true);
    }

    private void changeOKButtonStatus() {
        if (SELECTED_PHOTOS.size() == 0) {
            btSelectOK.setBackgroundResource(R.drawable.button_unclickable);
            btSelectOK.setTextColor(getResources().getColor(R.color.textSecondColor));
            btSelectOK.setText(getString(R.string.ok));
            btPreviewImage.setTextColor(getResources().getColor(R.color.textSecondColor));
        } else {
            btSelectOK.setBackgroundResource(R.drawable.button_clickable);
            btSelectOK.setTextColor(getResources().getColor(R.color.textWriteColor));
            String string = getResources().getString(R.string.ok_with_number);
            String format = String.format(string, SELECTED_PHOTOS.size());
            btSelectOK.setText(format);
            btPreviewImage.setTextColor(getResources().getColor(R.color.textBlackColor));
        }
        if (PhotoSelectorSetting.IS_SELECTED_FULL_IMAGE) {
            long size = 0;
            for (String selectedPhoto : SELECTED_PHOTOS) {
                size += new File(selectedPhoto).length();
            }
            float f = (float) size / (1024 * 1024);
            Log.i(TAG, "changeOKButtonStatus: " + f);
            String string = getString(R.string.full_image_with_size);
            String format = String.format(string, GetFileSize.getSize(size));
            btSelectFullImage.setText(format);
            Drawable drawable = getResources().getDrawable(R.drawable.choose_full_image_checked);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            btSelectFullImage.setCompoundDrawables(drawable, null, null, null);
        } else {
            btSelectFullImage.setText(getString(R.string.full_image));
            Drawable drawable = getResources().getDrawable(R.drawable.choose_full_image_unchecked);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            btSelectFullImage.setCompoundDrawables(drawable, null, null, null);
        }
    }
}
