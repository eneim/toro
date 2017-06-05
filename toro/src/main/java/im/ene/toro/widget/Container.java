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
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import im.ene.toro.Selector;
import im.ene.toro.ToroLayoutManager;
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

  PlayerManager manager;
  Selector selector;
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
    if (manager == null || selector == null) return;
    ViewHolder holder = getChildViewHolder(child);
    if (!(holder instanceof Player)) return;

    final Player player = (Player) holder;
    if (manager.manages(player)) {
      if (!player.isPlaying()) player.play();
    } else {
      child.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
        @Override public void onGlobalLayout() {
          child.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          if (player.wantsToPlay()) {
            player.prepare();
            if (manager.attachPlayer(player)) {
              dispatchUpdateOnAnimationFinished();
            }
          }
        }
      });
    }
  }

  @Override public void onChildDetachedFromWindow(View child) {
    super.onChildDetachedFromWindow(child);
    Log.e(TAG, "onChildDetachedFromWindow() called with: child = [" + child + "]");
    if (manager == null || selector == null) return;
    ViewHolder holder = getChildViewHolder(child);
    if (!(holder instanceof Player)) return;

    final Player player = (Player) holder;
    if (player.isPlaying()) player.pause();
    if (manager.manages(player)) {
      if (manager.detachPlayer(player)) {
        dispatchUpdateOnAnimationFinished();
      }
    }
  }

  @CallSuper @Override public void onScrollStateChanged(int state) {
    super.onScrollStateChanged(state);
    if (state != SCROLL_STATE_IDLE) return;
    if (manager == null || this.selector == null || getChildCount() == 0) return;

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

    manager.updatePlayback(this, selector);
  }

  ////// Manager and Selector stuff

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
    if (this.manager == null || this.selector == null) return;
    this.onScrollStateChanged(SCROLL_STATE_IDLE);
  }

  @SuppressWarnings("unused") @Nullable public Selector getSelector() {
    return selector;
  }

  public void setSelector(@Nullable Selector selector) {
    if (this.selector == selector) return;
    this.selector = selector;
    if (this.manager == null || this.selector == null) return;
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
    }
    super.swapAdapter(wrapper, removeAndRecycleExistingViews);
  }

  @Override public Adapter getAdapter() {
    Adapter adapter = super.getAdapter();
    return adapter != null && adapter instanceof AdapterWrapper ? //
        ((AdapterWrapper) adapter).origin : adapter;
  }

  private class ToroDataObserver extends AdapterDataObserver {

    ToroDataObserver() {
    }

    @Override public void onChanged() {
      dispatchUpdateOnAnimationFinished();
    }

    @Override public void onItemRangeChanged(int positionStart, int itemCount) {
      dispatchUpdateOnAnimationFinished();
    }

    @Override public void onItemRangeInserted(int positionStart, int itemCount) {
      dispatchUpdateOnAnimationFinished();
    }

    @Override public void onItemRangeRemoved(int positionStart, int itemCount) {
      dispatchUpdateOnAnimationFinished();
    }

    @Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
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
