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

package im.ene.toro.sample.custom;

import android.net.Uri;
import android.support.annotation.NonNull;
import im.ene.toro.ToroPlayer;
import im.ene.toro.exoplayer.Config;
import im.ene.toro.exoplayer.ExoPlayerViewHelper;
import im.ene.toro.exoplayer.MediaSourceBuilder;

/**
 * @author eneim (2018/01/05).
 */

class LoopingPlayerHelper extends ExoPlayerViewHelper {

  private static Config loopingConfig =
      new Config.Builder().setMediaSourceBuilder(MediaSourceBuilder.LOOPING).build();

  LoopingPlayerHelper(@NonNull ToroPlayer player, @NonNull Uri mediaUri) {
    // customized using Looping media source builder
    this(player, mediaUri, null);
  }

  LoopingPlayerHelper(@NonNull ToroPlayer player, @NonNull Uri mediaUri, String extension) {
    super(player, mediaUri, extension, loopingConfig);
  }
}
