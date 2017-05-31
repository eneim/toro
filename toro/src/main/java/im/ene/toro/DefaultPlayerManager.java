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
import java.util.ArrayList;
import java.util.List;

/**
 * @author eneim | 5/31/17.
 */

public class DefaultPlayerManager implements PlayerManager {

  private final List<Player> players = new ArrayList<>();

  @Override public boolean attachPlayer(Player player) {
    return players.add(player);
  }

  @Override public boolean detachPlayer(Player player) {
    return players.remove(player);
  }

  @Override public boolean manages(Player player) {
    return players.contains(player);
  }

  @NonNull @Override public List<Player> getPlayers() {
    return this.players;
  }
}
