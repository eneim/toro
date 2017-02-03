/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.android.exoplayer2.C;
import im.ene.toro.PlaybackState;
import im.ene.toro.Toro;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroStrategy;
import im.ene.toro.sample.BaseActivity;
import im.ene.toro.sample.R;
import im.ene.toro.sample.feature.facebook.playlist.FacebookPlaylistFragment;
import im.ene.toro.sample.feature.facebook.timeline.TimelineAdapter;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem;
import im.ene.toro.sample.util.Util;
import java.util.List;

/**
 * Created by eneim on 10/11/16.
 */

public class FacebookTimelineActivity extends BaseActivity
    implements FacebookPlaylistFragment.Callback {

  @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
  TimelineAdapter adapter;
  private RecyclerView.LayoutManager layoutManager;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.generic_recycler_view);
    ButterKnife.bind(this);

    adapter = new TimelineAdapter();
    layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    mRecyclerView.setHasFixedSize(false);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setAdapter(adapter);

    final int firstVideoPosition = adapter.firstVideoPosition();

    ToroStrategy strategy = new ToroStrategy() {
      ToroStrategy delegate = Toro.Strategies.FIRST_PLAYABLE_TOP_DOWN;
      boolean isFirstPlayerDone = firstVideoPosition != -1; // Valid first position only

      @Override public String getDescription() {
        return "First video plays first";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        return delegate.findBestPlayer(candidates);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        boolean allowToPlay = (isFirstPlayerDone || player.getPlayOrder() == firstVideoPosition)  //
            && delegate.allowsToPlay(player, parent);

        // A work-around to keep track of first video on top.
        if (player.getPlayOrder() == firstVideoPosition) {
          isFirstPlayerDone = true;
        }
        return allowToPlay;
      }
    };

    adapter.setOnItemClickListener(new TimelineAdapter.ItemClickListener() {
      @Override protected void onOgpItemClick(RecyclerView.ViewHolder viewHolder, View view,
          TimelineItem.OgpItem item) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getItemUrl()));
        startActivity(intent);
      }

      @Override protected void onPhotoClick(RecyclerView.ViewHolder viewHolder, View view,
          TimelineItem.PhotoItem item) {

      }

      @Override protected void onVideoClick(RecyclerView.ViewHolder viewHolder, View view,
          TimelineItem.VideoItem item) {
        long duration = C.LENGTH_UNSET;
        long position = C.POSITION_UNSET;
        int order = viewHolder.getAdapterPosition();
        ToroPlayer player = adapter.getPlayer();
        if (player != null) {
          PlaybackState state = adapter.getSavedState(Util.genVideoId(item.getVideoUrl(), order));
          duration = player.getDuration();
          position = player.isPlaying() ? player.getCurrentPosition()
              : state != null ? state.getPosition() : 0; // safe
        }

        if (item != null) {
          FacebookPlaylistFragment playlistFragment =
              FacebookPlaylistFragment.newInstance(item, position, duration, order);
          playlistFragment.show(getSupportFragmentManager(),
              FacebookPlaylistFragment.class.getSimpleName());
        }
      }
    });

    Toro.with(this).strategy(strategy).register(mRecyclerView);
  }

  boolean isActive = false;

  @Override protected void onActive() {
    super.onActive();
    isActive = true;
  }

  @Override protected void onInactive() {
    super.onInactive();
    isActive = false;
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    Toro.unregister(mRecyclerView);
  }

  private static final String TAG = "Toro:FB:TL";

  @Override public void onPlaylistAttached() {
    Log.i(TAG, "onPlaylistAttached() called");
    Toro.unregister(mRecyclerView);
  }

  @Override
  public void onPlaylistDetached(TimelineItem.VideoItem baseItem, Long position, int order) {
    Log.i(TAG,
        "onPlaylistDetached() called with: position = [" + position + "], order = [" + order + "]");
    if (adapter.getPlayer() != null) {
      adapter.saveVideoState(Util.genVideoId(baseItem.getVideoUrl(), order), position,
          adapter.getPlayer().getDuration());
    }

    if (isActive) {
      Toro.register(mRecyclerView);
    }
  }
}
