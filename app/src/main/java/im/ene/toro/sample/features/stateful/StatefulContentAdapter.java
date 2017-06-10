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

package im.ene.toro.sample.features.stateful;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import im.ene.toro.PlayerStateManager;
import im.ene.toro.media.PlayerState;
import im.ene.toro.sample.common.BaseViewHolder;
import im.ene.toro.sample.common.ContentAdapter;
import im.ene.toro.sample.common.DemoUtil;
import im.ene.toro.sample.common.MediaViewHolder;
import im.ene.toro.sample.data.DataSource;
import im.ene.toro.sample.data.Entity;
import ix.Ix;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author eneim | 6/10/17.
 */

class StatefulContentAdapter extends ContentAdapter implements PlayerStateManager {

  private static final String TAG = "Toro:Adapter";

  private final Map<Entity, PlayerState> stateCache =
      new TreeMap<>((o1, o2) -> DemoUtil.compare(o1.getIndex(), o2.getIndex()));

  @Override public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    BaseViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
    if (viewHolder instanceof MediaViewHolder) {
      ((MediaViewHolder) viewHolder).content.setOnClickListener(v -> {
        int pos = viewHolder.getAdapterPosition();
        if (pos >= 0) {
          entities.remove(pos);
          DataSource.getInstance().getEntities().remove(pos);
          notifyItemRemoved(pos);
        }
      });
    }
    return viewHolder;
  }

  @Override public void savePlayerState(int order, @NonNull PlayerState playerState) {
    if (order >= 0) stateCache.put(getItem(order), playerState);
  }

  @NonNull @Override public PlayerState getPlayerState(int order) {
    Entity entity = order >= 0 ? super.getItem(order) : null;
    PlayerState state = new PlayerState();
    if (entity != null) {
      state = stateCache.get(entity);
      if (state == null) {
        state = new PlayerState();
        stateCache.put(entity, state);
      }
    }
    return state;
  }

  // TODO return null if client doesn't want to save playback states on config change.
  @Nullable @Override public Collection<Integer> getSavedPlayerOrders() {
    return Ix.from(stateCache.keySet()).map(entities::indexOf).toList();
  }
}
