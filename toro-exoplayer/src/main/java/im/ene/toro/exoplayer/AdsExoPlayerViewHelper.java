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
import android.view.ViewGroup;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.annotations.Beta;
import im.ene.toro.helper.ToroPlayerHelper;

/**
 * A {@link ToroPlayerHelper} to integrate ExoPlayer IMA Extension. Work together with {@link AdsPlayable}.
 *
 * @author eneim (2018/08/22).
 * @since 3.6.0.2802
 */
@Beta //
public class AdsExoPlayerViewHelper extends ExoPlayerViewHelper {

  private static AdsPlayable createPlayable(  ///
      ToroPlayer player,      //
      ExoCreator creator,     //
      Uri contentUri,         //
      String fileExt,         //
      AdsLoader adsLoader,    //
      ViewGroup adContainer   //
  ) {
    return new AdsPlayable(creator, contentUri, fileExt, player, adsLoader, adContainer);
  }

  private static AdsPlayable createPlayable(  //
      ToroPlayer player,      //
      Config config,          //
      Uri contentUri,         //
      String fileExt,         //
      AdsLoader adsLoader,    //
      ViewGroup adContainer   //
  ) {
    Context context = player.getPlayerView().getContext();
    return createPlayable(player, ToroExo.with(context).getCreator(config), contentUri, fileExt,
        adsLoader, adContainer);
  }

  private static AdsPlayable createPlayable(  //
      ToroPlayer player,      //
      Uri contentUri,         //
      String fileExt,         //
      AdsLoader adsLoader,    //
      ViewGroup adContainer   //
  ) {
    Context context = player.getPlayerView().getContext();
    return createPlayable(player, ToroExo.with(context).getDefaultCreator(), contentUri, fileExt,
        adsLoader, adContainer);
  }

  // Neither ExoCreator nor Config are provided.

  /**
   * Create new {@link AdsExoPlayerViewHelper} for a {@link ToroPlayer} and {@link AdsLoader}.
   *
   * @param adContainer if {@code null} then overlay of {@link PlayerView} will be used.
   */
  public AdsExoPlayerViewHelper(        //
      @NonNull ToroPlayer player,       //
      @NonNull Uri uri,                 //
      @Nullable String fileExt,         //
      @NonNull AdsLoader adsLoader,     //
      @Nullable ViewGroup adContainer   //
  ) {
    super(player, createPlayable(player, uri, fileExt, adsLoader, adContainer));
  }

  // ExoCreator is provided.

  /**
   * Create new {@link AdsExoPlayerViewHelper} for a {@link ToroPlayer} and {@link AdsLoader}.
   *
   * @param adContainer if {@code null} then overlay of {@link PlayerView} will be used.
   */
  public AdsExoPlayerViewHelper(        //
      @NonNull ToroPlayer player,       //
      @NonNull Uri uri,                 //
      @Nullable String fileExt,         //
      @NonNull AdsLoader adsLoader,     //
      @Nullable ViewGroup adContainer,  //
      @NonNull ExoCreator creator       //
  ) {
    super(player, createPlayable(player, creator, uri, fileExt, adsLoader, adContainer));
  }

  // Config is provided.

  /**
   * Create new {@link AdsExoPlayerViewHelper} for a {@link ToroPlayer} and {@link AdsLoader}.
   *
   * @param adContainer if {@code null} then overlay of {@link PlayerView} will be used.
   */
  public AdsExoPlayerViewHelper(        //
      @NonNull ToroPlayer player,       //
      @NonNull Uri uri,                 //
      @Nullable String fileExt,         //
      @NonNull AdsLoader adsLoader,     //
      @Nullable ViewGroup adContainer,  //
      @NonNull Config config            //
  ) {
    super(player, createPlayable(player, config, uri, fileExt, adsLoader, adContainer));
  }
}
