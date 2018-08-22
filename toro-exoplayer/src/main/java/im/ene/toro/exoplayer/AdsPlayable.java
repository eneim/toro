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
import android.view.View;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.annotations.Beta;

/**
 * Only use this inside a {@link AdsExoPlayerViewHelper}.
 *
 * @author eneim (2018/08/22).
 * @since 3.6.0.2802
 */
@Beta //
class AdsPlayable extends ExoPlayable {

  static class FactoryImpl implements AdsMediaSource.MediaSourceFactory {

    final ExoCreator creator;
    final ToroPlayer player;

    FactoryImpl(ExoCreator creator, ToroPlayer player) {
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

  private final AdsLoader adsLoader;
  private final FactoryImpl factory;

  /* package */ AdsPlayable(ExoCreator creator, Uri uri, String fileExt, ToroPlayer player,
      AdsLoader adsLoader) {
    super(creator, uri, fileExt);
    this.adsLoader = adsLoader;
    this.factory = new FactoryImpl(this.creator, player);
  }

  @Override public void prepare(boolean prepareSource) {
    this.mediaSource =
        createAdsMediaSource(creator, mediaUri, fileExt, factory.player, adsLoader, factory);
    super.prepare(prepareSource);
  }

  private static MediaSource createAdsMediaSource(ExoCreator creator, Uri uri, String fileExt,
      ToroPlayer player, AdsLoader adsLoader, AdsMediaSource.MediaSourceFactory factory) {
    MediaSource original = creator.createMediaSource(uri, fileExt);
    View playerView = player.getPlayerView();
    if (!(playerView instanceof PlayerView)) {
      throw new IllegalArgumentException("Require PlayerView");
    }

    return new AdsMediaSource(original, factory, adsLoader,
        ((PlayerView) playerView).getOverlayFrameLayout());
  }
}
