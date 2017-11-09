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

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.AbsSavedState;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import im.ene.toro.CacheManager;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static android.content.Context.POWER_SERVICE;
import static im.ene.toro.widget.Common.max;

/**
 * @author eneim | 5/31/17.
 *
 *         A custom {@link RecyclerView} that is capable of managing and controling the
 *         {@link ToroPlayer}s' playback behaviour.
 *
 *         A client wish to have the auto playback behaviour should replace the normal use of
 *         {@link RecyclerView} with {@link Container}.
 *
 *         By default, {@link Container} doesn't support playback position saving/restoring. This
 *         is because {@link Container} has no idea about the uniqueness of media content those are
 *         being played. This can be archived by supplying {@link Container} with a valid {@link
 *         CacheManager}. A {@link CacheManager} will help providing the uniqness of Medias by
 *         which it can correctly save/restore the playback state of a specific media item. Setup
 *         this can be done using {@link Container#setCacheManager(CacheManager)}.
 *
 *         {@link Container} uses {@link PlayerSelector} to control the {@link ToroPlayer}. A
 *         {@link PlayerSelector} will be asked to select which {@link ToroPlayer} to start
 *         playback, and those are not selected will be paused. By default, it uses {@link
 *         PlayerSelector#DEFAULT}. Custom {@link PlayerSelector} can be set via {@link
 *         Container#setPlayerSelector(PlayerSelector)}.
 */

@SuppressWarnings({ "unused", "ConstantConditions" }) //
public class Container extends RecyclerView {

  private static final String TAG = "ToroLib:Container";

  /* package */ final PlayerManager playerManager = new PlayerManager();  // never null
  /* package */ PlayerSelector playerSelector = PlayerSelector.DEFAULT;   // null = do nothing
  /* package */ Handler animatorFinishHandler;  // null = Container is detached ...

  public Container(Context context) {
    this(context, null);
  }

  public Container(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public Container(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @CallSuper @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (getAdapter() != null) dataObserver.registerAdapter(getAdapter());
    if (animatorFinishHandler == null) {
      animatorFinishHandler = new Handler(new AnimatorHelper(this));
    }

    PowerManager powerManager = (PowerManager) getContext().getSystemService(POWER_SERVICE);
    if (powerManager.isScreenOn() /* new API calls isInteractive() internally */) {
      this.screenState = View.SCREEN_STATE_ON;
    } else {
      this.screenState = View.SCREEN_STATE_OFF;
    }
  }

  @CallSuper @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (animatorFinishHandler != null) {
      animatorFinishHandler.removeCallbacksAndMessages(null);
      animatorFinishHandler = null;
    }

    List<ToroPlayer> players = playerManager.getPlayers();
    if (!players.isEmpty()) {
      for (int size = players.size(), i = size - 1; i >= 0; i--) {
        ToroPlayer player = players.get(i);
        if (player.isPlaying()) playerManager.pause(player);
        playerManager.release(player);
      }
      playerManager.clear();
    }

    dataObserver.registerAdapter(null);
  }

  /**
   * Filter current managed {@link ToroPlayer}s using {@link Filter}. Result is sorted by Player
   * order obtained from {@link ToroPlayer#getPlayerOrder()}.
   *
   * @param filter the {@link Filter} to a {@link ToroPlayer}.
   * @return list of players accepted by {@link Filter}. Empty list if there is no available player.
   */
  @NonNull public List<ToroPlayer> filterBy(Filter filter) {
    List<ToroPlayer> result = new ArrayList<>();
    for (ToroPlayer player : playerManager.getPlayers()) {
      if (filter.accept(player)) result.add(player);
    }
    Collections.sort(result, Common.ORDER_COMPARATOR);
    return Collections.unmodifiableList(result);
  }

  @CallSuper @Override public void onChildAttachedToWindow(final View child) {
    super.onChildAttachedToWindow(child);
    final ViewHolder holder = getChildViewHolder(child);
    if (holder == null || !(holder instanceof ToroPlayer)) return;
    final ToroPlayer player = (ToroPlayer) holder;
    final View playerView = player.getPlayerView();
    if (playerView == null) {
      throw new NullPointerException("Expected non-null playerView, found null for: " + player);
    }

    if (playerManager.manages(player)) {
      // Only if container is in idle state and player is not playing.
      if (getScrollState() == SCROLL_STATE_IDLE && !player.isPlaying()) playerManager.play(player);
    } else {
      child.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
        @Override public void onGlobalLayout() {
          child.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          if (Common.allowsToPlay(playerView, Container.this)) {
            if (playerManager.attachPlayer(player)) {
              PlaybackInfo info = Container.this.getPlaybackInfo(player.getPlayerOrder());
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

    boolean playerManaged = playerManager.manages(player);
    if (player.isPlaying()) {
      if (!playerManaged) {
        throw new IllegalStateException(
            "Player is playing while it is not in managed state: " + player);
      }
      // save playback info
      this.savePlaybackInfo(player.getPlayerOrder(), player.getCurrentPlaybackInfo());
      playerManager.pause(player);
    }
    if (playerManaged) {
      playerManager.detachPlayer(player);
    }
    // RecyclerView#onChildDetachedFromWindow(View) is called after other removal finishes, so
    // sometime it happens after all Animation, but we also need to update playback here.
    dispatchUpdateOnAnimationFinished(true);
    // finally release the player
    // this player may not be managed so it should release by itself.
    player.release();
  }

  @CallSuper @Override public void onScrollStateChanged(int state) {
    super.onScrollStateChanged(state);
    if (state != SCROLL_STATE_IDLE) return;
    if (getChildCount() == 0) return;

    List<ToroPlayer> players = playerManager.getPlayers();
    // 1. Find players those are not qualified to play anymore.
    for (int i = 0, size = players.size(); i < size; i++) {
      ToroPlayer player = players.get(i);
      if (Common.allowsToPlay(player.getPlayerView(), Container.this)) continue;
      if (player.isPlaying()) {
        this.savePlaybackInfo(player.getPlayerOrder(), player.getCurrentPlaybackInfo());
        playerManager.pause(player);
      }
      playerManager.release(player);
      playerManager.detachPlayer(player);
    }

    // 2. Refresh the good players list.
    LayoutManager layout = super.getLayoutManager();
    int childCount = layout.getChildCount();
    if (childCount > 0) {
      for (int i = 0; i < childCount; i++) {
        View child = layout.getChildAt(i);
        ViewHolder holder = super.findContainingViewHolder(child);
        if (holder != null && holder instanceof ToroPlayer) {
          ToroPlayer player = (ToroPlayer) holder;
          // Check candidate's condition
          if (Common.allowsToPlay(player.getPlayerView(), Container.this)) {
            if (!playerManager.manages(player)) {
              playerManager.attachPlayer(player);
            }
            // Don't check the attach result, because the player may be managed already.
            if (!player.isPlaying()) {
              playerManager.initialize(player, this, this.getPlaybackInfo(player.getPlayerOrder()));
            }
          }
        }
      }
    }

    final List<ToroPlayer> source = playerManager.getPlayers();
    int count = source.size();
    if (count < 1) return;  // No available player, return.

    List<ToroPlayer> candidates = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      ToroPlayer player = source.get(i);
      if (player.wantsToPlay()) candidates.add(player);
    }
    Collections.sort(candidates, Common.ORDER_COMPARATOR);

    Collection<ToroPlayer> toPlay = playerSelector != null ? playerSelector.select(this, candidates)
        : Collections.<ToroPlayer>emptyList();
    for (ToroPlayer player : toPlay) {
      if (!player.isPlaying()) playerManager.play(player);
    }

    source.removeAll(toPlay);

    for (ToroPlayer player : source) {
      if (player.isPlaying()) {
        this.savePlaybackInfo(player.getPlayerOrder(), player.getCurrentPlaybackInfo());
        playerManager.pause(player);
      }
    }

    for (ToroPlayer player : playerManager.getPlayers()) {
      player.onSettled(this);
    }
  }

  /**
   * Setup a {@link PlayerSelector}. Set a {@code null} {@link PlayerSelector} will stop all
   * playback.
   *
   * @param playerSelector new {@link PlayerSelector} for this {@link Container}.
   */
  public final void setPlayerSelector(@Nullable PlayerSelector playerSelector) {
    if (this.playerSelector == playerSelector) return;
    this.playerSelector = playerSelector;
    dispatchUpdateOnAnimationFinished(true);
  }

  /**
   * Get current {@link PlayerSelector}. Can be {@code null}.
   *
   * @return current {@link #playerSelector}
   */
  @Nullable public final PlayerSelector getPlayerSelector() {
    return playerSelector;
  }

  ////// Handle update after data change animation

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

  /**
   * See {@link ToroDataObserver}
   */
  private final ToroDataObserver dataObserver = new ToroDataObserver();

  /**
   * {@inheritDoc}
   *
   * See {@link Adapter#registerAdapterDataObserver(AdapterDataObserver)}
   * See {@link Adapter#unregisterAdapterDataObserver(AdapterDataObserver)}
   */
  @CallSuper @Override public void setAdapter(Adapter adapter) {
    super.setAdapter(adapter);
    dataObserver.registerAdapter(adapter);
  }

  /**
   * {@inheritDoc}
   *
   * See {@link Container#setAdapter(Adapter)}
   */
  @CallSuper @Override public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
    super.swapAdapter(adapter, removeAndRecycleExistingViews);
    dataObserver.registerAdapter(adapter);
  }

  //// PlaybackInfo Cache implementation
  private CacheManager cacheManager = null; // null by default
  private Map<Object, PlaybackInfo> infoCache = new ArrayMap<>();

  /**
   * Save {@link PlaybackInfo} for the current {@link ToroPlayer} of a specific order.
   *
   * @param order order of the {@link ToroPlayer}.
   * @param playbackInfo current {@link PlaybackInfo} of the {@link ToroPlayer}.
   */
  public void savePlaybackInfo(int order, @NonNull PlaybackInfo playbackInfo) {
    if (cacheManager == null || order < 0) return;
    Object key = cacheManager.getKeyForOrder(order);
    if (key != null) infoCache.put(key, playbackInfo);
  }

  /**
   * Get the cached {@link PlaybackInfo} at a specific order.
   *
   * @param order order of the {@link ToroPlayer} to get the cached {@link PlaybackInfo}.
   * @return cached {@link PlaybackInfo} if available, a new one if there is no cached one.
   */
  @NonNull public PlaybackInfo getPlaybackInfo(int order) {
    if (cacheManager == null || order < 0) return new PlaybackInfo();

    Object key = cacheManager.getKeyForOrder(order);
    if (key == null) return new PlaybackInfo();
    PlaybackInfo info = infoCache.get(key);
    if (info == null) {
      info = new PlaybackInfo();
      infoCache.put(key, info);
    }

    return info;
  }

  /**
   * Get current list of {@link ToroPlayer}s' orders whose {@link PlaybackInfo} are cached.
   * Returning an empty list will disable the save/restore of player's position.
   *
   * @return list of {@link ToroPlayer}s' orders.
   */
  @NonNull public List<Integer> getSavedPlayerOrders() {
    List<Integer> orders = new ArrayList<>();
    if (cacheManager == null) return orders;
    for (Object key : infoCache.keySet()) {
      Integer order = cacheManager.getOrderForKey(key);
      if (order != null) orders.add(order);
    }
    return orders;
  }

  /**
   * Set a {@link CacheManager} to this {@link Container}. A {@link CacheManager} will
   * allow this {@link Container} to save/restore {@link PlaybackInfo} on various states or life
   * cycle events. Setting a {@code null} {@link CacheManager} will remove that ability.
   * {@link Container} doesn't have a non-null {@link CacheManager} by default.
   *
   * Setting this while there is a {@code non-null} {@link CacheManager} available will clear
   * current {@link PlaybackInfo} cache.
   *
   * @param cacheManager The {@link CacheManager} to set to the {@link Container}.
   */
  public final void setCacheManager(@Nullable CacheManager cacheManager) {
    if (this.cacheManager == cacheManager) return;
    this.infoCache.clear();
    this.cacheManager = cacheManager;
  }

  /**
   * Get current {@link CacheManager} of the {@link Container}.
   *
   * @return current {@link CacheManager} of the {@link Container}. Can be {@code null}.
   */
  @Nullable public final CacheManager getCacheManager() {
    return cacheManager;
  }

  /**
   * Temporary save current playback infos when the App is stopped but not re-created. (For example:
   * User press App Stack). If not {@code null} then user is back from a living-but-stopped state.
   */
  SparseArray<PlaybackInfo> tmpStates = null;

  /**
   * {@inheritDoc}
   *
   * In case user press "App Stack" button, this View's window will have visibility change from
   * {@link #VISIBLE} to {@link #INVISIBLE} to {@link #GONE}. When user is back from that state,
   * the visibility changes from {@link #GONE} to {@link #INVISIBLE} to {@link #VISIBLE}. A proper
   * playback needs to handle this case too.
   */
  @CallSuper @Override protected void onWindowVisibilityChanged(int visibility) {
    super.onWindowVisibilityChanged(visibility);
    if (visibility == View.GONE) {
      List<ToroPlayer> players = playerManager.getPlayers();
      // if onSaveInstanceState is called before, source will contain no item, just fine.
      for (ToroPlayer player : players) {
        if (player.isPlaying()) {
          this.savePlaybackInfo(player.getPlayerOrder(), player.getCurrentPlaybackInfo());
          playerManager.pause(player);
        }
      }
    } else if (visibility == View.VISIBLE) {
      if (tmpStates != null && tmpStates.size() > 0) {
        for (int i = 0; i < tmpStates.size(); i++) {
          int order = tmpStates.keyAt(i);
          PlaybackInfo playbackInfo = tmpStates.get(order);
          this.savePlaybackInfo(order, playbackInfo);
        }
      }
      tmpStates = null;
      dispatchUpdateOnAnimationFinished(true);
    }

    dispatchWindowVisibilityMayChange();
  }

  private int screenState;

  @Override public void onScreenStateChanged(int screenState) {
    super.onScreenStateChanged(screenState);
    this.screenState = screenState;
    dispatchWindowVisibilityMayChange();
  }

  @Override public void onWindowFocusChanged(boolean hasWindowFocus) {
    super.onWindowFocusChanged(hasWindowFocus);
    dispatchWindowVisibilityMayChange();
  }

  /**
   * This method supports the case that by some reasons, Container should changes it behaviour
   * without any Activity recreation (so {@link #onSaveInstanceState()} and
   * {@link #onRestoreInstanceState(Parcelable)} could not help).
   *
   * This method is called when:
   * - Screen state changed.
   * or - Window focus changed.
   * or - Window visibility changed.
   *
   * For each of that change, Screen may be turned off or Window's focused state may change, this
   * is to decide if Container should keep current playback state or change it.
   *
   * <strong>Discussion</strong>: In fact, we expect that: Container will be playing if the
   * following conditions are all satisfied:
   * - Current window is visible. (but not necessarily focused).
   * - Container is visible in Window (partly is fine, we care about the Media player).
   * - Container is focused in Window. (so we don't screw up other components' focuses).
   *
   * In lower API (eg: 16), {@link #getWindowVisibility()} always returns {@link #VISIBLE}, which
   * cannot tell much. We need to investigate this flag in various APIs in various Scenarios.
   */
  private void dispatchWindowVisibilityMayChange() {
    if (screenState == SCREEN_STATE_OFF) {
      List<ToroPlayer> players = playerManager.getPlayers();
      for (ToroPlayer player : players) {
        if (player.isPlaying()) {
          this.savePlaybackInfo(player.getPlayerOrder(), player.getCurrentPlaybackInfo());
          playerManager.pause(player);
        }
      }
    } else if (screenState == SCREEN_STATE_ON
        // Container is focused in current Window
        && hasFocus()
        // In fact, Android 24+ supports multi-window mode in which visible Window may not have focus.
        // In that case, other triggers will supposed to be called and we are safe here.
        // Need further investigation if need.
        && hasWindowFocus()) {
      // tmpStates may be consumed already, if there is a good reason for that, so not a big deal.
      if (tmpStates != null && tmpStates.size() > 0) {
        for (int i = 0; i < tmpStates.size(); i++) {
          int order = tmpStates.keyAt(i);
          PlaybackInfo playbackInfo = tmpStates.get(order);
          this.savePlaybackInfo(order, playbackInfo);
        }
      }
      tmpStates = null;
      dispatchUpdateOnAnimationFinished(true);
    }
  }

  @Override protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    final Collection<Integer> savedOrders = this.getSavedPlayerOrders();
    if (savedOrders.isEmpty()) return superState;
    // Process saving playback state from here since Client wants this.
    final SparseArray<PlaybackInfo> states = new SparseArray<>();

    List<ToroPlayer> source = playerManager.getPlayers();
    List<Integer> playingOrders = new ArrayList<>();
    for (ToroPlayer player : source) {
      if (player.isPlaying()) {
        playingOrders.add(player.getPlayerOrder());

        PlaybackInfo info = player.getCurrentPlaybackInfo();
        this.savePlaybackInfo(player.getPlayerOrder(), info);
        states.put(player.getPlayerOrder(), info);
        // playerManager.pause(player);
      }
    }

    savedOrders.removeAll(playingOrders);

    for (Integer order : savedOrders) {
      states.put(order, this.getPlaybackInfo(order));
    }

    boolean recreating =
        getContext() instanceof Activity && ((Activity) getContext()).isChangingConfigurations();

    // Release current players on recreation event only.
    // Note that there are cases where this method is called without the activity destroying/recreating.
    // For example: in API 26 (my test mostly run on O), when user clicking to "Current App" button,
    // current Activity will enter the "Stop" state but not be destroyed/recreated and View hierarchy
    // state will be saved (this method is called).
    //
    // In those cases, we don't need to release current resources.
    if (recreating) {
      for (ToroPlayer player : source) {
        playerManager.release(player);
        playerManager.detachPlayer(player);
      }
    }

    // Client must consider this behavior using CacheManager implement.
    PlayerViewState playerViewState = new PlayerViewState(superState);
    playerViewState.statesCache = states;

    // To mark that this method was called.
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
    int cacheSize;
    if (saveStates != null && (cacheSize = saveStates.size()) > 0) {
      for (int i = 0; i < cacheSize; i++) {
        int order = saveStates.keyAt(i);
        PlaybackInfo playbackInfo = (PlaybackInfo) saveStates.get(order);
        this.savePlaybackInfo(order, playbackInfo);
      }
    }
  }

  /**
   * Store the array of {@link PlaybackInfo} of recently cached playback. This state will be used
   * only when {@link #cacheManager} is not {@code null}. Extension of {@link Container} must
   * also have its own version of {@link SavedState} extends this {@link PlayerViewState}.
   */
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
     * Called by CREATOR
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
        new ClassLoaderCreator<PlayerViewState>() { // Added from API 13
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
      return "Cache{" + "states=" + statesCache + '}';
    }
  }

  private final class ToroDataObserver extends AdapterDataObserver {

    private Adapter adapter;

    final void registerAdapter(Adapter adapter) {
      if (this.adapter == adapter) return;
      if (this.adapter != null) this.adapter.unregisterAdapterDataObserver(this);
      this.adapter = adapter;
      if (this.adapter != null) this.adapter.registerAdapterDataObserver(this);
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

  /**
   * A {@link Handler.Callback} that will fake a scroll with {@link #SCROLL_STATE_IDLE} to refresh
   * all the playback.
   */
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

  /**
   * An utility interface, used by {@link Container} to filter for {@link ToroPlayer}.
   */
  public interface Filter {

    /**
     * Check a {@link ToroPlayer} for a condition.
     *
     * @param player the {@link ToroPlayer} to check.
     * @return {@code true} if this accepts the {@link ToroPlayer}, {@code false} otherwise.
     */
    boolean accept(@NonNull ToroPlayer player);

    /**
     * A built-in {@link Filter} that accepts only {@link ToroPlayer} that is playing.
     */
    Filter PLAYING = new Filter() {
      @Override public boolean accept(@NonNull ToroPlayer player) {
        return player.isPlaying();
      }
    };

    /**
     * A built-in {@link Filter} that accepts only {@link ToroPlayer} that is managed by Container.
     * Actually any {@link ToroPlayer} to be filtered is already managed.
     */
    Filter MANAGING = new Filter() {
      @Override public boolean accept(@NonNull ToroPlayer player) {
        return true;
      }
    };
  }
}
