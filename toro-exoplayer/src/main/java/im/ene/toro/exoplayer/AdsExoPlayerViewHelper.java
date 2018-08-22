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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import im.ene.toro.ToroPlayer;
import im.ene.toro.annotations.Beta;

/**
 * @author eneim (2018/08/22).
 * @since 3.6.0.2802
 */
@Beta //
public class AdsExoPlayerViewHelper extends ExoPlayerViewHelper {

  private static AdsPlayable createPlayable(ToroPlayer player, ExoCreator creator, Uri contentUri,
      String fileExt, AdsLoader adsLoader) {
    return new AdsPlayable(creator, contentUri, fileExt, player, adsLoader);
  }

  private static AdsPlayable createPlayable(ToroPlayer player, Config config, Uri contentUri,
      String fileExt, AdsLoader adsLoader) {
    Context context = player.getPlayerView().getContext();
    return createPlayable(player, ToroExo.with(context).getCreator(config), contentUri, fileExt,
        adsLoader);
  }

  private static AdsPlayable createPlayable(ToroPlayer player, Uri contentUri, String fileExt,
      AdsLoader adsLoader) {
    Context context = player.getPlayerView().getContext();
    return createPlayable(player, ToroExo.with(context).getDefaultCreator(), contentUri, fileExt,
        adsLoader);
  }

  public AdsExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull Uri uri,
      @Nullable String fileExt, @Nullable AdsLoader adsLoader) {
    super(player, createPlayable(player, uri, fileExt, adsLoader));
  }

  public AdsExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull Uri uri,
      @Nullable String fileExt, @NonNull ExoCreator creator, AdsLoader adsLoader) {
    super(player, createPlayable(player, creator, uri, fileExt, adsLoader));
  }

  public AdsExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull Uri uri,
      @Nullable String fileExt, @NonNull Config config, AdsLoader adsLoader) {
    super(player, createPlayable(player, config, uri, fileExt, adsLoader));
  }
}
