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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import im.ene.toro.media.PlayerState;
import im.ene.toro.widget.Container;
import java.util.Collection;

/**
 * @author eneim | 5/31/17.
 *
 *         Logic: collect all Players those "wantsToPlay()", then internally decide if we allow
 *         each of them to play or not.
 *
 *         All managed players must return {@code true} from {@link Player#wantsToPlay()}
 */

public interface PlayerManager {

  @SuppressWarnings("unused") String TAG = "ToroLib:PlayerManager";

  void updatePlayback(@NonNull Container container, @NonNull PlayerSelector selector);

  // Call before player starts playback
  boolean attachPlayer(@NonNull Player player);

  // Call after player pauses or stops playback
  boolean detachPlayer(@NonNull Player player);

  boolean manages(@NonNull Player player);

  @NonNull Collection<Player> getPlayers();

  // return null if client doesn't support/want to save/restore playback state on config change
  // note that this will ask Container to save a bunch of parcelable, it may not good for performance.
  @Nullable SparseArray<PlayerState> getPlayingPlayerStates();
}
