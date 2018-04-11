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
import android.support.annotation.NonNull;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.widget.Container;

import static im.ene.toro.exoplayer.ToroExo.with;

/**
 * An implementation of {@link ToroPlayerHelper} where the actual Player is an {@link ExoPlayer}
 * implementation. This is a bridge between ExoPlayer's callback and ToroPlayerHelper behaviors.
 *
 * @author eneim (2018/01/24).
 * @since 3.4.0
 */

public class ExoPlayerViewHelper extends BaseViewHelper<SimpleExoPlayerView> {

  @SuppressWarnings("unused") @Deprecated //
  public ExoPlayerViewHelper(Container container, @NonNull ToroPlayer player, @NonNull Uri uri) {
    this(player, uri, null, with(player.getPlayerView().getContext()).getDefaultCreator());
  }

  @SuppressWarnings("unused")
  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull Uri uri) {
    this(player, uri, null);
  }

  @SuppressWarnings("WeakerAccess")
  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull Uri uri, String extension) {
    this(player, uri, extension, with(player.getPlayerView().getContext()).getDefaultCreator());
  }

  @SuppressWarnings("WeakerAccess")
  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull Uri uri, String extension,
      @NonNull ExoCreator creator) {
    super(player, uri, extension, creator);
  }

  @NonNull @Override //
  Playable<SimpleExoPlayerView> requirePlayable(ExoCreator creator, @NonNull Uri uri, String ext) {
    return creator.createPlayable(uri, ext);
  }
}
