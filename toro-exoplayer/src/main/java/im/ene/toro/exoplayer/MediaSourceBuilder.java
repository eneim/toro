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

package im.ene.toro.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.C.ContentType;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;

import static android.text.TextUtils.isEmpty;
import static com.google.android.exoplayer2.util.Util.inferContentType;

/**
 * @author eneim (2018/01/24).
 * @since 3.4.0
 */

public interface MediaSourceBuilder {

  @NonNull MediaSource buildMediaSource(@NonNull Context context, @NonNull Uri uri,
      @Nullable String fileExt, @Nullable Handler handler,
      @NonNull DataSource.Factory manifestDataSourceFactory,
      @NonNull DataSource.Factory mediaDataSourceFactory,
      @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
      @Nullable MediaSourceEventListener listener);

  MediaSourceBuilder DEFAULT = new MediaSourceBuilder() {
    @NonNull @Override
    public MediaSource buildMediaSource(@NonNull Context context, @NonNull Uri uri,
        @Nullable String ext, @Nullable Handler handler,
        @NonNull DataSource.Factory manifestDataSourceFactory,
        @NonNull DataSource.Factory mediaDataSourceFactory,
        @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
        MediaSourceEventListener listener) {
      @ContentType int type = isEmpty(ext) ? inferContentType(uri) : inferContentType("." + ext);
      MediaSource result;
      switch (type) {
        case C.TYPE_SS:
          SsMediaSource.Factory factory =
              new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                  manifestDataSourceFactory);
          if (drmSessionManager != null) factory.setDrmSessionManager(drmSessionManager);
          result = factory.createMediaSource(uri);
          break;
        case C.TYPE_DASH:
          DashMediaSource.Factory factory1 = new DashMediaSource.Factory(
              new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
              manifestDataSourceFactory);
          if (drmSessionManager != null) factory1.setDrmSessionManager(drmSessionManager);
          result = factory1.createMediaSource(uri);
          break;
        case C.TYPE_HLS:
          HlsMediaSource.Factory factory2 = new HlsMediaSource.Factory(mediaDataSourceFactory);
          if (drmSessionManager != null) factory2.setDrmSessionManager(drmSessionManager);
          result =  factory2.createMediaSource(uri);
          break;
        case C.TYPE_OTHER:
          ProgressiveMediaSource.Factory factory3 =
              new ProgressiveMediaSource.Factory(mediaDataSourceFactory);
          if (drmSessionManager != null) factory3.setDrmSessionManager(drmSessionManager);
          result =  factory3.createMediaSource(uri);
          break;
        default:
          throw new IllegalStateException("Unsupported type: " + type);
      }

      result.addEventListener(handler, listener);
      return result;
    }
  };

  MediaSourceBuilder LOOPING = new MediaSourceBuilder() {

    @NonNull @Override
    public MediaSource buildMediaSource(@NonNull Context context, @NonNull Uri uri,
        @Nullable String fileExt, @Nullable Handler handler,
        @NonNull DataSource.Factory manifestDataSourceFactory,
        @NonNull DataSource.Factory mediaDataSourceFactory,
        @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
        @Nullable MediaSourceEventListener listener) {
      return new LoopingMediaSource(
          DEFAULT.buildMediaSource(context, uri, fileExt, handler, manifestDataSourceFactory,
              mediaDataSourceFactory, drmSessionManager, listener));
    }
  };
}
