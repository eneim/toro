/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample.feature.facebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.google.android.exoplayer2.C;
import im.ene.toro.PlaybackState;
import im.ene.toro.Toro;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroStrategy;
import im.ene.toro.sample.BaseToroFragment;
import im.ene.toro.sample.R;
import im.ene.toro.sample.feature.facebook.playlist.FacebookPlaylistFragment;
import im.ene.toro.sample.feature.facebook.timeline.TimelineAdapter;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem.VideoItem;
import im.ene.toro.sample.util.DemoUtil;
import java.util.List;

/**
 * @author eneim.
 * @since 4/13/17.
 */

public class FacebookTimelineFragment extends BaseToroFragment
    implements FacebookPlaylistFragment.Callback {

  private static final String TAG = "FbTimeline";

  public static FacebookTimelineFragment newInstance() {
    Bundle args = new Bundle();
    FacebookTimelineFragment fragment = new FacebookTimelineFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
  TimelineAdapter adapter;
  private RecyclerView.LayoutManager layoutManager;
  boolean isActive = false;

  Unbinder unbinder;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.generic_recycler_view, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    adapter = new TimelineAdapter();
    layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    mRecyclerView.setHasFixedSize(false);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setAdapter(adapter);

    final ToroStrategy oldStrategy = Toro.getStrategy();
    final int firstVideoPosition = adapter.firstVideoPosition();

    Toro.setStrategy(new ToroStrategy() {
      boolean isFirstPlayerDone = firstVideoPosition != -1; // Valid first position only

      @Override public String getDescription() {
        return "First video plays first";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        return oldStrategy.findBestPlayer(candidates);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        boolean allowToPlay = (isFirstPlayerDone || player.getPlayOrder() == firstVideoPosition)  //
            && oldStrategy.allowsToPlay(player, parent);

        // A work-around to keep track of first video on top.
        if (player.getPlayOrder() == firstVideoPosition) {
          isFirstPlayerDone = true;
        }
        return allowToPlay;
      }
    });

    adapter.setOnItemClickListener(new TimelineAdapter.ItemClickListener() {
      @Override protected void onOgpItemClick(RecyclerView.ViewHolder viewHolder, View view,
          TimelineItem.OgpItem item) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getItemUrl()));
        startActivity(intent);
      }

      @Override protected void onPhotoClick(RecyclerView.ViewHolder viewHolder, View view,
          TimelineItem.PhotoItem item) {

      }

      @Override
      protected void onVideoClick(RecyclerView.ViewHolder viewHolder, View view, VideoItem item) {
        if (item == null) {
          return;
        }

        long duration = C.LENGTH_UNSET;
        long position = C.POSITION_UNSET;
        int order = viewHolder.getAdapterPosition();
        ToroPlayer player = adapter.getPlayer();
        if (player != null) {
          PlaybackState state =
              adapter.getPlaybackState(DemoUtil.genVideoId(item.getVideoUrl(), order));
          duration = player.getDuration();
          position = player.isPlaying() ? player.getCurrentPosition()
              : state != null ? state.getPosition() : 0; // safe
        }

        FacebookPlaylistFragment playlistFragment =
            FacebookPlaylistFragment.newInstance(item, position, duration, order);
        playlistFragment.setTargetFragment(FacebookTimelineFragment.this, 1000);
        playlistFragment.show(getChildFragmentManager(),
            FacebookPlaylistFragment.class.getSimpleName());
      }
    });

    Toro.register(mRecyclerView);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    Toro.unregister(mRecyclerView);
    if (unbinder != null) {
      unbinder.unbind();
    }
  }

  @Override public void onPlaylistAttached() {
    Toro.unregister(mRecyclerView);
  }

  @Override
  public void onPlaylistDetached(VideoItem baseItem, @NonNull PlaybackState state, int order) {
    adapter.savePlaybackState(DemoUtil.genVideoId(baseItem.getVideoUrl(), order),
        state.getPosition(), state.getDuration());

    if (isActive) {
      Toro.register(mRecyclerView);
    }
  }

  @Override protected void dispatchFragmentActive() {
    isActive = true;
  }

  @Override protected void dispatchFragmentInactive() {
    isActive = false;
  }
}
