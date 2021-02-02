package com.pingpong.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class CacheDataSourceFactory implements DataSource.Factory {

    private static final String TAG = "CacheDataSourceFactory";
    private static final long DEFAULT_MAX_CACHE_SIZE = 20 * 1024 * 1024;
    private static final long DEFAULT_MAX_FILE_SIZE = 20 * 1024 * 1024;
    private final Context context;
    private final DataSource.Factory defaultDataSourceFactory;
    private long maxCacheSize;
    private long maxFileSize;

    public CacheDataSourceFactory(Context context, DefaultBandwidthMeter bandwidthMeter, long maxCacheSize, long maxFileSize) {
        this.context = context.getApplicationContext();
        this.maxCacheSize = maxCacheSize;
        this.maxFileSize = maxFileSize;
        if (bandwidthMeter == null) {
            bandwidthMeter = new DefaultBandwidthMeter();
        }
        defaultDataSourceFactory = buildDataSourceFactory(bandwidthMeter);
    }

    private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(context, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(context, context.getPackageName()), bandwidthMeter);
    }

    @Override
    public DataSource createDataSource() {
        if (maxCacheSize <= 0) {
            maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
        }
        if (maxFileSize <= 0) {
            maxFileSize = DEFAULT_MAX_FILE_SIZE;
        }
        //Log.d(TAG, " maxCacheSize = "+maxCacheSize+ " maxFileSize = "+ maxFileSize);
        File dir = new File(context.getCacheDir(), "media");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (SimpleCache.isCacheFolderLocked(dir)) {
            //SimpleCache.disableCacheFolderLocking();
        }
        SimpleCache simpleCache = new SimpleCache(dir, new LeastRecentlyUsedCacheEvictor(maxCacheSize));
        return new CacheDataSource(simpleCache, defaultDataSourceFactory.createDataSource(),
                new FileDataSource(), new CacheDataSink(simpleCache, maxFileSize),
                CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
    }

}