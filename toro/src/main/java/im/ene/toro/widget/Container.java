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
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.view.AbsSavedState;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import im.ene.toro.Player;
import im.ene.toro.PlayerManager;
import im.ene.toro.PlayerStateManager;
import im.ene.toro.Selector;
import im.ene.toro.ToroLayoutManager;
import im.ene.toro.media.PlayerState;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author eneim | 5/31/17.
 */

public class Container extends RecyclerView {

  @SuppressWarnings("unused") private static final String TAG = "ToroLib:Container";

  PlayerManager playerManager;
  Selector playerSelector;
  PlayerStateManager playerStateManager;
  Handler animatorFinishHandler;

  public Container(Context context) {
    this(context, null);
  }

  public Container(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public Container(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (animatorFinishHandler == null) {
      animatorFinishHandler = new Handler(new AnimatorHelper(this));
    }
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (animatorFinishHandler != null) {
      animatorFinishHandler.removeCallbacksAndMessages(null);
      animatorFinishHandler = null;
    }
  }

  @CallSuper @Override public void onChildAttachedToWindow(final View child) {
    super.onChildAttachedToWindow(child);
    Log.d(TAG, "onChildAttachedToWindow() called with: child = [" + child + "]");
    ViewHolder holder = getChildViewHolder(child);
    if (!(holder instanceof Player)) return;
    final Player player = (Player) holder;
    if (this.playerStateManager != null) {
      this.playerStateManager.getPlayerState(player.getPlayOrder());
    }

    if (playerManager != null) {
      if (playerManager.manages(player)) {
        if (!player.isPlaying()) player.play();
      } else {
        child.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            child.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            if (player.wantsToPlay()) {
              player.prepare(playerStateManager != null ?  //
                  playerStateManager.getPlayerState(player.getPlayOrder()) : new PlayerState());
              if (playerManager.attachPlayer(player)) {
                dispatchUpdateOnAnimationFinished();
              }
            }
          }
        });
      }
    }
  }

  @Override public void onChildDetachedFromWindow(View child) {
    super.onChildDetachedFromWindow(child);
    Log.e(TAG, "onChildDetachedFromWindow() called with: child = [" + child + "]");
    ViewHolder holder = getChildViewHolder(child);
    if (!(holder instanceof Player)) return;
    final Player player = (Player) holder;
    if (this.playerStateManager != null) {
      this.playerStateManager.savePlayerState(player.getPlayOrder(), player.getCurrentState());
    }

    if (playerManager != null) {
      if (player.isPlaying()) player.pause();
      if (playerManager.manages(player)) {
        playerManager.detachPlayer(player);
      }
    }
    // RecyclerView#onChildDetachedFromWindow(View) is called after other removal finishes, so
    // sometime it happens after all Animation, but we also need to update playback here.
    dispatchUpdateOnAnimationFinished();
    player.release();
  }

  @Override
  protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    Log.w(TAG, "onOverScrolled() called with: scrollX = ["
        + scrollX
        + "], scrollY = ["
        + scrollY
        + "], clampedX = ["
        + clampedX
        + "], clampedY = ["
        + clampedY
        + "]");
  }

  @CallSuper @Override public void onScrollStateChanged(int state) {
    super.onScrollStateChanged(state);
    if (state != SCROLL_STATE_IDLE) return;
    if (playerManager == null || this.playerSelector == null || getChildCount() == 0) return;

    final List<Player> currentPlayers = new ArrayList<>(playerManager.getPlayers());
    int count = currentPlayers.size();
    for (int i = count - 1; i >= 0; i--) {
      Player player = currentPlayers.get(i);
      if (!player.wantsToPlay()) {
        player.pause();
        playerManager.detachPlayer(player);
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
            if (!playerManager.manages(candidate)) {
              if (playerManager.attachPlayer(candidate)) {
                candidate.prepare(playerStateManager != null ? playerStateManager.getPlayerState(
                    candidate.getPlayOrder()) : new PlayerState());
              }
            }
          }
        }
      }
    }

    playerManager.updatePlayback(this, playerSelector);
  }

  ////// Manager and Selector stuff

  @Nullable public PlayerManager getPlayerManager() {
    return playerManager;
  }

  public void setPlayerManager(@Nullable PlayerManager playerManager) {
    if (this.playerManager == playerManager) return;
    if (this.playerManager != null) {
      for (Player player : this.playerManager.getPlayers()) {
        player.pause();
      }
      this.playerManager.getPlayers().clear();
    }

    this.playerManager = playerManager;
    if (this.playerManager == null || this.playerSelector == null) return;
    this.onScrollStateChanged(SCROLL_STATE_IDLE);
  }

  @SuppressWarnings("unused") @Nullable public Selector getSelector() {
    return playerSelector;
  }

  public void setSelector(@Nullable Selector playerSelector) {
    if (this.playerSelector == playerSelector) return;
    this.playerSelector = playerSelector;
    if (this.playerManager == null || this.playerSelector == null) return;
    this.onScrollStateChanged(SCROLL_STATE_IDLE);
  }

  ////// Handle update after data change animation

  private static long max(Long... numbers) {
    List<Long> list = Arrays.asList(numbers);
    return Collections.<Long>max(list);
  }

  long getMaxAnimationDuration() {
    ItemAnimator animator = getItemAnimator();
    if (animator == null) return 50; // a blink ...
    return max(animator.getAddDuration(), animator.getMoveDuration(), animator.getRemoveDuration(),
        animator.getChangeDuration());
  }

  void dispatchUpdateOnAnimationFinished() {
    if (getItemAnimator() != null) {
      getItemAnimator().isRunning(new ItemAnimator.ItemAnimatorFinishedListener() {
        @Override public void onAnimationsFinished() {
          animatorFinishHandler.removeCallbacksAndMessages(null);
          animatorFinishHandler.sendEmptyMessageDelayed(-1, getMaxAnimationDuration());
        }
      });
    } else {
      animatorFinishHandler.removeCallbacksAndMessages(null);
      animatorFinishHandler.sendEmptyMessageDelayed(-1, getMaxAnimationDuration());
    }
  }

  ////// Adapter Data Observer setup

  final ToroDataObserver dataObserver = new ToroDataObserver();

  @Override public void setAdapter(Adapter adapter) {
    Adapter oldAdapter = getAdapter();
    if (oldAdapter != null && oldAdapter instanceof AdapterWrapper) {
      ((AdapterWrapper) oldAdapter).unregister();
    }

    AdapterWrapper wrapper = null;
    if (adapter != null) {
      wrapper = new AdapterWrapper(adapter, dataObserver);
      wrapper.register();
      if (adapter instanceof PlayerStateManager) {
        this.playerStateManager = (PlayerStateManager) adapter;
      }
    }
    super.setAdapter(wrapper);
  }

  @Override public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
    Adapter oldAdapter = getAdapter();
    if (oldAdapter != null && oldAdapter instanceof AdapterWrapper) {
      ((AdapterWrapper) oldAdapter).unregister();
    }

    AdapterWrapper wrapper = null;
    if (adapter != null) {
      wrapper = new AdapterWrapper(adapter, dataObserver);
      wrapper.register();
      if (adapter instanceof PlayerStateManager) {
        this.playerStateManager = (PlayerStateManager) adapter;
      }
    }
    super.swapAdapter(wrapper, removeAndRecycleExistingViews);
  }

  @Override public Adapter getAdapter() {
    Adapter adapter = super.getAdapter();
    return adapter != null && adapter instanceof AdapterWrapper ? //
        ((AdapterWrapper) adapter).origin : adapter;
  }

  @Override protected Parcelable onSaveInstanceState() {
    return super.onSaveInstanceState();
  }

  @Override protected void onRestoreInstanceState(Parcelable state) {
    super.onRestoreInstanceState(state);
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public static class ContainerState extends AbsSavedState {



    protected ContainerState(Parcelable superState) {
      super(superState);
    }
  }

  private class ToroDataObserver extends AdapterDataObserver {

    ToroDataObserver() {
    }

    @Override public void onChanged() {
      // call this to trigger update for all players
      if (playerStateManager != null) playerStateManager.onMediaChange(-1, -1);
      dispatchUpdateOnAnimationFinished();
    }

    @Override public void onItemRangeChanged(int positionStart, int itemCount) {
      if (playerStateManager != null) {
        for (int order = positionStart, total = positionStart + itemCount; order < total; order++) {
          playerStateManager.onMediaChange(order, order);
        }
      }
      dispatchUpdateOnAnimationFinished();
    }

    @Override public void onItemRangeInserted(int positionStart, int itemCount) {
      if (playerStateManager != null) {
        for (int order = positionStart, total = positionStart + itemCount; order < total; order++) {
          playerStateManager.onMediaChange(order, order);
        }
      }
      dispatchUpdateOnAnimationFinished();
    }

    @Override public void onItemRangeRemoved(int positionStart, int itemCount) {
      if (playerStateManager != null) {
        for (int order = positionStart, total = positionStart + itemCount; order < total; order++) {
          playerStateManager.onMediaChange(order, order);
        }
      }
      dispatchUpdateOnAnimationFinished();
    }

    @Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
      if (playerStateManager != null) {
        for (int order = fromPosition, total = fromPosition + itemCount; order < total; order++) {
          playerStateManager.onMediaChange(order, order);
        }
      }
      dispatchUpdateOnAnimationFinished();
    }
  }

  private static class AdapterWrapper extends Adapter {

    final Adapter origin;
    final ToroDataObserver observer;

    AdapterWrapper(@NonNull Adapter origin, @NonNull ToroDataObserver observer) {
      super();
      this.origin = origin;
      this.observer = observer;
    }

    void unregister() {
      this.origin.unregisterAdapterDataObserver(this.observer);
    }

    void register() {
      this.origin.registerAdapterDataObserver(this.observer);
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return origin.onCreateViewHolder(parent, viewType);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      //noinspection unchecked
      origin.onBindViewHolder(holder, position);
    }

    @Override public int getItemCount() {
      return origin.getItemCount();
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position, List payloads) {
      //noinspection unchecked
      origin.onBindViewHolder(holder, position, payloads);
    }

    @Override public int getItemViewType(int position) {
      return origin.getItemViewType(position);
    }

    @Override public void setHasStableIds(boolean hasStableIds) {
      origin.setHasStableIds(hasStableIds);
    }

    @Override public long getItemId(int position) {
      return origin.getItemId(position);
    }

    @Override public void onViewRecycled(ViewHolder holder) {
      //noinspection unchecked
      origin.onViewRecycled(holder);
    }

    @Override public boolean onFailedToRecycleView(ViewHolder holder) {
      //noinspection unchecked
      return origin.onFailedToRecycleView(holder);
    }

    @Override public void onViewAttachedToWindow(ViewHolder holder) {
      //noinspection unchecked
      origin.onViewAttachedToWindow(holder);
    }

    @Override public void onViewDetachedFromWindow(ViewHolder holder) {
      //noinspection unchecked
      origin.onViewDetachedFromWindow(holder);
    }

    @Override public void registerAdapterDataObserver(AdapterDataObserver observer) {
      origin.registerAdapterDataObserver(observer);
    }

    @Override public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
      origin.unregisterAdapterDataObserver(observer);
    }

    @Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
      origin.onAttachedToRecyclerView(recyclerView);
    }

    @Override public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
      origin.onDetachedFromRecyclerView(recyclerView);
    }
  }

  private static class AnimatorHelper implements Handler.Callback {

    @NonNull private final WeakReference<Container> container;

    AnimatorHelper(Container container) {
      this.container = new WeakReference<>(container);
    }

    @Override public boolean handleMessage(Message msg) {
      Log.w(TAG, "handleMessage() called with: msg = [" + msg + "]");
      Container container = this.container.get();
      if (container != null) {
        container.onScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE);
      }

      return true;
    }
  }
}
