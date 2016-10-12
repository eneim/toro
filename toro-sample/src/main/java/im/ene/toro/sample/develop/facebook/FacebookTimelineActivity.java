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

package im.ene.toro.sample.develop.facebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;
import butterknife.Bind;
import butterknife.ButterKnife;
import im.ene.toro.Toro;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroStrategy;
import im.ene.toro.sample.BaseActivity;
import im.ene.toro.sample.R;
import im.ene.toro.sample.develop.facebook.timeline.TimelineAdapter;
import im.ene.toro.sample.develop.facebook.timeline.TimelineItem;
import java.util.List;

/**
 * Created by eneim on 10/11/16.
 */

public class FacebookTimelineActivity extends BaseActivity {

  @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
  private TimelineAdapter adapter;
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

    final ToroStrategy oldStrategy = Toro.getStrategy();
    final int firstVideoPosition = adapter.firstVideoPosition();

    Toro.setStrategy(new ToroStrategy() {
      boolean isFirstPlayerDone = firstVideoPosition != -1; // Valid first position only

      @Override public String getDescription() {
        return "First video plays first";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        return oldStrategy.findBestPlayer(candidates);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        boolean allowToPlay = (isFirstPlayerDone || player.getPlayOrder() == firstVideoPosition)  //
            && oldStrategy.allowsToPlay(player, parent);

        // A work-around to keep track of first video on top.
        if (player.getPlayOrder() == firstVideoPosition) {
          isFirstPlayerDone = true;
        }
        return allowToPlay;
      }
    });

    adapter.setOnItemClickListener(new TimelineAdapter.ItemClickListener() {
      @Override protected void onOgpItemClick(View view, TimelineItem.OgpItem item) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getItemUrl()));
        startActivity(intent);
      }

      @Override protected void onPhotoClick(View view, TimelineItem.PhotoItem item) {

      }

      @Override protected void onVideoClick(View view, TimelineItem.VideoItem item) {

      }
    });
  }

  @Override protected void onActive() {
    super.onActive();
    Toro.register(mRecyclerView);
  }

  @Override protected void onInactive() {
    super.onInactive();
    Toro.unregister(mRecyclerView);
  }
}
