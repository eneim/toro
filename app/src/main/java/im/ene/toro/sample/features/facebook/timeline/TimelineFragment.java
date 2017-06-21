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

package im.ene.toro.sample.features.facebook.timeline;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.sample.features.facebook.data.FbItem;
import im.ene.toro.sample.features.facebook.data.FbVideo;
import im.ene.toro.sample.features.facebook.playlist.MoreVideosFragment;
import im.ene.toro.widget.Container;

/**
 * @author eneim | 6/18/17.
 */

public class TimelineFragment extends BaseFragment implements MoreVideosFragment.Callback {

  @SuppressWarnings("unused") public static TimelineFragment newInstance() {
    Bundle args = new Bundle();
    TimelineFragment fragment = new TimelineFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @BindView(R.id.recycler_view) Container container;
  TimelineAdapter adapter;
  RecyclerView.LayoutManager layoutManager;
  TimelineAdapter.Callback adapterCallback;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.layout_container_facebook, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    adapter = new TimelineAdapter(System.currentTimeMillis());
    layoutManager = new LinearLayoutManager(getContext());
    container.setAdapter(adapter);
    container.setLayoutManager(layoutManager);
    container.setPlayerStateManager(adapter);
    adapterCallback = new TimelineAdapter.Callback() {
      @Override void onItemClick(@NonNull TimelineViewHolder viewHolder, @NonNull View view,
          @NonNull FbItem item, int position) {
        if (viewHolder instanceof ToroPlayer && item instanceof FbVideo) {
          PlaybackInfo info = ((ToroPlayer) viewHolder).getCurrentPlaybackInfo();
          MoreVideosFragment moreVideos =
              MoreVideosFragment.newInstance(position, (FbVideo) item, info);
          moreVideos.show(getChildFragmentManager(), MoreVideosFragment.TAG);
        }
      }
    };
    adapter.setCallback(adapterCallback);
    selector = container.getPlayerSelector(); // save for later use.
  }

  @Override public void onDestroyView() {
    adapterCallback = null;
    adapter = null;
    layoutManager = null;
    selector = null;
    super.onDestroyView();
  }

  // Implement MoreVideosFragment callback

  PlayerSelector selector;  // backup current selector.

  @Override public void onPlaylistViewCreated() {
    container.setPlayerSelector(PlayerSelector.NONE);
  }

  @Override
  public void onPlaylistViewDestroyed(int basePosition, FbVideo baseItem, PlaybackInfo latestInfo) {
    adapter.savePlaybackInfo(basePosition, latestInfo);
    container.setPlayerSelector(selector);
  }
}
