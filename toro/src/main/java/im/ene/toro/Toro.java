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

package im.ene.toro;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by eneim on 1/31/16.
 *
 * Control Application's lifecycle to properly handling callbacks, prevent Memory leak and
 * unexpected behavior;
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)  //
public final class Toro implements Application.ActivityLifecycleCallbacks {

  private static final String TAG = "LOG:TORO";
  private static final Object LOCK = new Object();

  public static final double DEFAULT_OFFSET = 0.75;

  /**
   * Stop playback strategy
   */
  private static final ToroStrategy REST = new ToroStrategy() {
    @Override public String getDescription() {
      return "'Do nothing' Strategy";
    }

    @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
      return null;
    }

    @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
      return false;
    }
  };

  // Singleton, GOD object
  static volatile Toro sInstance;

  // Used to swap strategies if need. It should be a strong reference.
  private static volatile ToroStrategy cachedStrategy;

  // It requires client to detach Activity/unregister View to prevent Memory leak
  // Use RecyclerView#hashCode() to sync between maps
  final Map<Integer, RecyclerView> mViews = new ConcurrentHashMap<>();
  final Map<Integer, ToroScrollListener> mListeners = new ConcurrentHashMap<>();

  // !IMPORTANT: I limit this Map capacity to 3
  private StateLinkedList mStates;

  // Default strategy
  private ToroStrategy mStrategy = Strategies.MOST_VISIBLE_TOP_DOWN;

  /**
   * Attach an activity to Toro. Toro register activity's life cycle to properly handle Screen
   * visibility: free necessary resource if User doesn't need it anymore
   *
   * @param activity the Activity to which Toro gonna attach to
   */
  public static void attach(@NonNull Activity activity) {
    init(activity.getApplication());
    if (sInstance.mStates == null) {
      sInstance.mStates = new StateLinkedList(3);
    }
  }

  /**
   * Same purpose to {@link Toro#attach(Activity)}, but support overall the Application
   *
   * @param application the Application where Toro will apply for.
   */
  public static void init(Application application) {
    if (sInstance == null) {
      synchronized (LOCK) {
        sInstance = new Toro();
      }
    }

    if (application != null) {
      application.registerActivityLifecycleCallbacks(sInstance);
    }
  }

  /**
   * Carefully detach current Activity from Toro. Should be coupled with {@link
   * Toro#attach(Activity)}
   *
   * @param activity The host Activity where Toro will detach from.
   */
  public static void detach(Activity activity) {
    Application application = activity.getApplication();
    if (application != null) {
      application.unregisterActivityLifecycleCallbacks(sInstance);
    }

    if (sInstance.mStates != null) {
      sInstance.mStates.clear();
    }

    // Cleanup
    for (RecyclerView view : sInstance.mViews.values()) {
      unregister(view);
    }
  }

  public static ToroStrategy getStrategy() {
    return sInstance.mStrategy;
  }

  /**
   * Support custom playing policy
   *
   * @param strategy requested policy from client
   */
  public static void setStrategy(@NonNull ToroStrategy strategy) {
    if (sInstance.mStrategy == strategy) {
      // Nothing changes
      return;
    }

    sInstance.mStrategy = strategy;
    dispatchStrategyChanged(strategy);
  }

  /**
   * Register a View (currently, must be one of RecyclerView) to listen to its Videos
   *
   * @param view which will be registered
   */
  public static void register(RecyclerView view) {
    if (view == null) {
      throw new NullPointerException("Registering View must not be null");
    }

    if (sInstance.mViews.containsKey(view.hashCode())) {
      if (sInstance.mListeners.containsKey(view.hashCode())) {
        sInstance.mListeners.get(view.hashCode()).getManager().onRegistered();
        return;
      }
    }

    // 1. Retrieve current MediaPlayerManager instance
    final MediaPlayerManager playerManager;
    RecyclerView.Adapter adapter = view.getAdapter();
    // Client of this API should implement MediaPlayerManager to its Adapter.
    if (adapter instanceof MediaPlayerManager) {
      playerManager = (MediaPlayerManager) adapter;
    } else {
      playerManager = new MediaPlayerManagerImpl();
    }

    final ToroScrollListener listener = new ToroScrollListener(playerManager);
    view.addOnScrollListener(listener);
    // Save to Cache
    sInstance.mViews.put(view.hashCode(), view);
    sInstance.mListeners.put(view.hashCode(), listener);

    final SavedState state;
    if (sInstance.mStates.containsKey(view.hashCode())) {
      state = sInstance.mStates.get(view.hashCode());
    } else {
      state = new SavedState();
      sInstance.mStates.put(view.hashCode(), state);
    }

    if (state.player != null) {
      // Cold start MediaPlayerManager from a saved state
      playerManager.setPlayer(state.player);
      playerManager.saveVideoState(state.player.getMediaId(),
          playerManager.getSavedPosition(state.player.getMediaId()), state.player.getDuration());

      if (!state.player.isPlaying() && state.player.wantsToPlay() && Toro.getStrategy()
          .allowsToPlay(state.player, view)) {
        playerManager.restoreVideoState(state.player.getMediaId());
        playerManager.startPlayback();
      }
    }

    // Done registering new View
    playerManager.onRegistered();
  }

  /**
   * Unregister a registered View
   *
   * @param view which will be unregistered
   */
  public static void unregister(RecyclerView view) {
    if (view == null) {
      throw new NullPointerException("Un-registering View must not be null");
    }

    if (sInstance.mListeners.containsKey(view.hashCode())) {
      // Obtain listener which will be removed
      ToroScrollListener listener = sInstance.mListeners.remove(view.hashCode());
      // Process related View
      // Cleanup manager
      if (listener.getManager().getPlayer() != null) {
        final ToroPlayer player = listener.getManager().getPlayer();
        // 1. Save current state
        final SavedState state;
        if (sInstance.mStates.containsKey(view.hashCode())) {
          state = sInstance.mStates.get(view.hashCode());
        } else {
          state = new SavedState();
          sInstance.mStates.put(view.hashCode(), state);
        }
        state.player = player;
        state.position = player.getCurrentPosition();

        listener.getManager()
            .saveVideoState(player.getMediaId(), player.getCurrentPosition(), player.getDuration());
        if (player.isPlaying()) {
          listener.getManager().pausePlayback();
        }
      }

      listener.getManager().onUnregistered();
      view.removeOnScrollListener(listener);
      // Remove View from cache
      sInstance.mViews.remove(view.hashCode());
    }
  }

  public static void rest(boolean willPause) {
    if (willPause) {
      cachedStrategy = getStrategy();
      setStrategy(REST);
    } else {
      // Don't allow to unrest if Toro has not been in rested state. Be careful.
      if (getStrategy() != REST) {
        throw new IllegalStateException("Toro has already waken up.");
      }

      if (cachedStrategy != null) { // Actually, cachedStrategy would not be null here.
        setStrategy(cachedStrategy);
        cachedStrategy = null;  // release
      }
    }
  }

  // Experiment
  public static boolean isResting() {
    return getStrategy() == REST;
  }

  private static void dispatchStrategyChanged(ToroStrategy newStrategy) {
    for (RecyclerView view : sInstance.mViews.values()) {
      ToroScrollListener listener = sInstance.mListeners.get(view.hashCode());
      if (listener != null) { // Trigger an 'idle scroll'
        listener.onScrollStateChanged(view, RecyclerView.SCROLL_STATE_IDLE);
      }
    }
  }

  @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    if (mStates == null) {
      mStates = new StateLinkedList(3);
    }
  }

  @Override public void onActivityStarted(Activity activity) {
    if (SDK_INT > 23) { // Android N and up
      dispatchOnActivityActive(activity);
    }
  }

  @Override public void onActivityResumed(Activity activity) {
    if (SDK_INT <= 23) {
      dispatchOnActivityActive(activity);
    }
  }

  @Override public void onActivityPaused(Activity activity) {
    if (SDK_INT <= 23) {
      dispatchOnActivityInactive(activity);
    }
  }

  @Override public void onActivityStopped(Activity activity) {
    if (SDK_INT > 23) { // Android N and up
      dispatchOnActivityInactive(activity);
    }
  }

  @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    // TODO: deal with orientation changes
  }

  @Override public void onActivityDestroyed(Activity activity) {
    if (mStates != null) {
      for (SavedState state : mStates.values()) {
        if (state.player != null) {
          // Release resource if there is any
          state.player.stop();
          state.player.onActivityInactive();
          // Release this player
          state.player = null;
        }
      }

      mStates.clear();
    }
  }

  // Built-in Strategies
  public static final class Strategies {

    /**
     * Among all playable Videos, select the most visible item (Max {@link
     * ToroPlayer#visibleAreaOffset()}). In case there are more than one item, chose the first item
     * on the top
     */
    public static final ToroStrategy MOST_VISIBLE_TOP_DOWN = new ToroStrategy() {

      @Override public String getDescription() {
        return "Most visible item, top - down";
      }

      @Nullable @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        if (candidates == null || candidates.size() < 1) {
          return null;
        }

        // 1. Sort candidates by the order of player
        Collections.sort(candidates, new Comparator<ToroPlayer>() {
          @Override public int compare(ToroPlayer lhs, ToroPlayer rhs) {
            return lhs.getPlayOrder() - rhs.getPlayOrder();
          }
        });

        // 2. Sort candidates by the visible offset
        Collections.sort(candidates, new Comparator<ToroPlayer>() {
          @Override public int compare(ToroPlayer lhs, ToroPlayer rhs) {
            return Float.compare(rhs.visibleAreaOffset(), lhs.visibleAreaOffset());
          }
        });

        return candidates.get(0);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        return doAllowsToPlay(player, parent);
      }
    };

    /**
     * Among all playable Videos, select the most visible item (Max {@link
     * ToroPlayer#visibleAreaOffset()}). In case there are more than one item, chose the first item
     * on the top. But if current player is still playable, but not staying on the top, we still
     * keep it.
     */
    public static final ToroStrategy MOST_VISIBLE_TOP_DOWN_KEEP_LAST = new ToroStrategy() {
      @Override public String getDescription() {
        return "Most visible item, top - down. Keep last playing item.";
      }

      @Nullable @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        if (candidates == null || candidates.size() < 1) {
          return null;
        }

        // Sort candidates by the visible offset
        Collections.sort(candidates, new Comparator<ToroPlayer>() {
          @Override public int compare(ToroPlayer lhs, ToroPlayer rhs) {
            return Float.compare(rhs.visibleAreaOffset(), lhs.visibleAreaOffset());
          }
        });

        return candidates.get(0);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        return doAllowsToPlay(player, parent);
      }
    };

    /**
     * Scan top down of candidates, chose the first playable Video
     */
    public static final ToroStrategy FIRST_PLAYABLE_TOP_DOWN = new ToroStrategy() {
      @Override public String getDescription() {
        return "First playable item, top - down";
      }

      @Nullable @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        if (candidates == null || candidates.size() < 1) {
          return null;
        }

        // 1. Sort candidates by the order of player
        Collections.sort(candidates, new Comparator<ToroPlayer>() {
          @Override public int compare(ToroPlayer lhs, ToroPlayer rhs) {
            return lhs.getPlayOrder() - rhs.getPlayOrder();
          }
        });

        return candidates.get(0);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        return doAllowsToPlay(player, parent);
      }
    };

    /**
     * Scan top down (by layout direction) of candidates, chose the first playable Video. But if
     * current player is still playable, but not on the top, we keep using it
     */
    public static final ToroStrategy FIRST_PLAYABLE_TOP_DOWN_KEEP_LAST = new ToroStrategy() {

      @Override public String getDescription() {
        return "First playable item, top - down. Keep last playing item.";
      }

      @Nullable @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        if (candidates == null || candidates.size() < 1) {
          return null;
        }

        return candidates.get(0);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        return doAllowsToPlay(player, parent);
      }
    };
  }

  /**
   * @hide
   */
  static boolean doAllowsToPlay(ToroPlayer player, ViewParent parent) {
    Rect windowRect = new Rect();
    Rect parentRect = new Rect();
    if (parent instanceof View) {
      // 1. Get Window's vision from parent
      ((View) parent).getWindowVisibleDisplayFrame(windowRect);
      // 2. Get parent's global rect
      ((View) parent).getGlobalVisibleRect(parentRect, null);
    }
    // 3. Get player global rect
    View videoView = player.getPlayerView();
    Rect videoRect = new Rect();
    // Headache !!!
    int[] screenLoc = new int[2];
    videoView.getLocationOnScreen(screenLoc);
    videoRect.left += screenLoc[0];
    videoRect.right += screenLoc[0] + videoView.getWidth();
    videoRect.top += screenLoc[1];
    videoRect.bottom += screenLoc[1] + videoView.getHeight();

    // Condition: window contains parent, and parent contains Video or parent intersects Video
    return windowRect.contains(parentRect) && (parentRect.contains(videoRect)
        || parentRect.intersect(videoRect));
  }

  // Centralize Video state callbacks

  void onVideoPrepared(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {
    MediaPlayerManager manager = null;
    ToroScrollListener listener;
    RecyclerView view;
    // Find correct Player manager for this player
    for (Map.Entry<Integer, ToroScrollListener> entry : Toro.sInstance.mListeners.entrySet()) {
      Integer key = entry.getKey();
      view = Toro.sInstance.mViews.get(key);
      if (view != null && view == parent) { // Found the parent view in our cache
        listener = entry.getValue();
        manager = listener.getManager();
        break;
      }
    }

    if (manager == null) {
      return;
    }

    // 1. Check if current manager wrapped this player
    if (player.equals(manager.getPlayer())) {
      if (player.wantsToPlay() && Toro.getStrategy().allowsToPlay(player, parent)) {
        manager.restoreVideoState(player.getMediaId());
        manager.startPlayback();
      }
    } else {
      // There is no current player, but this guy is prepared, so let's him go ...
      if (manager.getPlayer() == null) {
        // ... if it's possible
        if (player.wantsToPlay() && Toro.getStrategy().allowsToPlay(player, parent)) {
          manager.setPlayer(player);
          manager.restoreVideoState(player.getMediaId());
          manager.startPlayback();
        }
      }
    }
  }

  void onPlaybackCompletion(@NonNull ToroPlayer player) {
    // 1. Internal jobs
    MediaPlayerManager manager = null;
    for (ToroScrollListener listener : Toro.sInstance.mListeners.values()) {
      manager = listener.getManager();
      if (player == manager.getPlayer()) {
        break;
      } else {
        manager = null;
      }
    }

    // Update video position as 0
    if (manager != null) {
      manager.saveVideoState(player.getMediaId(), 0L, player.getDuration());
    }
  }

  boolean onPlaybackError(@NonNull ToroPlayer player, @NonNull Exception error) {
    for (ToroScrollListener listener : Toro.sInstance.mListeners.values()) {
      MediaPlayerManager manager = listener.getManager();
      if (player.equals(manager.getPlayer())) {
        manager.saveVideoState(player.getMediaId(), 0L, player.getDuration());
        manager.pausePlayback();
      }
    }
    return true;
  }

  // Update to correctly support API 24+
  private void dispatchOnActivityInactive(Activity activity) {
    for (Map.Entry<Integer, RecyclerView> viewEntry : mViews.entrySet()) {
      if (viewEntry.getValue().getContext() == activity) {
        ToroScrollListener listener = mListeners.get(viewEntry.getKey());
        if (listener == null) { // This should not happen generally
          continue;
        }

        SavedState state = mStates.get(viewEntry.getKey());
        if (state == null) {
          state = new SavedState();
          mStates.put(viewEntry.getKey(), state);
        }

        MediaPlayerManager manager = listener.getManager();
        if (manager.getPlayer() != null) {
          // Save state
          state.player = manager.getPlayer();
          state.position = manager.getPlayer().getCurrentPosition();

          if (manager.getPlayer().isPlaying()) {
            manager.saveVideoState(manager.getPlayer().getMediaId(),
                manager.getPlayer().getCurrentPosition(), manager.getPlayer().getDuration());
            manager.pausePlayback();
          }

          manager.getPlayer().onActivityInactive();
        }
      }
    }
  }

  private void dispatchOnActivityActive(Activity activity) {
    for (Map.Entry<Integer, RecyclerView> viewEntry : mViews.entrySet()) {
      if (viewEntry.getValue().getContext() == activity) {
        ToroScrollListener listener = mListeners.get(viewEntry.getKey());
        if (listener == null) { // This should not happen generally
          continue;
        }

        SavedState state = mStates.get(viewEntry.getKey());
        MediaPlayerManager manager = listener.getManager();
        if (manager.getPlayer() == null) {
          if (state != null && state.player != null) {
            manager.setPlayer(state.player);
            manager.saveVideoState(state.player.getMediaId(), state.position,
                state.player.getDuration());
          }
        }

        if (manager.getPlayer() != null) {
          manager.getPlayer().onActivityActive();
          manager.restoreVideoState(manager.getPlayer().getMediaId());
          manager.startPlayback();
        }
      }
    }
  }
}
