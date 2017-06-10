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
import im.ene.toro.media.PlayerState;
import java.util.Collection;

/**
 * @author eneim | 6/6/17.
 */

public interface PlayerStateManager {

  void savePlayerState(@IntRange(from = -1) int order, @NonNull PlayerState playerState);

  @NonNull PlayerState getPlayerState(@IntRange(from = -1) int order);

  // return null if client doesn't support/want to save/restore playback state on config change
  // note that this will ask Container to save a bunch of parcelable, it may not good for performance.
  @Nullable Collection<Integer> getSavedOrders();
}
