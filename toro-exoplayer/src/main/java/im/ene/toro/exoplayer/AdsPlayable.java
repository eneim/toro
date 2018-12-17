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

import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;

/**
 * A {@link Playable} that is able to integrate with {@link AdsLoader}.
 *
 * @author eneim (2018/08/22).
 * @since 3.6.0.2802
 * @deprecated Use {@link DefaultAdsPlayable} instead.
 */
@Deprecated //
public class AdsPlayable extends ExoPlayable {

  static class FactoryImpl implements AdsMediaSource.MediaSourceFactory {

    @NonNull final ExoCreator creator;
    @NonNull final ToroPlayer player;

    FactoryImpl(@NonNull ExoCreator creator, @NonNull ToroPlayer player) {
      this.creator = creator;
      this.player = player;
    }

    @Override public MediaSource createMediaSource(Uri uri) {
      return this.creator.createMediaSource(uri, null);
    }

    @Override public int[] getSupportedTypes() {
      // IMA does not support Smooth Streaming ads.
      return new int[] { C.TYPE_DASH, C.TYPE_HLS, C.TYPE_OTHER };
    }
  }

  @NonNull private final AdsLoader adsLoader;
  @NonNull private final FactoryImpl factory;
  @Nullable private final ViewGroup adsContainer;

  @SuppressWarnings("WeakerAccess")
  public AdsPlayable(ExoCreator creator, Uri uri, String fileExt, ToroPlayer player,
      @NonNull AdsLoader adsLoader, @Nullable ViewGroup adsContainer) {
    super(creator, uri, fileExt);
    this.adsLoader = adsLoader;
    this.adsContainer = adsContainer;
    this.factory = new FactoryImpl(this.creator, player);
  }

  @CallSuper
  @Override public void prepare(boolean prepareSource) {
    this.mediaSource = createAdsMediaSource(creator, mediaUri, fileExt, //
        factory.player, adsLoader, adsContainer, factory);
    super.prepare(prepareSource);
  }

  private static MediaSource createAdsMediaSource(ExoCreator creator, Uri uri, String fileExt,
      ToroPlayer player, AdsLoader adsLoader, ViewGroup adContainer,
      AdsMediaSource.MediaSourceFactory factory) {
    MediaSource original = creator.createMediaSource(uri, fileExt);
    View playerView = player.getPlayerView();
    if (!(playerView instanceof PlayerView)) {
      throw new IllegalArgumentException("Require PlayerView");
    }

    return new AdsMediaSource(original, factory, adsLoader,
        adContainer == null ? ((PlayerView) playerView).getOverlayFrameLayout() : adContainer);
  }
}
