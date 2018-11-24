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
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import java.util.Collections;
import java.util.List;
import toro.v4.Media;
import toro.v4.exo.factory.MediaSourceFactoryProvider;

/**
 * @author eneim (2018/11/10).
 */
@SuppressWarnings("WeakerAccess") //
public class DefaultMediaSourceFactoryProvider implements MediaSourceFactoryProvider {

  @NonNull protected final Context context;
  @NonNull protected final DataSource.Factory upstreamFactory;
  @NonNull protected final DataSource.Factory dataSourceFactory;
  @Nullable protected final Cache mediaCache;

  public DefaultMediaSourceFactoryProvider(@NonNull Context context,
      @NonNull DataSource.Factory upstreamFactory, @Nullable Cache mediaCache) {
    this.context = context.getApplicationContext();
    this.upstreamFactory = upstreamFactory;
    this.mediaCache = mediaCache;

    if (this.mediaCache != null) {
      this.dataSourceFactory = new CacheDataSourceFactory( //
          this.mediaCache, this.upstreamFactory, //
          new FileDataSourceFactory(), //
          /* cacheWriteDataSinkFactory= */ null, //
          CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, //
          /* eventListener= */ null //
      );
    } else {
      this.dataSourceFactory = this.upstreamFactory;
    }
  }

  @Override public AdsMediaSource.MediaSourceFactory provideMediaSourceFactory(Media media) {
    final AdsMediaSource.MediaSourceFactory result;
    @C.ContentType int type = Util.inferContentType(media.getUri(), media.getExtension());
    switch (type) {
      case C.TYPE_DASH:
        result = new DashMediaSource.Factory(dataSourceFactory) //
            .setManifestParser( //
                new FilteringManifestParser<>( //
                    new DashManifestParser(), getOfflineStreamKeys(media.getUri()) //
                ) //
            );
        break;
      case C.TYPE_SS:
        result = new SsMediaSource.Factory(dataSourceFactory) //
            .setManifestParser( //
                new FilteringManifestParser<>( //
                    new SsManifestParser(), getOfflineStreamKeys(media.getUri()) //
                ) //
            );
        break;
      case C.TYPE_HLS:
        result = new HlsMediaSource.Factory(dataSourceFactory) //
            .setPlaylistParserFactory( //
                new DefaultHlsPlaylistParserFactory(getOfflineStreamKeys(media.getUri())) //
            );
        break;
      case C.TYPE_OTHER:
        result = new ExtractorMediaSource.Factory(dataSourceFactory);
        break;
      default: {
        throw new IllegalStateException("Unsupported type: " + type);
      }
    }
    return result;
  }

  @SuppressWarnings({ "unused", "WeakerAccess" })
  protected List<StreamKey> getOfflineStreamKeys(Uri uri) {
    return Collections.<StreamKey>emptyList();
  }
}
