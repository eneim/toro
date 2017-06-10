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

package im.ene.toro.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import im.ene.toro.PlayerManager;
import im.ene.toro.PlayerSelector;

/**
 * @author eneim | 5/31/17.
 */

public final class ToroHelper {

  private static final String TAG = "ToroLib:Helper";

  @NonNull private final PlayerManager playerManager;
  @Nullable private PlayerSelector playerSelector;  // changeable on demand.
  private Container container;

  @SuppressWarnings("unused") public ToroHelper(@NonNull PlayerManager playerManager) {
    this(playerManager, null);
  }

  @SuppressWarnings("WeakerAccess")
  public ToroHelper(@NonNull PlayerManager playerManager, @Nullable PlayerSelector playerSelector) {
    this.playerManager = playerManager;
    this.playerSelector = playerSelector;
  }

  public void registerContainer(@Nullable Container container) {
    if (this.container == container) return;
    if (this.container != null) {
      this.container.setPlayerManager(null);
      this.container.setPlayerSelector(null);
    }

    this.container = container;
    if (this.container != null) {
      if (this.container.getPlayerManager() != null) {
        throw new IllegalStateException(
            "This Container has already been registered to another Helper.");
      }
      this.container.setPlayerSelector(this.playerSelector);
      this.container.setPlayerManager(this.playerManager);
    }
  }

  public void setPlayerSelector(@Nullable PlayerSelector playerSelector) {
    if (this.playerSelector == playerSelector) return;
    this.playerSelector = playerSelector;
    if (this.container != null) {
      this.container.setPlayerSelector(this.playerSelector);
    }
  }

  @Nullable public PlayerSelector getPlayerSelector() {
    return playerSelector;
  }

  @NonNull public PlayerManager getPlayerManager() {
    return playerManager;
  }
}
