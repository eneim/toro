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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.PlaybackState;
import im.ene.toro.Toro;
import im.ene.toro.sample.BaseActivity;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.OrderedVideoObject;
import im.ene.toro.sample.v3.action.Action;
import im.ene.toro.sample.v3.action.ActionAdapter;

/**
 * Created by eneim on 2/9/17.
 */

public class MediaListActivity extends BaseActivity {

  static final int REQUEST_CODE_POSITION = 100;
  PlaybackState playbackState;  // save the latest playback state

  @BindView(R.id.recycler_view) RecyclerView recyclerView;
  MediaListAdapter mediaListAdapter;
  ItemTouchHelper itemTouchHelper;

  @BindView(R.id.list_actions) RecyclerView actions;
  ActionAdapter actionAdapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_experiment);
    ButterKnife.bind(this);

    // Setup actions
    actionAdapter = new ActionAdapter();
    LinearLayoutManager actionLayoutManager =
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    actions.setLayoutManager(actionLayoutManager);
    actions.setAdapter(actionAdapter);
    actionAdapter.setActionClickListener(new ActionAdapter.ActionClickListener() {
      @Override public void onActionClick(View view, Action action) {
        handleAction(view, action);
      }
    });

    mediaListAdapter = new MediaListAdapter();
    if (!mediaListAdapter.hasStableIds()) throw new AssertionError("Adapter must have stable Ids");
    LinearLayoutManager layoutManager = new GridLayoutManager(this, 1);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(mediaListAdapter);

    itemTouchHelper = new ItemTouchHelper(  //
        new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN  //
            | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
          @Override
          public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
              RecyclerView.ViewHolder target) {
            final int fromPos = viewHolder.getAdapterPosition();
            final int toPos = target.getAdapterPosition();
            return mediaListAdapter.moveItem(fromPos, toPos);
          }

          @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // Do nothing
          }

          @Override public boolean isItemViewSwipeEnabled() {
            // Disable swipe
            return false;
          }
        });

    itemTouchHelper.attachToRecyclerView(recyclerView);

    mediaListAdapter.setItemClickHandler(new MediaListAdapter.ItemClickHandler() {
      @Override
      public void openVideoPlayer(View view, OrderedVideoObject source, PlaybackState state) {
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
    mediaListAdapter.setItemClickHandler(null);
    itemTouchHelper.attachToRecyclerView(null);
    itemTouchHelper = null;
    Toro.unregister(recyclerView);

    actionAdapter.setActionClickListener(null);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_CODE_POSITION && this.playbackState != null) {
        long position = data.getLongExtra(MediaPlayerActivity.EXTRA_INIT_POSITION, 0);
        this.playbackState.setPosition(position);
        mediaListAdapter.savePlaybackState(playbackState.getMediaId(), playbackState.getPosition(),
            playbackState.getDuration());
      }
    }
  }

  // Handle actions
  void handleAction(View view, Action action) {
    switch (action) {
      case RESET:
        mediaListAdapter.reset();
        break;
      case ADD_NOTIFY_ALL:
        mediaListAdapter.addItemNotifyAll();
        break;
      case ADD_NOTIFY:
        mediaListAdapter.addItemNotify();
        break;
      case REMOVE_NOTIFY:
        mediaListAdapter.removeItemNotify();
        break;
      case REMOVE_NOTIFY_ALL:
        mediaListAdapter.removeItemNotifyAll();
        break;
      case MOVE_NOTIFY:
        break;
      default:
        break;
    }
  }
}
