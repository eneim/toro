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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import im.ene.toro.ToroPlayer;
import im.ene.toro.exoplayer.ExoPlayerViewHelper;
import im.ene.toro.widget.Container;

/**
 * @author eneim (2018/01/05).
 */

public class LoopingPlayerHelper extends ExoPlayerViewHelper {

  public LoopingPlayerHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri mediaUri) {
    super(container, player, mediaUri);
  }

  @Override protected MediaSource createMediaSource(@NonNull Uri uri, @Nullable Handler handler,
      @Nullable MediaSourceEventListener listener) {
    MediaSource origin = super.createMediaSource(uri, handler, listener);
    return new LoopingMediaSource(origin);
  }
}
