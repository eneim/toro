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

package im.ene.toro.widget;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import im.ene.toro.Player;
import im.ene.toro.PlayerManager;
import im.ene.toro.Selector;
import im.ene.toro.ToroLayoutManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author eneim | 5/31/17.
 */

public class Container extends RecyclerView {

  private static final String TAG = "ToroLib:Container";

  PlayerManager manager;
  Selector selector;

  public Container(Context context) {
    this(context, null);
  }

  public Container(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public Container(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @CallSuper @Override public void onChildAttachedToWindow(final View child) {
    super.onChildAttachedToWindow(child);
    if (manager == null) return;
    ViewHolder holder = getChildViewHolder(child);
    if (!(holder instanceof Player)) return;
    final Player player = (Player) holder;

    if (manager.manages(player)) {
      player.play();
    } else {
      child.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
        @Override public void onGlobalLayout() {
          child.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          if (player.wantsToPlay()) {
            if (manager.attachPlayer(player)) {
              player.prepare();
              manager.updatePlayback(Container.this, Selector.DEFAULT);
            }
          }
        }
      });
    }
  }

  @Override public void onChildDetachedFromWindow(View child) {
    super.onChildDetachedFromWindow(child);
    if (manager == null) return;
    ViewHolder holder = getChildViewHolder(child);
    if (!(holder instanceof Player)) return;

    final Player player = (Player) holder;
    player.pause();
    if (manager.manages(player)) {
      manager.detachPlayer(player);
    }
    player.release();
  }

  @CallSuper @Override public void onScrollStateChanged(int state) {
    super.onScrollStateChanged(state);
    if (state != SCROLL_STATE_IDLE) return;
    if (manager == null) return;

    final List<Player> currentPlayers = new ArrayList<>(manager.getPlayers());
    int count = currentPlayers.size();
    for (int i = count - 1; i >= 0; i--) {
      Player player = currentPlayers.get(i);
      if (!player.wantsToPlay()) {
        player.pause();
        manager.detachPlayer(player);
      }
    }

    int firstVisiblePosition = NO_POSITION;
    int lastVisiblePosition = NO_POSITION;

    LayoutManager layoutManager = getLayoutManager();
    if (layoutManager instanceof LinearLayoutManager) {
      firstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
      lastVisiblePosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
      int[] firstVisibleItemPositions =
          ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
      Arrays.sort(firstVisibleItemPositions);
      if (firstVisibleItemPositions.length > 0) {
        firstVisiblePosition = firstVisibleItemPositions[0];
      }

      int[] lastVisiblePositions =
          ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
      Arrays.sort(lastVisiblePositions);
      if (lastVisiblePositions.length > 0) {
        lastVisiblePosition = lastVisiblePositions[0];
      }
    } else if (layoutManager instanceof ToroLayoutManager) {
      firstVisiblePosition = ((ToroLayoutManager) layoutManager).getFirstVisibleItemPosition();
      lastVisiblePosition = ((ToroLayoutManager) layoutManager).getLastVisibleItemPosition();
    }

    if (firstVisiblePosition <= lastVisiblePosition /* protect the 'for' loop */ &&  //
        (firstVisiblePosition != NO_POSITION || lastVisiblePosition != NO_POSITION)) {
      for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
        // Detected a view holder for media player
        RecyclerView.ViewHolder holder = findViewHolderForAdapterPosition(i);
        if (holder != null && holder instanceof Player) {
          Player candidate = (Player) holder;
          // check candidate's condition
          if (candidate.wantsToPlay()) {
            if (!manager.manages(candidate)) {
              if (manager.attachPlayer(candidate)) {
                candidate.prepare();
              }
            }
          }
        }
      }
    }

    manager.updatePlayback(this, Selector.DEFAULT);
  }

  //////

  @Nullable public PlayerManager getManager() {
    return manager;
  }

  public void setManager(@Nullable PlayerManager manager) {
    if (this.manager == manager) return;
    if (this.manager != null) {
      for (Player player : this.manager.getPlayers()) {
        player.pause();
      }
      this.manager.getPlayers().clear();
    }

    this.manager = manager;
    if (this.manager != null) {
      this.onScrollStateChanged(SCROLL_STATE_IDLE);
    }
  }

  @Nullable public Selector getSelector() {
    return selector;
  }

  public void setSelector(@Nullable Selector selector) {
    if (this.selector == selector) return;
    this.selector = selector;
    if (this.selector != null) {
      this.onScrollStateChanged(SCROLL_STATE_IDLE);
    }
  }
}
