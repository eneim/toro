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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import im.ene.toro.widget.Container;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

  Container container;
  YoutubePlaylistAdapter adapter;

  PlaylistViewModel viewModel;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    viewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);

    container = findViewById(R.id.container);
    adapter = new YoutubePlaylistAdapter(getSupportFragmentManager());
    container.setLayoutManager(new LinearLayoutManager(this));
    container.setAdapter(adapter);
    container.setCacheManager(adapter);

    try {
      viewModel.getPlaylist().observe(this, response -> adapter.setData(response));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
