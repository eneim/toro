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
import im.ene.toro.PlayerSelector;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.io.IOException;

import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;

public class HomeActivity extends AppCompatActivity implements YouTubePlayerDialog.Callback {

  Container container;
  YouTubePlaylistAdapter adapter;
  YouTubePlayerManager playerManager;
  PlaylistViewModel viewModel;
  RecyclerView.LayoutManager layoutManager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Prepare Container
    playerManager = new YouTubePlayerManager(getSupportFragmentManager());
    container = findViewById(R.id.container);
    adapter = new YouTubePlaylistAdapter(playerManager);
    int spanCount = getResources().getInteger(R.integer.span_count);
    layoutManager = new StaggeredGridLayoutManager(spanCount, VERTICAL);
    container.setLayoutManager(layoutManager);
    container.setAdapter(adapter);
    container.setCacheManager(adapter);

    selector = container.getPlayerSelector();

    // Prepare data
    viewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);
    viewModel.getPlaylist().observe(this, response -> adapter.setData(response));
    if (savedInstanceState == null) {
      try {
        viewModel.refresh();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

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
