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

import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import com.google.android.youtube.player.YouTubePlayer;
import im.ene.toro.CacheManager;
import im.ene.toro.PlayerSelector;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.io.IOException;

import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;
import static im.ene.toro.youtube.common.ScreenHelper.shouldUseBigPlayer;

public class HomeActivity extends AppCompatActivity
    implements YouTubePlayerDialog.Callback, YouTubePlayerHelper.Callback {

  Container container;
  PlaylistViewModel viewModel;
  YouTubePlayerManager playerManager;
  YouTubePlaylistAdapter adapter;
  RecyclerView.LayoutManager layoutManager;

  // Judge current window size to open big player or not.
  WindowManager windowManager;
  // Save manifest orientation
  int manifestOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

  @Override protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.activity_home);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    windowManager = getWindowManager();

    if (state == null) manifestOrientation = getRequestedOrientation();

    // Prepare Container
    playerManager = new YouTubePlayerManager(getSupportFragmentManager(), this);
    container = findViewById(R.id.container);
    adapter = new YouTubePlaylistAdapter(playerManager);
    int spanCount = getResources().getInteger(R.integer.span_count);
    layoutManager = new StaggeredGridLayoutManager(spanCount, VERTICAL);
    container.setLayoutManager(layoutManager);
    container.setAdapter(adapter);
    container.setCacheManager(CacheManager.DEFAULT);

    // Prepare data
    viewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);
    viewModel.getPlaylist().observe(this, response -> adapter.setData(response));

    selector = container.getPlayerSelector();

    initData = state != null ? state.getParcelable(STATE_INIT_DATA) : null;
    YouTubePlayerDialog bigPlayer = (YouTubePlayerDialog) getSupportFragmentManager() //
        .findFragmentByTag(YouTubePlayerDialog.TAG);
    YouTubePlayerDialog.InitData temp = bigPlayer != null ? bigPlayer.getDataFromArgs() : null;

    boolean requestPlayerDialog =
        initData != null && shouldUseBigPlayer(windowManager.getDefaultDisplay());

    if (!requestPlayerDialog) {
      if (bigPlayer != null) bigPlayer.dismissAllowingStateLoss();
    } else {
      //noinspection StatementWithEmptyBody
      if (temp == initData) {
        // The Fullscreen dialog is recovered from saved state and have the same init data.
        // Or they are both null and there is no need for a Big player here.
      } else {
        if (bigPlayer != null) bigPlayer.dismissAllowingStateLoss();
        bigPlayer = YouTubePlayerDialog.newInstance(initData);
        bigPlayer.show(getSupportFragmentManager(), YouTubePlayerDialog.TAG);
      }
    }

    if (state == null) {
      try {
        viewModel.refresh();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override protected void onPostCreate(@Nullable Bundle state) {
    super.onPostCreate(state);
    if (state != null) {
      YouTubePlayerDialog.InitData savedData = state.getParcelable(STATE_SAVED_DATA);
      if (savedData != null) {
        container.savePlaybackInfo(savedData.adapterOrder, savedData.playbackInfo);
      }
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    windowManager = null;
  }

  PlayerSelector selector;

  @Override public void onBigPlayerCreated() {
    container.setPlayerSelector(PlayerSelector.NONE);
    container.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onBigPlayerDestroyed(int videoOrder, String baseItem, PlaybackInfo latestInfo) {
    container.setVisibility(View.VISIBLE);
    if (latestInfo != null) container.savePlaybackInfo(videoOrder, latestInfo);
    // HelperManager need access to FragmentManager, and setPlayerSelector will trigger it.
    // If we doing this in the state that FragmentManager is also executing tasks, it will throw
    // IllegalStateException.
    if (!isDestroyed()) container.setPlayerSelector(selector);
  }

  static final String STATE_INIT_DATA = "toro:yt:init_data";
  static final String STATE_SAVED_DATA = "toro:yt:saved_data";

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (initData != null) outState.putParcelable(STATE_INIT_DATA, initData);
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(YouTubePlayerDialog.TAG);
    if (fragment instanceof YouTubePlayerDialog) {
      YouTubePlayerDialog.InitData data = ((YouTubePlayerDialog) fragment).getLatestData();
      outState.putParcelable(STATE_SAVED_DATA, data);
    }
  }

  // If non-null then save to state to the recreated Activity.
  YouTubePlayerDialog.InitData initData;
  YouTubePlayerHelper activeHelper;
  YouTubePlayer activePlayer;

  @Override
  public void onPlayerCreated(@NonNull YouTubePlayerHelper helper, @NonNull YouTubePlayer player) {
    if (activeHelper != helper) {
      activeHelper = helper;
      activePlayer = player;
    }
  }

  @Override public void onPlayerDestroyed(@NonNull YouTubePlayerHelper helper) {
    if (activeHelper == helper) {
      initData = new YouTubePlayerDialog.InitData(  //
          helper.getPlayer().getPlayerOrder(),  //
          helper.videoId,   //
          helper.getLatestPlaybackInfo(), //
          manifestOrientation  //
      );

      activePlayer = null;
      activeHelper = null;
    }
  }

  // Triggered by User interaction.
  @Override public void onFullscreen(@NonNull YouTubePlayerHelper helper, YouTubePlayer player,
      boolean fullscreen) {
    // A hint that User request Fullscreen playback by clicking the button.
    // We prepare some information here and stuff.
    initData = new YouTubePlayerDialog.InitData(  //
        helper.getPlayer().getPlayerOrder(),  //
        helper.videoId,   //
        helper.getLatestPlaybackInfo(), //
        manifestOrientation //
    );
  }
}
