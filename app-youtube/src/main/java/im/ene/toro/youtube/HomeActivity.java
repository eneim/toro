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
import im.ene.toro.widget.Container;
import java.io.IOException;

import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;

public class HomeActivity extends AppCompatActivity {

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

    viewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);

    playerManager = new YouTubePlayerManager(this, getSupportFragmentManager());
    if (savedInstanceState != null) {
      playerManager.onRestoreState(savedInstanceState);
    }

    container = findViewById(R.id.container);
    adapter = new YouTubePlaylistAdapter(playerManager);
    layoutManager =
        new StaggeredGridLayoutManager(getResources().getInteger(R.integer.span_count), VERTICAL);
    container.setLayoutManager(layoutManager);
    container.setAdapter(adapter);
    container.setCacheManager(adapter);

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

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (outState != null && isChangingConfigurations()) playerManager.onSaveState(outState);
  }
}
