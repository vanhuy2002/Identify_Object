package com.example.identify_object;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.Target;
import com.example.identify_object.Adapter.HistoryAdapter;

public interface iLoadImage {
    Target<Drawable> setImage(RequestManager glideRequest, HistoryAdapter.CreateViewHolder holder, Uri uri);
}
