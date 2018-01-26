/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.widget.Container;

/**
 * @author eneim | 6/11/17.
 *
 *         Extension of {@link ToroPlayerHelper}, aims to support {@link ExoPlayer} via its
 *         components {@link SimpleExoPlayer} and {@link SimpleExoPlayerView}.
 *
 *         {@link Deprecated}, use {@link ExoPlayerViewHelper} instead.
 */
@Deprecated //
public final class SimpleExoPlayerViewHelper extends ExoPlayerViewHelper {

  public SimpleExoPlayerViewHelper(Container container, ToroPlayer player, @NonNull Uri mediaUri) {
    super(container, player, mediaUri);
  }
}
