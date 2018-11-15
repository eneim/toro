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
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource.MediaSourceFactory;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.annotations.Beta;
import im.ene.toro.exoplayer.Playable;
import toro.v4.Media;
import toro.v4.exo.factory.ExoPlayerManager;
import toro.v4.exo.factory.MediaSourceFactoryProvider;

/**
 * @author eneim (2018/10/19).
 * @since 3.7.0.2900
 *
 * A {@link Playable} that is able to integrate with {@link AdsLoader}.
 */
@SuppressWarnings("WeakerAccess") @Beta //
public class DefaultAdsPlayable extends DefaultPlayable {

  @NonNull private final AdsLoader adsLoader;
  @NonNull private final ViewGroup adsContainer;
  @NonNull private final MediaSourceFactory adsMediaSourceFactory;

  public DefaultAdsPlayable(@NonNull Context context, @NonNull Media media,
      @NonNull MediaSourceFactoryProvider mediaSourceFactoryProvider,
      @NonNull ExoPlayerManager playerProvider, @NonNull AdsLoader adsLoader,
      @NonNull ViewGroup adsContainer, @NonNull MediaSourceFactory adsMediaSourceFactory) {
    super(context, media, mediaSourceFactoryProvider, playerProvider);
    this.adsLoader = adsLoader;
    this.adsContainer = adsContainer;
    this.adsMediaSourceFactory = adsMediaSourceFactory;
  }

  @CallSuper @Override public void prepare(boolean prepareSource) {
    this.mediaSource = createAdsMediaSource(media, adsLoader, adsContainer, mediaSourceFactory,
        adsMediaSourceFactory);
    super.prepare(prepareSource);
  }

  private static MediaSource createAdsMediaSource(Media media, AdsLoader adsLoader,
      ViewGroup adContainer, MediaSourceFactory factory, MediaSourceFactory adsMediaSourceFactory) {
    MediaSource original = factory.createMediaSource(media.getUri());
    return new AdsMediaSource(original, adsMediaSourceFactory, adsLoader, adContainer);
  }

  // Used for Ads only.
  public static class AdsMediaSourceFactory implements MediaSourceFactory {

    private final DataSource.Factory dataSourceFactory;

    public AdsMediaSourceFactory(DataSource.Factory dataSourceFactory) {
      this.dataSourceFactory = dataSourceFactory;
    }

    @Override public MediaSource createMediaSource(Uri uri) {
      @C.ContentType int type = Util.inferContentType(uri);
      switch (type) {
        case C.TYPE_DASH:
          return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        case C.TYPE_SS:
          return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        case C.TYPE_HLS:
          return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        case C.TYPE_OTHER:
          return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        default:
          throw new IllegalStateException("Unsupported type: " + type);
      }
    }

    @Override public int[] getSupportedTypes() {
      return new int[] { C.TYPE_DASH, C.TYPE_HLS, C.TYPE_OTHER };
    }
  }
}
