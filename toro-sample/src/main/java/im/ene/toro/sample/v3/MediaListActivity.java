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

package im.ene.toro.sample.v3;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import butterknife.Bind;
import butterknife.ButterKnife;
import im.ene.toro.Toro;
import im.ene.toro.sample.BaseActivity;
import im.ene.toro.sample.R;

/**
 * Created by eneim on 2/9/17.
 */

public class MediaListActivity extends BaseActivity {

  @Bind(R.id.recycler_view) RecyclerView recyclerView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.generic_recycler_view);
    ButterKnife.bind(this);

    MediaListAdapter adapter = new MediaListAdapter();
    GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
    Toro.register(recyclerView);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    Toro.unregister(recyclerView);
  }
}
