package com.fire.photoselector.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fire.photoselector.R;
import com.fire.photoselector.adapter.PhotoViewAdapter;
import com.fire.photoselector.models.PhotoMessage;

import java.util.ArrayList;

import static com.fire.photoselector.models.PhotoMessage.PHOTOS_LIST_TRANSFER;
import static com.fire.photoselector.models.PhotoMessage.SELECTED_PHOTOS;
import static com.fire.photoselector.models.PhotoSelectorSetting.LAST_MODIFIED_LIST;
import static com.fire.photoselector.models.PhotoSelectorSetting.MAX_PHOTO_SUM;

/**
 * Created by Fire on 2017/4/11.
 */

public class PhotoViewActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "PhotoViewActivity";
    private ImageView ivSelectCancel;
    private Button btSelectOK;
    private PhotoViewAdapter photoViewAdapter;
    private ImageView ivPhotoSelected;
    private ViewPager vpPhotoView;
    private TextView tvPhotoIndicator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        ivSelectCancel = (ImageView) findViewById(R.id.iv_select_cancel);
        tvPhotoIndicator = (TextView) findViewById(R.id.tv_photo_indicator);
        ivPhotoSelected = (ImageView) findViewById(R.id.iv_photo_selected);
        btSelectOK = (Button) findViewById(R.id.bt_select_ok);
        vpPhotoView = (ViewPager) findViewById(R.id.vp_photo_view);
        ivSelectCancel.setOnClickListener(this);
        btSelectOK.setOnClickListener(this);
        ivPhotoSelected.setOnClickListener(this);
        Intent intent = getIntent();
        int index = intent.getIntExtra("Index", 0);
        photoViewAdapter = new PhotoViewAdapter(this, PHOTOS_LIST_TRANSFER);
        vpPhotoView.setAdapter(photoViewAdapter);
        vpPhotoView.setCurrentItem(index, false);
        vpPhotoView.addOnPageChangeListener(new PageChangeListener());
        changePhotoSelectStatus(vpPhotoView.getCurrentItem());
        changeOKButtonStatus();
        changePhotoIndicator(index + 1);
    }

    @Override
    public void onClick(View v) {
        if (v == ivSelectCancel) {
            finish();
        } else if (v == btSelectOK) {
            if (SELECTED_PHOTOS.size() != 0) {
                ArrayList<String> image = new ArrayList<>();
                image.addAll(SELECTED_PHOTOS);
                Intent intent = new Intent();
                intent.putExtra(LAST_MODIFIED_LIST, image);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if (v == ivPhotoSelected) {
            // 当前ViewPager页面脚标
            int position = vpPhotoView.getCurrentItem();
            // 添加/删除当前页面照片
            boolean result = PhotoMessage.togglePhotoSelected(PHOTOS_LIST_TRANSFER.get(position));
            if (result) {
                // 添加/删除成功
                changePhotoSelectStatus(position);
            } else {
                // 添加失败,超出可选照片上限
                ivPhotoSelected.setImageResource(R.drawable.compose_photo_preview_default);
                String string = getString(R.string.photo_sum_max);
                String format = String.format(string, MAX_PHOTO_SUM);
                Toast.makeText(this, format, Toast.LENGTH_SHORT).show();
            }
            // 更改确定按钮文字
            changeOKButtonStatus();
        }
    }

    /**
     * ViewPager滑动监听
     */
    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            // 判断当前页面照片是否在已选集合中
            changePhotoSelectStatus(position);
            // 变更指示器状态
            changePhotoIndicator(position + 1);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    /**
     * 更改勾选按钮状态
     *
     * @param position
     */
    private void changePhotoSelectStatus(int position) {
        if (PhotoMessage.isPhotoSelected(PHOTOS_LIST_TRANSFER.get(position))) {
            ivPhotoSelected.setImageResource(R.drawable.compose_photo_preview_right);
        } else {
            ivPhotoSelected.setImageResource(R.drawable.compose_photo_preview_default);
        }
    }

    /**
     * 更改确定按钮状态
     */
    private void changeOKButtonStatus() {
        if (SELECTED_PHOTOS.size() == 0) {
            btSelectOK.setBackgroundResource(R.drawable.button_unclickable);
            btSelectOK.setTextColor(getResources().getColor(R.color.textSecondColor));
            btSelectOK.setText(getString(R.string.ok));
        } else {
            btSelectOK.setBackgroundResource(R.drawable.button_clickable);
            btSelectOK.setTextColor(getResources().getColor(R.color.textWriteColor));
            String string = getResources().getString(R.string.ok_with_number);
            String format = String.format(string, SELECTED_PHOTOS.size());
            btSelectOK.setText(format);
        }
    }

    /**
     * 更改相册指示器
     *
     * @param position
     */
    private void changePhotoIndicator(int position) {
        String string = getString(R.string.photo_sum_indicator);
        String format = String.format(string, position, PHOTOS_LIST_TRANSFER.size());
        tvPhotoIndicator.setText(format);
    }
}
