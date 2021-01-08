package com.pingpong;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.pingpong.network.RetrofitClient;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@GlideModule
public class AppGlideModule extends com.bumptech.glide.module.AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        int memoryCacheSizeBytes = 1024 * 1024 * 60; // 60mb
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, memoryCacheSizeBytes));
        builder.setDefaultRequestOptions(requestOptions());
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient client = RetrofitClient.getUnsafeOkHttpClient(false).build();
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        glide.getRegistry().replace(GlideUrl.class, InputStream.class, factory);
    }

    private static RequestOptions requestOptions(){
        return new RequestOptions()
                .signature(new ObjectKey(System.currentTimeMillis() / (60 * 60 * 1000)))
               //.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                //.centerCrop()
               // .encodeFormat(Bitmap.CompressFormat.PNG)
                //.encodeQuality(100)
                .onlyRetrieveFromCache(false)
                .error(R.drawable.album_art_placeholder_large)
                .placeholder(R.drawable.album_art_placeholder_large)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .format(DecodeFormat.PREFER_ARGB_8888);
    }
}
