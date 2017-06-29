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

package im.ene.toro;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.util.Collection;

/**
 * @author eneim | 6/6/17.
 *
 *         A {@link PlayerStateManager} will provide the ability to resume the playback on scroll
 *         and to save/restore {@link PlaybackInfo} at various states or life cycle events to
 *         {@link Container}. An implementation of this interface must guarantee to have a one-one
 *         mapping between the playback media and its cached {@link PlaybackInfo}. Failing to do so
 *         can result in a weird playback behavior such as wrongly resuming and so on.
 */

public interface PlayerStateManager {

  /**
   * Save current {@link PlaybackInfo} of the {@link ToroPlayer} with playback order of {@code
   * order}.
   *
   * @param order the playback order of the Player.
   * @param playbackInfo latest {@link PlaybackInfo} of the Player.
   */
  void savePlaybackInfo(@IntRange(from = -1) int order, @NonNull PlaybackInfo playbackInfo);

  /**
   * Get a saved {@link PlaybackInfo} of the {@link ToroPlayer} with playback order of {@code
   * order}.
   *
   * @param order the playback order of the Player
   * @return saved {@link PlaybackInfo} for the Player, be initialized {@link PlaybackInfo} if there
   * is no saved one presents in the {@link PlayerStateManager}.
   */
  @NonNull PlaybackInfo getPlaybackInfo(@IntRange(from = -1) int order);

  // return null if client doesn't support/want to save/restore playback state on config change
  // note that this will ask Container to save a bunch of parcelable, it may not good for performance.

  /**
   * Return a {@link Collection} of orders of {@link ToroPlayer} whose {@link PlaybackInfo}s are
   * saved by this {@link PlayerStateManager}. With this returns a non-null {@link Collection},
   * {@link Container} will be able to save/restore the {@link PlaybackInfo} at various states as
   * well as life cycle events.
   *
   * An implementation of {@link PlayerStateManager} that returns {@code null} from this will
   * disable the ability to save/restore {@link PlaybackInfo}, but not the resuming behaviour.
   */
  @Nullable Collection<Integer> getSavedPlayerOrders();
}
