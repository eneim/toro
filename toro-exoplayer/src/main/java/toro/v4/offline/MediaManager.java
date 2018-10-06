/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toro.v4.offline;

import android.content.Context;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.BuildConfig;
import java.io.File;

public final class MediaManager {

  private static final String DOWNLOAD_ACTION_FILE = "toro_download_actions";
  private static final String DOWNLOAD_TRACKER_ACTION_FILE = "toro_tracked_actions";
  private static final String DOWNLOAD_CONTENT_DIRECTORY = "toro_content";
  private static final int MAX_SIMULTANEOUS_DOWNLOADS = 2;

  private File downloadDirectory;
  private Cache downloadCache;
  private DownloadManager downloadManager;
  private DownloadTracker downloadTracker;

  @SuppressWarnings("FieldCanBeLocal") //
  private final String userAgent;
  private final Context context;

  @SuppressWarnings("WeakerAccess") //
  public final TransferListener transferListener;
  @SuppressWarnings("WeakerAccess") //
  public final DataSource.Factory httpDataSource;
  public final DataSource.Factory cacheDataSource;

  public MediaManager(Context context) {
    this.context = context.getApplicationContext();
    this.userAgent = Util.getUserAgent(this.context,
        "Toro " + BuildConfig.VERSION_NAME + ", ExoPlayer extension.");

    this.transferListener = new DefaultBandwidthMeter();
    this.httpDataSource = new DefaultHttpDataSourceFactory(this.userAgent, this.transferListener);

    DefaultDataSourceFactory upstreamFactory =
        new DefaultDataSourceFactory(this.context, this.httpDataSource);
    this.cacheDataSource = buildCacheDataSource(upstreamFactory, getDownloadCache());
  }

  @SuppressWarnings("WeakerAccess") //
  public DownloadManager getDownloadManager() {
    initDownloadManager();
    return downloadManager;
  }

  public DownloadTracker getDownloadTracker() {
    initDownloadManager();
    return downloadTracker;
  }

  private synchronized void initDownloadManager() {
    if (downloadManager == null) {
      DownloaderConstructorHelper downloaderConstructorHelper =
          new DownloaderConstructorHelper(getDownloadCache(), this.httpDataSource);

      downloadManager = new DownloadManager( //
          downloaderConstructorHelper, //
          MAX_SIMULTANEOUS_DOWNLOADS,  //
          DownloadManager.DEFAULT_MIN_RETRY_COUNT, //
          new File(getDownloadDirectory(), DOWNLOAD_ACTION_FILE) //
      );

      downloadTracker = new DownloadTracker( //
          /* context= */ this.context, this.cacheDataSource, //
          new File(getDownloadDirectory(), DOWNLOAD_TRACKER_ACTION_FILE) //
      );

      downloadManager.addListener(downloadTracker);
    }
  }

  private synchronized Cache getDownloadCache() {
    if (downloadCache == null) {
      File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
      downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
    }
    return downloadCache;
  }

  private synchronized File getDownloadDirectory() {
    if (downloadDirectory == null) {
      downloadDirectory = this.context.getExternalFilesDir(null);
      if (downloadDirectory == null) {
        downloadDirectory = this.context.getFilesDir();
      }
    }
    return downloadDirectory;
  }

  private static CacheDataSourceFactory buildCacheDataSource(
      DefaultDataSourceFactory upstreamFactory, Cache cache) {
    return new CacheDataSourceFactory(cache, upstreamFactory, new FileDataSourceFactory(),
        /* cacheWriteDataSinkFactory= */ null, CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
        /* eventListener= */ null);
  }
}
