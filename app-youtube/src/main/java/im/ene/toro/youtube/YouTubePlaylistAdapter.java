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

package im.ene.toro.youtube;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import im.ene.toro.CacheManager;
import im.ene.toro.ToroPlayer;
import im.ene.toro.widget.Container;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.reactivex.Observable.fromIterable;

/**
 * @author eneim (2017/11/23).
 */

public class YouTubePlaylistAdapter extends RecyclerView.Adapter<PlaylistItemViewHolder>
    implements CacheManager {

  private static final String TAG = "Toro:Yt:Adapter";

  private final FragmentManager fragmentManager;
  private final Map<ToroPlayer, YouTubePlayerHelper> helpers = new HashMap<>();

  private VideoListResponse data = new VideoListResponse();

  YouTubePlaylistAdapter(@NonNull FragmentManager fragmentManager) {
    super();
    // setHasStableIds(true);
    this.fragmentManager = fragmentManager;
    FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentLifecycleCallbacks() {
      // Actively release resource base on FragmentManager behaviour.
      @Override public void onFragmentViewDestroyed(FragmentManager fm, Fragment fragment) {
        Log.w(TAG, "View Destroyed: " + fragment);
        getHelpers(fragment).forEach(helper -> {
          helper.releasePlayer();
          helper.ytFragment = null;
        });
      }
    };
    this.fragmentManager.registerFragmentLifecycleCallbacks(lifecycleCallbacks, false);
  }

  public void setData(VideoListResponse data) {
    this.data = data;
    notifyDataSetChanged();
  }

  @Override public PlaylistItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(PlaylistItemViewHolder.LAYOUT_RES, parent, false);
    return new PlaylistItemViewHolder(this, view);
  }

  @Override public void onBindViewHolder(PlaylistItemViewHolder holder, int position) {
    holder.bind(getItem(position));
  }

  /// Manage the YouTubePlayerHelpers

  YouTubePlayerHelper createHelper(Container container, ToroPlayer player, String videoId) {
    YouTubePlayerHelper helper = helpers.get(player);
    if (helper == null) {
      helper = new YouTubePlayerHelper(container, player, this.fragmentManager, videoId);
      helpers.put(player, helper);
    }
    return helper;
  }

  @SuppressWarnings("UnusedReturnValue") YouTubePlayerHelper destroyHelper(ToroPlayer player) {
    return this.helpers.remove(player);
  }

  @SuppressWarnings("WeakerAccess") Observable<YouTubePlayerHelper> getHelpers(Fragment fragment) {
    return fromIterable(helpers.values()).filter(helper -> helper.ytFragment == fragment);
  }

  private Video getItem(int position) {
    return getItems().get(position);
  }

  private List<Video> getItems() {
    List<Video> items = data.getItems();
    if (items == null) items = new ArrayList<>();
    return items;
  }

  @Override public int getItemCount() {
    return getItems().size();
  }

  @Override public long getItemId(int position) {
    return getItem(position).getId().hashCode();
  }

  /// CacheManager implementation

  @Nullable @Override public Object getKeyForOrder(int order) {
    return order < 0 ? null : getItem(order);
  }

  @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
    return key instanceof Video ? getItems().indexOf(key) : null;
  }
}
