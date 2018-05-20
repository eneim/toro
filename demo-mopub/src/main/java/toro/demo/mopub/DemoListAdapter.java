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

package toro.demo.mopub;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import im.ene.toro.CacheManager;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ui.PlayerView;
import im.ene.toro.exoplayer.ui.ToroControlView;
import im.ene.toro.widget.Container;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Adapter introduces 2 practices for Toro:
 *
 * 1. Acts as a {@link CacheManager}. The implementation is trivial.
 * 2. Acts as a {@link PlayerSelector} and minds the 'UI interaction'.
 * The background is: the {@link VideoViewHolder} has a {@link PlayerView} by default, in which
 * a {@link ToroControlView} is available. User can interact to that widget to play/pause/change
 * volume for a playback. If a playback is paused by User, we should not start it automatically.
 *
 * To be able to do this, we keep track of the player position that User has manually paused,
 * and use the ability of {@link PlayerSelector} to disallow it to start automatically, until
 * User manually do it again. Right now it caches only one position, but the implementation for
 * many should be trivial.
 *
 * @author eneim (2018/03/13).
 */

public class DemoListAdapter  //
    extends RecyclerView.Adapter<BaseViewHolder>  //
    implements PlayerSelector, CacheManager {

  private static final int TYPE_TEXT = 10;
  private static final int TYPE_VIDEO = 30;

  DemoListAdapter(PlayerSelector origin) {
    this.origin = ToroUtil.checkNotNull(origin);
  }

  DemoListAdapter() {
    this(PlayerSelector.DEFAULT);
  }

  private LayoutInflater inflater;

  @NonNull @Override
  public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (inflater == null || inflater.getContext() != parent.getContext()) {
      inflater = LayoutInflater.from(parent.getContext());
    }

    return viewType == TYPE_VIDEO ? //
        new UiAwareVideoViewHolder(this, parent, inflater, R.layout.view_holder_video)
        : new TextViewHolder(parent, inflater, R.layout.view_holder_text);
  }

  @Override public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    // Boolean.TRUE --> if holder is a TextViewHolder, then show both Text and Photo.
    // Boolean.FALSE --> if holder is a TextViewHolder, then show only Text.
    holder.bind(position % 5 == 0 ? Boolean.TRUE : Boolean.FALSE);
  }

  @Override public int getItemCount() {
    return Integer.MAX_VALUE;
  }

  @Override public int getItemViewType(int position) {
    return position % 4 == 1 ? TYPE_VIDEO : TYPE_TEXT;
  }

  /// PlayerSelector implementation

  @SuppressWarnings("WeakerAccess") //
  final PlayerSelector origin;
  // Keep a cache of the Playback order that is manually paused by User.
  // So that if User scroll to it again, it will not start play.
  // Value will be updated by the ViewHolder.
  final AtomicInteger lastUserPause = new AtomicInteger(-1);

  @NonNull @Override public Collection<ToroPlayer> select(@NonNull Container container,
      @NonNull List<ToroPlayer> items) {
    Collection<ToroPlayer> originalResult = origin.select(container, items);
    ArrayList<ToroPlayer> result = new ArrayList<>(originalResult);
    if (lastUserPause.get() >= 0) {
      for (Iterator<ToroPlayer> it = result.iterator(); it.hasNext(); ) {
        if (it.next().getPlayerOrder() == lastUserPause.get()) {
          it.remove();
          break;
        }
      }
    }

    return result;
  }

  @NonNull @Override public PlayerSelector reverse() {
    return origin.reverse();
  }

  /// CacheManager implementation

  @Nullable @Override public Object getKeyForOrder(int order) {
    return order;
  }

  @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
    return key instanceof Integer ? (Integer) key : null;
  }
}
