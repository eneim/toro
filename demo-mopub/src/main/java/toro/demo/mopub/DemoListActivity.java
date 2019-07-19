/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package toro.demo.mopub;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import im.ene.toro.widget.Container;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;

public class DemoListActivity extends AppCompatActivity {

  Container container;
  DemoListAdapter adapter;
  RecyclerView.LayoutManager layoutManager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo_list);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    container = findViewById(R.id.container);
    layoutManager = new LinearLayoutManager(this);
    // See DemoListAdapter for detail usage.
    adapter = new DemoListAdapter(PlayerSelector.DEFAULT);
    container.setPlayerSelector(adapter);
    container.setCacheManager(adapter);

    container.setLayoutManager(layoutManager);
    container.setAdapter(adapter);
    container.setPlayerDispatcher(__ -> 500); // The playback will be delayed 500ms.
    container.setPlayerInitializer(order -> {
      VolumeInfo volumeInfo = new VolumeInfo(true, 0.75f);
      return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
    });

    // Only when you use Container inside a CoordinatorLayout and depends on Behavior.
    ToroUtil.wrapParamBehavior(container, () -> container.onScrollStateChanged(SCROLL_STATE_IDLE));
  }
}
