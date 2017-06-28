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
import android.os.Parcel;
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
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import im.ene.toro.PlayerSelector;
import im.ene.toro.PlayerStateManager;
import im.ene.toro.ToroLayoutManager;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author eneim | 5/31/17.
 *
 *         A special {@link RecyclerView} that is empowered to control the {@link ToroPlayer}s.
 *
 *         A client wish to have the power of this library should replace the normal use of {@link
 *         RecyclerView} with this {@link Container}.
 *
 *         By default, a {@link Container} listen to Children's attach/detach events as well as its
 *         own attach/detach/scroll event, to manage {@link ToroPlayer} using a {@link
 *         PlayerManager}.
 *         *
 *         By default, {@link Container} doesn't support playback position saving/restoring. This
 *         is because {@link Container} has no idea about the uniqueness of media those are being
 *         played. This can be archive by supplying it with a valid {@link PlayerStateManager}. A
 *         {@link PlayerStateManager} will help providing the uniqness of Medias by which it can
 *         correctly save/restore the playback state of a specific media item. Setup this can be
 *         done using {@link Container#setPlayerStateManager(PlayerStateManager)}.
 *
 *         {@link Container} can also use various {@link PlayerSelector} to have its own behaviour.
 *         By default, it uses {@link PlayerSelector#DEFAULT}. Custom {@link PlayerSelector} can be
 *         set via {@link Container#setPlayerSelector(PlayerSelector)}.
 *
 *         Last but not least, {@link Container} plays well with {@link Adapter}'s {@link
 *         AdapterDataObserver}. With this support, {@link Container} will correctly respond to
 *         data
 *         change events, animations and so on to update the playback behavior.
 */

@SuppressWarnings("unused") //
public class Container extends RecyclerView {

  private static final String TAG = "ToroLib:Container";

  final PlayerManager playerManager;  // never null
  PlayerSelector playerSelector;  // null = do nothing
  PlayerStateManager playerStateManager;  // null = no position cache
  Handler animatorFinishHandler;  // null = detached ...

  public Container(Context context) {
    this(context, null);
  }

  public Container(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public Container(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    // Setup here so we have tool for state save/restore stuff.
    setPlayerSelector(PlayerSelector.DEFAULT);
    this.playerManager = new PlayerManager();
  }

  @CallSuper @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (animatorFinishHandler == null) {
      animatorFinishHandler = new Handler(new AnimatorHelper(this));
    }
  }

  @CallSuper @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (animatorFinishHandler != null) {
      animatorFinishHandler.removeCallbacksAndMessages(null);
      animatorFinishHandler = null;
    }

    List<ToroPlayer> players = playerManager != null ? playerManager.getPlayers() : null;
    if (players != null && !players.isEmpty()) {
      for (int i = players.size() - 1; i >= 0; i--) {
        ToroPlayer player = players.get(i);
        if (player.isPlaying()) playerManager.pause(player);
        playerManager.release(player);
      }
      playerManager.clear();
    }
  }

  /**
   * Get current active players (players those are playing), sorted by Player order obtained from
   * {@link ToroPlayer#getPlayerOrder()}.
   *
   * @return list of playing players. Empty list if there is no available player.
   */
  @NonNull public List<ToroPlayer> getActivePlayers() {
    List<ToroPlayer> players = new ArrayList<>();
    for (ToroPlayer player : playerManager.getPlayers()) {
      if (player.isPlaying()) players.add(player);
    }
    Collections.sort(players, Common.ORDER_COMPARATOR);
    return Collections.unmodifiableList(players);
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
              PlaybackInfo info = playerStateManager == null ? null
                  : playerStateManager.getPlaybackInfo(player.getPlayerOrder());
              playerManager.initialize(player, Container.this, info);
              dispatchUpdateOnAnimationFinished(false);
            }
          }
        }
      });
    }
  }

  @CallSuper @Override public void onChildDetachedFromWindow(View child) {
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

    List<ToroPlayer> players = playerManager.getPlayers();
    for (int i = 0; i < players.size(); i++) {
      ToroPlayer player = players.get(i);
      if (Common.allowsToPlay(player.getPlayerView(), Container.this)) continue;
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
                  playerStateManager == null ? null
                      : playerStateManager.getPlaybackInfo(player.getPlayerOrder()) //
              );
            }
          }
        }
      }
    }

    final List<ToroPlayer> source = playerManager.getPlayers();
    int count = source.size();
    if (count < 1) return;

    List<ToroPlayer> candidates = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      ToroPlayer player = source.get(i);
      if (player.wantsToPlay()) candidates.add(player);
    }
    Collections.sort(candidates, Common.ORDER_COMPARATOR);

    Collection<ToroPlayer> toPlay = playerSelector.select(this, candidates);
    for (ToroPlayer player : toPlay) {
      if (!player.isPlaying()) playerManager.play(player);
    }

    source.removeAll(toPlay);

    for (ToroPlayer player : source) {
      if (player.isPlaying()) {
        if (playerStateManager != null) {
          playerStateManager.savePlaybackInfo(player.getPlayerOrder(),
              player.getCurrentPlaybackInfo());
        }
        playerManager.pause(player);
      }
    }
  }

  public final void setPlayerSelector(@Nullable PlayerSelector playerSelector) {
    if (this.playerSelector == playerSelector) return;
    this.playerSelector = playerSelector;
    if (this.playerManager == null || this.playerSelector == null) return;
    this.onScrollStateChanged(SCROLL_STATE_IDLE);
  }

  @Nullable public final PlayerSelector getPlayerSelector() {
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
  @CallSuper @Override protected void onWindowVisibilityChanged(int visibility) {
    super.onWindowVisibilityChanged(visibility);
    if (playerManager == null || playerStateManager == null) return;

    if (visibility == View.GONE) {
      List<ToroPlayer> players = playerManager.getPlayers();
      // if onSaveInstanceState is called before, source will contain no item, just fine.
      for (ToroPlayer player : players) {
        if (player.isPlaying()) {
          playerStateManager.savePlaybackInfo(player.getPlayerOrder(),
              player.getCurrentPlaybackInfo());
        }
      }
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

    List<ToroPlayer> source = playerManager.getPlayers();
    List<Integer> playingOrders = new ArrayList<>();
    for (ToroPlayer player : source) {
      if (player.isPlaying()) playingOrders.add(player.getPlayerOrder());
    }
    savedOrders.removeAll(playingOrders);

    for (Integer order : savedOrders) {
      states.put(order, playerStateManager.getPlaybackInfo(order));
    }

    for (ToroPlayer player : source) {
      if (player.isPlaying()) {
        PlaybackInfo info = player.getCurrentPlaybackInfo();
        playerStateManager.savePlaybackInfo(player.getPlayerOrder(), info);
        states.put(player.getPlayerOrder(), info);
        playerManager.pause(player);
      }
      playerManager.detachPlayer(player);
      player.release();
    }

    // Client must consider this behavior using PlayerStateManager implement.
    PlayerViewState playerViewState = new PlayerViewState(superState);
    playerViewState.statesCache = states;

    // To remind that this method was called
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

  @RestrictTo(RestrictTo.Scope.LIBRARY) //
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

    PlayerViewState(Parcel in) {
      super(in);
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      //noinspection unchecked
      dest.writeSparseArray((SparseArray<Object>) statesCache);
    }

    public static final Creator<PlayerViewState> CREATOR =
        new ClassLoaderCreator<PlayerViewState>() {
          @Override public PlayerViewState createFromParcel(Parcel in, ClassLoader loader) {
            return new PlayerViewState(in, loader);
          }

          @Override public PlayerViewState createFromParcel(Parcel source) {
            return new PlayerViewState(source);
          }

          @Override public PlayerViewState[] newArray(int size) {
            return new PlayerViewState[size];
          }
        };

    @Override public String toString() {
      // "The shorter the better, the String is." - ???
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
