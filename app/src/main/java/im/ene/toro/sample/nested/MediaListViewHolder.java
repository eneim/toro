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

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.CacheManager;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.widget.Container;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;

/**
 * @author eneim (7/1/17).
 *
 *         This {@link RecyclerView.ViewHolder} will contain a {@link Container}.
 */

@SuppressWarnings("unused") //
class MediaListViewHolder extends BaseViewHolder implements ToroPlayer {

  private static final String TAG = "Toro:Nested";

  static final int LAYOUT_RES = R.layout.view_holder_nested_container;

  @BindView(R.id.container) Container container;
  private final SnapHelper snapHelper = new PagerSnapHelper();
  private int initPosition = 0;

  public MediaListViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  void bind(int position, Object item) {
    MediaList mediaList = (MediaList) item;
    Adapter adapter = new Adapter(mediaList);
    container.setAdapter(adapter);
    if (container.getCacheManager() == null) {
      container.setCacheManager(new StateManager(mediaList));
    }
    snapHelper.attachToRecyclerView(container);
  }

  void detach() {
    snapHelper.attachToRecyclerView(null);
  }

  @NonNull @Override public View getPlayerView() {
    return container;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    Collection<Integer> cached = container.getSavedPlayerOrders();
    if (cached.isEmpty()) {
      return new PlaybackInfo();
    } else {
      SparseArray<PlaybackInfo> infos = new SparseArray<>();
      ExtraPlaybackInfo info = new ExtraPlaybackInfo(infos);

      List<ToroPlayer> activePlayers = container.filterBy(Container.Filter.PLAYING);
      Observable.fromIterable(activePlayers).doOnNext(player -> {
        infos.put(player.getPlayerOrder(), player.getCurrentPlaybackInfo());
        cached.remove(player.getPlayerOrder());
      }).subscribe();

      Observable.fromIterable(cached).doOnNext( //
          integer -> infos.put(integer, container.getPlaybackInfo(integer))  //
      ).doOnComplete(() -> {
        if (activePlayers.size() >= 1) {
          info.setResumeWindow(activePlayers.get(0).getPlayerOrder());
        }
      }).subscribe();
      return info;
    }
  }

  @Override
  public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
    Log.i(TAG, "initialize: " + playbackInfo);
    if (playbackInfo != null && playbackInfo instanceof ExtraPlaybackInfo) {
      //noinspection unchecked
      SparseArray<PlaybackInfo> cache = ((ExtraPlaybackInfo) playbackInfo).actualInfo;
      if (cache != null) {
        for (int i = 0; i < cache.size(); i++) {
          int key = cache.keyAt(i);
          this.container.savePlaybackInfo(key, cache.get(key));
        }
      }
      this.initPosition = playbackInfo.getResumeWindow();
    }
    this.container.setPlayerSelector(PlayerSelector.NONE);
  }

  @Override public void play() {
    this.container.scrollToPosition(initPosition);
    this.container.setPlayerSelector(PlayerSelector.DEFAULT);
  }

  @Override public void pause() {
    this.container.setPlayerSelector(PlayerSelector.NONE);
  }

  @Override public boolean isPlaying() {
    return this.container.filterBy(Container.Filter.PLAYING).size() > 0;
  }

  @Override public void release() {
    this.container.setPlayerSelector(PlayerSelector.NONE);
    // release here
    List<ToroPlayer> managed = this.container.filterBy(Container.Filter.MANAGING);
    for (ToroPlayer player : managed) {
      if (player.isPlaying()) {
        this.container.savePlaybackInfo(player.getPlayerOrder(), player.getCurrentPlaybackInfo());
        player.pause();
      }
      player.release();
    }
  }

  @Override public boolean wantsToPlay() {
    Rect viewRect = new Rect();
    boolean visible = this.container.getGlobalVisibleRect(viewRect, new Point());
    if (!visible) return false;

    Rect drawRect = new Rect();
    container.getDrawingRect(drawRect);

    int drawArea = drawRect.width() * drawRect.height();

    float offset = 0.f;
    if (drawArea > 0) {
      int visibleArea = viewRect.height() * viewRect.width();
      offset = visibleArea / (float) drawArea;
    }

    return offset >= 0.85;
  }

  @Override public void onSettled(Container container) {
    // Do nothing
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }

  static class Adapter extends RecyclerView.Adapter<NestedPlayerViewHolder> {

    final MediaList mediaList;

    Adapter(MediaList mediaList) {
      this.mediaList = mediaList;
    }

    @Override public NestedPlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(NestedPlayerViewHolder.LAYOUT_RES, parent, false);
      return new NestedPlayerViewHolder(view);
    }

    @Override public void onBindViewHolder(NestedPlayerViewHolder holder, int position) {
      holder.bind(mediaList.get(position));
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

  public static class ExtraPlaybackInfo extends PlaybackInfo {

    final SparseArray actualInfo;

    public ExtraPlaybackInfo(SparseArray<PlaybackInfo> actualInfo) {
      this.actualInfo = actualInfo;
    }

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      //noinspection unchecked
      dest.writeSparseArray(this.actualInfo);
    }

    protected ExtraPlaybackInfo(Parcel in) {
      super(in);
      this.actualInfo = in.readSparseArray(PlaybackInfo.class.getClassLoader());
    }

    public static final Creator<ExtraPlaybackInfo> CREATOR =
        new ClassLoaderCreator<ExtraPlaybackInfo>() {
          @Override public ExtraPlaybackInfo createFromParcel(Parcel source) {
            return new ExtraPlaybackInfo(source);
          }

          @Override public ExtraPlaybackInfo createFromParcel(Parcel source, ClassLoader loader) {
            return new ExtraPlaybackInfo(source);
          }

          @Override public ExtraPlaybackInfo[] newArray(int size) {
            return new ExtraPlaybackInfo[size];
          }
        };

    @Override public String toString() {
      return "ExtraPlaybackInfo{" + "actualInfo=" + actualInfo + '}';
    }
  }
}
