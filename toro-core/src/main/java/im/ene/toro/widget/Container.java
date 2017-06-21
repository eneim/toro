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
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import im.ene.toro.Common;
import im.ene.toro.PlayerSelector;
import im.ene.toro.PlayerStateManager;
import im.ene.toro.R;
import im.ene.toro.ToroLayoutManager;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import ix.Ix;
import ix.IxConsumer;
import ix.IxFunction;
import ix.IxPredicate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author eneim | 5/31/17.
 *
 *         A RecyclerView that contains many {@link ToroPlayer}s.
 */

@SuppressWarnings("unused") //
public class Container extends RecyclerView {

  @SuppressWarnings("unused") private static final String TAG = "ToroLib:Container";

  final PlayerManager playerManager;
  PlayerSelector playerSelector;
  PlayerStateManager playerStateManager;
  Handler animatorFinishHandler;

  private int maxPlayerNumber = 1;  // changeable by attr as well as setter

  public Container(Context context) {
    this(context, null);
  }

  public Container(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public Container(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Container);
    try {
      this.maxPlayerNumber = a.getInt(R.styleable.Container_max_player_number, 1);
    } finally {
      a.recycle();
    }

    // FIXME consider to remove these lines
    // Setup here so we have tool for state save/restore stuff.
    setPlayerSelector(PlayerSelector.DEFAULT);
    this.playerManager = new PlayerManager();
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

    Collection<ToroPlayer> players = playerManager != null ? playerManager.getPlayers() : null;
    if (players != null && !players.isEmpty()) {
      Ix.from(players).doOnNext(new IxConsumer<ToroPlayer>() {
        @Override public void accept(ToroPlayer player) {
          if (player.isPlaying()) playerManager.pause(player);
          playerManager.release(player);
        }
      }).doOnCompleted(new Runnable() {
        @Override public void run() {
          playerManager.clear();
        }
      }).subscribe();
    }
  }

  public void setMaxPlayerNumber(int maxPlayerNumber) {
    this.setMaxPlayerNumber(maxPlayerNumber, false);
  }

  public void setMaxPlayerNumber(int maxPlayerNumber, boolean immediately) {
    this.maxPlayerNumber = maxPlayerNumber;
    if (immediately) {
      onScrollStateChanged(SCROLL_STATE_IDLE);
    }
  }

  @CallSuper @Override public void onChildAttachedToWindow(final View child) {
    super.onChildAttachedToWindow(child);
    if (playerManager == null) return;

    ViewHolder holder = getChildViewHolder(child);
    if (holder == null || !(holder instanceof ToroPlayer)) return;
    final ToroPlayer player = (ToroPlayer) holder;

    if (playerManager.manages(player)) {
      // Only if container is in idle state and player is not playing.
      if (getScrollState() == 0 && !player.isPlaying()) playerManager.play(player);
    } else {
      child.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
        @Override public void onGlobalLayout() {
          child.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          if (Common.allowsToPlay(player.getPlayerView(), Container.this)) {
            if (playerManager.attachPlayer(player)) {
              PlaybackInfo info = playerStateManager == null ? new PlaybackInfo()
                  : playerStateManager.getPlaybackInfo(player.getPlayerOrder());
              playerManager.initialize(player, Container.this, info);
              dispatchUpdateOnAnimationFinished(false);
            }
          }
        }
      });
    }
  }

  @Override public void onChildDetachedFromWindow(View child) {
    super.onChildDetachedFromWindow(child);
    ViewHolder holder = getChildViewHolder(child);
    if (holder == null || !(holder instanceof ToroPlayer)) return;
    final ToroPlayer player = (ToroPlayer) holder;

    boolean playerManaged = playerManager != null && playerManager.manages(player);
    if (player.isPlaying()) {
      if (!playerManaged) {
        throw new IllegalStateException(
            "Player is playing while it is not in managed state: " + player);
      }
      // save playback info
      if (this.playerStateManager != null) {
        this.playerStateManager.savePlaybackInfo(player.getPlayerOrder(),
            player.getCurrentPlaybackInfo());
      }
      playerManager.pause(player);
    }
    if (playerManaged) {
      playerManager.detachPlayer(player);
    }
    // RecyclerView#onChildDetachedFromWindow(View) is called after other removal finishes, so
    // sometime it happens after all Animation, but we also need to update playback here.
    dispatchUpdateOnAnimationFinished(true);
    // finally release the player
    player.release();
  }

  @CallSuper @Override public void onScrollStateChanged(int state) {
    super.onScrollStateChanged(state);
    if (state != SCROLL_STATE_IDLE) return;
    if (playerManager == null || this.playerSelector == null || getChildCount() == 0) return;

    // When settled down, remove players those are not allowed to player anymore.
    Ix.from(playerManager.getPlayers()).filter(new IxPredicate<ToroPlayer>() {
      @Override public boolean test(ToroPlayer player) {
        return !Common.allowsToPlay(player.getPlayerView(), Container.this);
      }
    }).doOnNext(new IxConsumer<ToroPlayer>() {
      @Override public void accept(ToroPlayer player) {
        if (player.isPlaying()) {
          if (playerStateManager != null) {
            playerStateManager.savePlaybackInfo(player.getPlayerOrder(),
                player.getCurrentPlaybackInfo());
          }
          playerManager.pause(player);
        }
        playerManager.detachPlayer(player);
        player.release();
      }
    }).subscribe();

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
        if (holder != null && holder instanceof ToroPlayer) {
          ToroPlayer player = (ToroPlayer) holder;
          // check candidate's condition
          if (Common.allowsToPlay(player.getPlayerView(), Container.this)) {
            if (!playerManager.manages(player)) {
              playerManager.attachPlayer(player);
            }
            if (!player.isPlaying()) {
              playerManager.initialize(player, Container.this,
                  playerStateManager == null ? new PlaybackInfo()
                      : playerStateManager.getPlaybackInfo(player.getPlayerOrder()) //
              );
            }
          }
        }
      }
    }

    final Collection<ToroPlayer> sources = playerManager.getPlayers();
    if (sources.isEmpty()) return;
    final Ix<ToroPlayer> candidates = Ix.from(sources).filter(new IxPredicate<ToroPlayer>() {
      @Override public boolean test(ToroPlayer player) {
        return player.wantsToPlay();
      }
    });

    Ix.from(sources).except(
        // 1. ask selector to select players those can start playback.
        Ix.from(this.playerSelector.select(this, candidates, maxPlayerNumber))  //
            .doOnNext(new IxConsumer<ToroPlayer>() {
              @Override public void accept(ToroPlayer player) {
                if (!player.isPlaying()) playerManager.play(player);
              }
            })
        // 2. items from source except the items above come here, pause the players those should.
    ).doOnNext(new IxConsumer<ToroPlayer>() {
      @Override public void accept(ToroPlayer player) {
        if (player.isPlaying()) {
          if (playerStateManager != null) {
            playerStateManager.savePlaybackInfo(player.getPlayerOrder(),
                player.getCurrentPlaybackInfo());
          }
          playerManager.pause(player);
        }
      }
    }).subscribe();
  }

  public void setPlayerSelector(@Nullable PlayerSelector playerSelector) {
    if (this.playerSelector == playerSelector) return;
    this.playerSelector = playerSelector;
    if (this.playerManager == null || this.playerSelector == null) return;
    this.onScrollStateChanged(SCROLL_STATE_IDLE);
  }

  @Nullable public PlayerSelector getPlayerSelector() {
    return playerSelector;
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

  void dispatchUpdateOnAnimationFinished(boolean immediate) {
    if (animatorFinishHandler == null) return;
    final long duration = immediate ? 50 : getMaxAnimationDuration();
    if (getItemAnimator() != null) {
      getItemAnimator().isRunning(new ItemAnimator.ItemAnimatorFinishedListener() {
        @Override public void onAnimationsFinished() {
          animatorFinishHandler.removeCallbacksAndMessages(null);
          animatorFinishHandler.sendEmptyMessageDelayed(-1, duration);
        }
      });
    } else {
      animatorFinishHandler.removeCallbacksAndMessages(null);
      animatorFinishHandler.sendEmptyMessageDelayed(-1, duration);
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

  public final void setPlayerStateManager(@Nullable PlayerStateManager playerStateManager) {
    this.playerStateManager = playerStateManager;
  }

  @Nullable public final PlayerStateManager getPlayerStateManager() {
    return playerStateManager;
  }

  // Temporary save current playback infos when the App is stopped but not re-created
  // (User press App Stack for example).
  SparseArray<PlaybackInfo> tmpStates = null;

  // In case user press "App Stack" button, this View's window will have visibility change from 0 -> 4 -> 8.
  // When user is back from that state, the visibility changes from 8 -> 4 -> 0.
  @Override protected void onWindowVisibilityChanged(int visibility) {
    super.onWindowVisibilityChanged(visibility);
    if (playerManager == null || playerStateManager == null) return;

    if (visibility == View.GONE) {
      // if onSaveInstanceState is called before, source will contain no item, just fine.
      Ix.from(playerManager.getPlayers()).filter(new IxPredicate<ToroPlayer>() {
        @Override public boolean test(ToroPlayer player) {
          return player.isPlaying();
        }
      }).foreach(new IxConsumer<ToroPlayer>() {
        @Override public void accept(ToroPlayer player) {
          playerStateManager.savePlaybackInfo(player.getPlayerOrder(),
              player.getCurrentPlaybackInfo());
        }
      });
    } else if (visibility == View.VISIBLE) {
      if (tmpStates != null && tmpStates.size() > 0) {
        for (int i = 0; i < tmpStates.size(); i++) {
          int order = tmpStates.keyAt(i);
          PlaybackInfo playbackInfo = tmpStates.get(order);
          playerStateManager.savePlaybackInfo(order, playbackInfo);
        }
        this.onScrollStateChanged(SCROLL_STATE_IDLE);
      }
      tmpStates = null;
    }
  }

  @Override protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    if (playerManager == null || playerStateManager == null) return superState;
    final Collection<Integer> savedOrders = playerStateManager.getSavedPlayerOrders();
    if (savedOrders == null) return superState;
    // Process saving playback state from here since Client wants this.
    final SparseArray<PlaybackInfo> states = new SparseArray<>();

    Collection<ToroPlayer> source = playerManager.getPlayers();

    Ix.from(savedOrders).except(  // using except operator
        Ix.from(source).filter(new IxPredicate<ToroPlayer>() {
          @Override public boolean test(ToroPlayer player) {
            return player.isPlaying();
          }
        }).map(new IxFunction<ToroPlayer, Integer>() {
          @Override public Integer apply(ToroPlayer player) {
            return player.getPlayerOrder();
          }
        })  //
    ).doOnNext(new IxConsumer<Integer>() {
      @Override public void accept(Integer integer) {
        states.put(integer, playerStateManager.getPlaybackInfo(integer));
      }
    }).subscribe();

    Ix.from(source).doOnNext(new IxConsumer<ToroPlayer>() {
      @Override public void accept(ToroPlayer player) {
        if (player.isPlaying()) {
          PlaybackInfo info = player.getCurrentPlaybackInfo();
          playerStateManager.savePlaybackInfo(player.getPlayerOrder(), info);
          states.put(player.getPlayerOrder(), info);
          playerManager.pause(player);
        }
      }
    }).doOnNext(new IxConsumer<ToroPlayer>() {
      @Override public void accept(ToroPlayer player) {
        playerManager.detachPlayer(player);
        player.release();
      }
    }).subscribe();

    // Client must consider this behavior using PlayerStateManager implement.
    PlayerViewState playerViewState = new PlayerViewState(superState);
    playerViewState.statesCache = states;

    // FIXME kind of dirty workaround.
    tmpStates = states;
    return playerViewState;
  }

  @Override protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof PlayerViewState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    PlayerViewState viewState = (PlayerViewState) state;
    super.onRestoreInstanceState(viewState.getSuperState());
    SparseArray<?> saveStates = viewState.statesCache;
    if (playerStateManager != null && saveStates != null && saveStates.size() > 0) {
      for (int i = 0; i < saveStates.size(); i++) {
        int order = saveStates.keyAt(i);
        PlaybackInfo playbackInfo = (PlaybackInfo) saveStates.get(order);
        playerStateManager.savePlaybackInfo(order, playbackInfo);
      }
    }
  }

  @SuppressWarnings("WeakerAccess") //
  public static class PlayerViewState extends AbsSavedState {

    SparseArray<?> statesCache;

    /**
     * Called by onSaveInstanceState
     */
    PlayerViewState(Parcelable superState) {
      super(superState);
    }

    /**
     * called by CREATOR
     */
    PlayerViewState(Parcel in, ClassLoader loader) {
      super(in, loader);
      statesCache = in.readSparseArray(loader);
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      //noinspection unchecked
      dest.writeSparseArray((SparseArray<Object>) statesCache);
    }

    public static final Creator<PlayerViewState> CREATOR =
        ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<PlayerViewState>() {
          @Override public PlayerViewState createFromParcel(Parcel in, ClassLoader loader) {
            return new PlayerViewState(in, loader);
          }

          @Override public PlayerViewState[] newArray(int size) {
            return new PlayerViewState[size];
          }
        });

    @Override public String toString() {
      // "The shorter the better, the String is." - Oda
      return "Cache{" + "states=" + statesCache + '}';
    }
  }

  private class ToroDataObserver extends AdapterDataObserver {

    ToroDataObserver() {
    }

    @Override public void onChanged() {
      dispatchUpdateOnAnimationFinished(true);
    }

    @Override public void onItemRangeChanged(int positionStart, int itemCount) {
      dispatchUpdateOnAnimationFinished(false);
    }

    @Override public void onItemRangeInserted(int positionStart, int itemCount) {
      dispatchUpdateOnAnimationFinished(false);
    }

    @Override public void onItemRangeRemoved(int positionStart, int itemCount) {
      dispatchUpdateOnAnimationFinished(false);
    }

    @Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
      dispatchUpdateOnAnimationFinished(false);
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

    @NonNull private final Container container;

    AnimatorHelper(@NonNull Container container) {
      this.container = container;
    }

    @Override public boolean handleMessage(Message msg) {
      this.container.onScrollStateChanged(SCROLL_STATE_IDLE);
      return true;
    }
  }
}
