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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import com.google.api.services.youtube.model.Video;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import im.ene.toro.youtube.YouTubePlayerDialog.InitData;
import im.ene.toro.youtube.common.ScreenHelper;
import java.io.IOException;
import java.util.List;

import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;

public class HomeActivity extends AppCompatActivity implements YouTubePlayerDialog.Callback {

  Container container;
  YouTubePlaylistAdapter adapter;
  YouTubePlayerManager playerManager;
  PlaylistViewModel viewModel;
  RecyclerView.LayoutManager layoutManager;

  private WindowManager windowManager;
  private int originalOrientation;  // At Activity creation

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    viewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);

    originalOrientation = getRequestedOrientation();
    windowManager = getWindowManager();

    playerManager = new YouTubePlayerManager(this, getSupportFragmentManager());
    if (savedInstanceState != null) {
      playerManager.onRestoreState(savedInstanceState,
          ScreenHelper.shouldUseBigPlayer(windowManager.getDefaultDisplay()));
    }

    container = findViewById(R.id.container);
    adapter = new YouTubePlaylistAdapter(playerManager);
    int spanCount = getResources().getInteger(R.integer.span_count);
    layoutManager = new StaggeredGridLayoutManager(spanCount, VERTICAL);
    container.setLayoutManager(layoutManager);
    container.setAdapter(adapter);
    container.setCacheManager(adapter);

    selector = container.getPlayerSelector();

    try {
      viewModel.getPlaylist().observe(this, response -> adapter.setData(response));
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (savedInstanceState == null) {
      try {
        viewModel.refresh();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    windowManager = null;
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    InitData initData = null;
    List<ToroPlayer> activePlayers = container.filterBy(Container.Filter.PLAYING);
    if (!activePlayers.isEmpty()) {
      ToroPlayer firstPlayer = activePlayers.get(0);  // get the first one only.
      // We will store the Media object, playback state.
      Video item = adapter.getItem(firstPlayer.getPlayerOrder());
      if (item == null) {
        throw new IllegalStateException("Video is null for active Player: " + firstPlayer);
      }

      initData = new InitData(firstPlayer.getPlayerOrder(), item.getId(),
          firstPlayer.getCurrentPlaybackInfo(), originalOrientation);
    }

    super.onSaveInstanceState(outState);
    playerManager.onSaveState(outState, initData, isChangingConfigurations());
  }

  /// YouTubePlayerDialog.Callback
  PlayerSelector selector;

  @Override public void onBigPlayerCreated() {
    container.setPlayerSelector(PlayerSelector.NONE);
    container.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onBigPlayerDestroyed(int videoOrder, String baseItem, PlaybackInfo latestInfo) {
    container.savePlaybackInfo(videoOrder, latestInfo);
    container.setPlayerSelector(selector);
    container.setVisibility(View.VISIBLE);
  }
}
