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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.Bind;
import butterknife.ButterKnife;
import im.ene.toro.PlaybackState;
import im.ene.toro.Toro;
import im.ene.toro.sample.BaseActivity;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.SimpleVideoObject;

/**
 * Created by eneim on 2/9/17.
 */

public class MediaListActivity extends BaseActivity {

  static final int REQUEST_CODE_POSITION = 100;
  PlaybackState playbackState;  // save the latest playback state

  @Bind(R.id.recycler_view) RecyclerView recyclerView;
  MediaListAdapter adapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.generic_recycler_view);
    ButterKnife.bind(this);

    adapter = new MediaListAdapter();
    GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    adapter.setItemClickHandler(new MediaListAdapter.ItemClickHandler() {
      @Override
      public void openVideoPlayer(View view, SimpleVideoObject source, PlaybackState state) {
        playbackState = state;
        startActivityForResult(
            MediaPlayerActivity.createIntent(MediaListActivity.this, source, state.getPosition()),
            REQUEST_CODE_POSITION);
      }
    });

    Toro.register(recyclerView);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    Toro.unregister(recyclerView);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_CODE_POSITION && this.playbackState != null) {
        long position = data.getLongExtra(MediaPlayerActivity.EXTRA_INIT_POSITION, 0);
        this.playbackState.setPosition(position);
        adapter.saveVideoState(playbackState.getMediaId(), playbackState.getPosition(),
            playbackState.getDuration());
      }
    }
  }
}
