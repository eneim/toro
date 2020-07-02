/*
 * Copyright (c) 2017 Thiago Ricieri, thiago.ricieri@gmail.com
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
import java.util.Collection;
import java.util.List;

import im.ene.toro.widget.Container;

/**
 * @author thiagoricieri | 12/11/17.
 *
 *         SinglePlayerSelector is a convenient class to provide a single {@link PlayerSelector}
 *         to {@link Container}, ideal for Recycler Views that are setup to not play
 *         video automatically.
 *
 *         1. Setup the container view to use NONE by default.
 *         <pre>
 *         {@code
 *         myContainer.setPlayerSelector(PlayerSelector.NONE)
 *         }
 *         </pre>
 *
 *         2. When an event occurs, provide a SinglePlayerSelector that'll use the desired
 *         {@link ToroPlayer} instance. This is ideal to start a player on click event.
 *         <pre>
 *         {@code
 *         myContainer.setPlayerSelector(new SinglePlayerSelector(myPlayer))
 *         }
 *         </pre>
 *
 */

public class SinglePlayerSelector implements PlayerSelector {

  ToroPlayer player;

  public SinglePlayerSelector(ToroPlayer player) {
    this.player = player;
  }

  @NonNull
  @Override public Collection<ToroPlayer> select(@NonNull Container container, @NonNull List<ToroPlayer> items) {
    ArrayList<ToroPlayer> toSelect = new ArrayList<>();
    toSelect.add(player);
    return toSelect;
  }

  @NonNull @Override public PlayerSelector reverse() {
    return this;
  }
}