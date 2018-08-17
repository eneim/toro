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

package im.ene.toro.sample.nested;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.CacheManager;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.widget.Container;
import java.util.List;

/**
 * This {@link RecyclerView.ViewHolder} will contain a {@link Container}.
 *
 * @author eneim (7/1/17).
 */

@SuppressWarnings("unused") //
class MediaListViewHolder extends BaseViewHolder implements ToroPlayer {

  private static final String TAG = "Toro:Nested";

  static final int LAYOUT_RES = R.layout.view_holder_nested_container;

  @SuppressWarnings("WeakerAccess") @BindView(R.id.container) Container container;
  private final SnapHelper snapHelper = new PagerSnapHelper();
  private int initPosition = -1;

  MediaListViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  // Called by Adapter
  void bind(int position, Object item) {
    MediaList mediaList = (MediaList) item;
    Adapter adapter = new Adapter(mediaList);
    container.setAdapter(adapter);
    if (container.getCacheManager() == null) {
      container.setCacheManager(new StateManager(mediaList));
    }
  }

  void onDetached() {
    snapHelper.attachToRecyclerView(null);
  }

  void onAttached() {
    snapHelper.attachToRecyclerView(container);
  }

  // ToroPlayer implementation
  @NonNull @Override public View getPlayerView() {
    return container;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    SparseArray<PlaybackInfo> actualInfos = container.getLatestPlaybackInfos();
    ExtraPlaybackInfo resultInfo = new ExtraPlaybackInfo(actualInfos);

    List<ToroPlayer> activePlayers = container.filterBy(Container.Filter.PLAYING);
    if (activePlayers.size() >= 1) {
      resultInfo.setResumeWindow(activePlayers.get(0).getPlayerOrder());
    }

    return resultInfo;
  }

  @Override
  public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
    this.initPosition = -1;
    if (playbackInfo instanceof ExtraPlaybackInfo) {
      //noinspection unchecked
      SparseArray<PlaybackInfo> cache = ((ExtraPlaybackInfo) playbackInfo).actualInfo;
      if (cache != null && cache.size() > 0) {
        for (int i = 0; i < cache.size(); i++) {
          int key = cache.keyAt(i);
          PlaybackInfo info = cache.get(key);
          this.container.savePlaybackInfo(key, info);
        }
      }
      this.initPosition = playbackInfo.getResumeWindow();
    }
    this.container.setPlayerSelector(PlayerSelector.NONE);
  }

  @Override public void play() {
    if (initPosition >= 0) this.container.scrollToPosition(initPosition);
    initPosition = -1;
    this.container.setPlayerSelector(PlayerSelector.DEFAULT);
  }

  @Override public void pause() {
    this.container.setPlayerSelector(PlayerSelector.NONE);
  }

  @Override public boolean isPlaying() {
    return this.container.filterBy(Container.Filter.PLAYING).size() > 0;
  }

  @Override public void release() {
    // release here
    List<ToroPlayer> managed = this.container.filterBy(Container.Filter.MANAGING);
    for (ToroPlayer player : managed) {
      if (player.isPlaying()) {
        PlaybackInfo info = player.getCurrentPlaybackInfo();
        this.container.savePlaybackInfo(player.getPlayerOrder(), info);
        player.pause();
      }
      player.release();
    }
    this.container.setPlayerSelector(PlayerSelector.NONE);
  }

  @Override public boolean wantsToPlay() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.85;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }

  static class Adapter extends RecyclerView.Adapter<BaseViewHolder> {

    static final int TYPE_VIDEO = 10;
    static final int TYPE_TEXT = 20;

    private LayoutInflater inflater;
    final MediaList mediaList;

    Adapter(MediaList mediaList) {
      this.mediaList = mediaList;
    }

    @NonNull @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      if (inflater == null || inflater.getContext() != parent.getContext()) {
        inflater = LayoutInflater.from(parent.getContext());
      }

      final View view;
      final BaseViewHolder viewHolder;
      switch (viewType) {
        case TYPE_VIDEO:
          view = inflater.inflate(NestedPlayerViewHolder.LAYOUT_RES, parent, false);
          viewHolder = new NestedPlayerViewHolder(view);
          break;
        default:
          view = inflater.inflate(HorizontalTextViewHolder.LAYOUT_RES, parent, false);
          viewHolder = new HorizontalTextViewHolder(view);
          break;
      }

      return viewHolder;
    }

    @Override public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
      holder.bind(position, mediaList.get(position));
    }

    @Override public int getItemViewType(int position) {
      return position % 2 == 1 ? TYPE_TEXT : TYPE_VIDEO;
    }

    @Override public int getItemCount() {
      return mediaList.size();
    }
  }

  static class StateManager implements CacheManager {

    final MediaList mediaList;

    StateManager(MediaList mediaList) {
      this.mediaList = mediaList;
    }

    @NonNull @Override public Object getKeyForOrder(int order) {
      return this.mediaList.get(order);
    }

    @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
      return key instanceof Content.Media ? this.mediaList.indexOf(key) : null;
    }
  }
}
