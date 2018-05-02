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

package im.ene.toro.sample.basic;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.CacheManager;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseActivity;
import im.ene.toro.widget.Container;

/**
 * @author eneim (7/2/17).
 */

public class BasicListActivity extends BaseActivity {

  @BindView(R.id.player_container) Container container;
  LinearLayoutManager layoutManager;
  BasicListAdapter adapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fragment_basic);
    ButterKnife.bind(this);

    layoutManager = new LinearLayoutManager(this);
    container.setLayoutManager(layoutManager);
    adapter = new BasicListAdapter();
    container.setAdapter(adapter);
  }

  @Override protected void onDestroy() {
    layoutManager = null;
    adapter = null;
    super.onDestroy();
  }
}
