package com.fire.photoselector.models;


import java.util.ArrayList;
import java.util.List;

import static com.fire.photoselector.models.PhotoSelectorSetting.MAX_PHOTO_SUM;

/**
 * Created by Fire on 2017/4/10.
 */

public class PhotoMessage {
    public static List<String> SELECTED_PHOTOS = new ArrayList<>();
    public static ArrayList<String> PHOTOS_LIST_TRANSFER = new ArrayList<>();

    public static boolean isPhotoSelected(String path) {
        return SELECTED_PHOTOS.contains(path);
    }

    public static boolean togglePhotoSelected(String path) {
        if (SELECTED_PHOTOS.contains(path)) {
            SELECTED_PHOTOS.remove(path);
            return true;
        } else {
            if (SELECTED_PHOTOS.size() == MAX_PHOTO_SUM) {
                return false;
            } else {
                SELECTED_PHOTOS.add(path);
                return true;
            }
        }
    }
}
