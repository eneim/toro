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

package toro.v4.exo;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import im.ene.toro.annotations.Beta;
import java.io.File;
import java.util.Collections;
import java.util.List;
import toro.v4.exo.factory.Downloader;
import toro.v4.exo.factory.DownloaderProvider;
import toro.v4.exo.factory.MediaSourceFactoryProvider;

/**
 * @author eneim (2018/10/11).
 *
 * Implementation note: an Implementation of {@link MediaSourceFactoryProvider} that plans to
 * provide Offline support should consider to also implement {@link DownloaderProvider}. The
 * Downloader will keep track of downloaded Uri and use it in {@link MediaSource} creation.
 *
 * Dependency Graph:
 * [MediaSourceFactoryProvider] depends on [Downloader], [Cache], [DataSource.Factory]
 */
@SuppressWarnings({ "WeakerAccess", "unused" }) //
@Beta public class DownloadableMediaSourceFactoryProvider extends DefaultMediaSourceFactoryProvider
    implements DownloaderProvider {

  // Below: adapt from ExoPlayer demo.
  private static final String DOWNLOAD_ACTION_FILE = "toro_download_actions";
  private static final String DOWNLOAD_TRACKER_ACTION_FILE = "toro_tracked_actions";
  private static final int MAX_SIMULTANEOUS_DOWNLOADS = 2;

  @Nullable protected final File downloadDir;

  DownloadManager downloadManager;
  Downloader downloader; // depends on CacheDataSource as well ...

  public DownloadableMediaSourceFactoryProvider(@NonNull Context context,
      @NonNull DataSource.Factory upstreamFactory, @Nullable Cache mediaCache,
      @Nullable File downloadDir) {
    super(context, upstreamFactory, mediaCache);
    this.downloadDir = downloadDir;
    if (this.mediaCache != null && this.downloadDir != null) {
      DownloaderConstructorHelper downloaderConstructorHelper =
          new DownloaderConstructorHelper(this.mediaCache, this.upstreamFactory);

      downloadManager = new DownloadManager( //
          downloaderConstructorHelper, //
          MAX_SIMULTANEOUS_DOWNLOADS,  //
          DownloadManager.DEFAULT_MIN_RETRY_COUNT, //
          new File(this.downloadDir, DOWNLOAD_ACTION_FILE) //
      );

      downloader = new DownloaderImpl( //
          /* context= */ this.context, this.dataSourceFactory, //
          new File(this.downloadDir, DOWNLOAD_TRACKER_ACTION_FILE) //
      );

      downloadManager.addListener(downloader);
    }
  }

  @Override protected List<StreamKey> getOfflineStreamKeys(Uri uri) {
    return this.downloader == null ? Collections.<StreamKey>emptyList()
        : this.downloader.getOfflineStreamKeys(uri);
  }

  @Nullable @Override public Downloader provideDownloader() {
    return this.downloader;
  }

  @Nullable @Override public DownloadManager provideDownloadManager() {
    return this.downloadManager;
  }
}
