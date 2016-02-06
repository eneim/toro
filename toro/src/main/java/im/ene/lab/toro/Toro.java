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

package im.ene.lab.toro;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewParent;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by eneim on 1/31/16.
 * <p/>
 * Control Application's lifecycle to properly handling callbacks, prevent Memory leak and
 * unexpected behavior;
 *
 * @<code> </code>
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) public final class Toro
    implements Application.ActivityLifecycleCallbacks {

  private static final Object LOCK = new Object();

  // Singleton
  static volatile Toro sInstance;

  // It requires client to detach Activity/unregister View to prevent Memory leak
  // Use RecyclerView#hashCode() to sync between maps
  private final Map<Integer, RecyclerView> mViews = new ConcurrentHashMap<>();
  private final Map<Integer, ToroScrollListener> mListeners = new ConcurrentHashMap<>();

  // !IMPORTANT I limit this Map capacity to 3
  private StateLinkedList mStates;

  private ToroStrategy mStrategy = Strategies.MOST_VISIBLE_TOP_DOWN;  // Default strategy

  /**
   * Helper object, support RecyclerView's ViewHolder
   */
  private static VideoViewHolderHelper RECYCLER_VIEW_HELPER = new VideoViewHolderHelper() {
    @Override public void onAttachedToParent(ToroPlayer player, View itemView, ViewParent parent) {
      for (Map.Entry<Integer, RecyclerView> entry : sInstance.mViews.entrySet()) {
        RecyclerView view = entry.getValue();
        if (view != null && view == parent) {
          ToroScrollListener listener = sInstance.mListeners.get(view.hashCode());
          if (listener != null && listener.getManager().getPlayer() == null) {
            if (player.wantsToPlay() && player.isAbleToPlay() &&
                getStrategy().allowsToPlay(player, parent)) {
              listener.getManager().setPlayer(player);
              listener.getManager().restoreVideoState(player.getVideoId());
              listener.getManager().startPlayback();
              player.onPlaybackStarted();
            }
          }
        }
      }
    }

    @Override
    public void onDetachedFromParent(ToroPlayer player, View itemView, ViewParent parent) {
      for (Map.Entry<Integer, RecyclerView> entry : sInstance.mViews.entrySet()) {
        RecyclerView view = entry.getValue();
        if (view != null && view == parent) {
          ToroScrollListener listener = sInstance.mListeners.get(view.hashCode());
          // Manually save Video state
          if (listener != null && player.equals(listener.getManager().getPlayer())) {
            listener.getManager()
                .saveVideoState(player.getVideoId(), player.getCurrentPosition(),
                    player.getDuration());
            if (player.isPlaying()) {
              listener.getManager().pausePlayback();
              player.onPlaybackPaused();
            }
          }
        }
      }
    }

    @Override public boolean onItemLongClick(ToroPlayer player, View itemView, ViewParent parent) {
      RecyclerView view = null;
      ToroScrollListener listener = null;
      for (Map.Entry<Integer, RecyclerView> entry : sInstance.mViews.entrySet()) {
        view = entry.getValue();
        if (view != null && view == parent) {
          listener = sInstance.mListeners.get(view.hashCode());
          break;
        }
      }

      // Important components are missing, return
      if (view == null || listener == null) {
        return false;
      }

      // Being pressed player is not be able to play, return
      if (!player.wantsToPlay() || !player.isAbleToPlay() ||
          !getStrategy().allowsToPlay(player, parent)) {
        return false;
      }

      VideoPlayerManager manager = listener.getManager();
      ToroPlayer currentPlayer = manager.getPlayer();

      // Being pressed player is a new one
      if (!player.equals(currentPlayer)) {
        // All condition to switch players has passed, process the switching
        // Manually save Video state
        // Not the current player, and new player wants to play, so switch players
        if (currentPlayer != null) {
          manager.saveVideoState(currentPlayer.getVideoId(), currentPlayer.getCurrentPosition(),
              currentPlayer.getDuration());
          if (currentPlayer.isPlaying()) {
            manager.pausePlayback();
            currentPlayer.onPlaybackPaused();
          }
        }

        // Trigger new player
        manager.setPlayer(player);
        manager.restoreVideoState(player.getVideoId());
        manager.startPlayback();
        player.onPlaybackStarted();
        return true;
      } else {
        // Pressing current player, pause it if it is playing
        if (currentPlayer.isPlaying()) {
          manager.saveVideoState(currentPlayer.getVideoId(), currentPlayer.getCurrentPosition(),
              currentPlayer.getDuration());
          if (currentPlayer.isPlaying()) {
            manager.pausePlayback();
            currentPlayer.onPlaybackPaused();
          }
        } else {
          // It's paused, so we resume it
          manager.restoreVideoState(currentPlayer.getVideoId());
          manager.startPlayback();
          currentPlayer.onPlaybackStarted();
        }
      }

      return false;
    }

    @Override public void onPrepared(ToroPlayer player, View itemView, ViewParent parent,
        MediaPlayer mediaPlayer) {
      checkNotNull();
      sInstance.onPrepared(player, itemView, parent, mediaPlayer);
    }

    @Override public void onCompletion(ToroPlayer player, MediaPlayer mp) {
      checkNotNull();
      sInstance.onCompletion(player, mp);
    }

    @Override public boolean onError(ToroPlayer player, MediaPlayer mp, int what, int extra) {
      checkNotNull();
      return sInstance.onError(player, mp, what, extra);
    }

    @Override public boolean onInfo(ToroPlayer player, MediaPlayer mp, int what, int extra) {
      checkNotNull();
      return sInstance.onInfo(player, mp, what, extra);
    }

    @Override public void onSeekComplete(ToroPlayer player, MediaPlayer mp) {
      checkNotNull();
      sInstance.onSeekComplete(player, mp);
    }
  };

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
   */
  public static void detach(Activity activity) {
    checkNotNull();
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
    checkNotNull();
    return sInstance.mStrategy;
  }

  /**
   * Support custom playing policy
   *
   * @param strategy requested policy from client
   */
  public static void setStrategy(@NonNull ToroStrategy strategy) {
    checkNotNull();
    sInstance.mStrategy = strategy;
  }

  /**
   * Register a View (currently, must be one of RecyclerView or ListView) to listen to its Videos
   *
   * @param view which will be registered
   */
  public static void register(RecyclerView view) {
    checkNotNull();
    if (view == null) {
      throw new NullPointerException("Registering View must not be null");
    }

    if (sInstance.mViews.containsKey(view.hashCode())) {
      if (sInstance.mListeners.containsKey(view.hashCode())) {
        sInstance.mListeners.get(view.hashCode()).getManager().onRegistered();
        return;
      }
    }

    // 1. retrieve current TotoManager instance
    VideoPlayerManager playerManager = null;
    RecyclerView.Adapter adapter = view.getAdapter();
    // Client of this API should implement ToroManager to her Adapter.
    if (adapter instanceof VideoPlayerManager) {
      playerManager = (VideoPlayerManager) adapter;
    }

    // If no manager found, fallback to Built-in Manager
    if (playerManager == null) {
      playerManager = new VideoPlayerManagerImpl();
    }

    RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
    ToroScrollListener listener;
    if (layoutManager instanceof LinearLayoutManager) {
      listener = new LinearLayoutScrollListener(playerManager);
    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
      listener = new StaggeredGridLayoutScrollListener(playerManager);
    } else {
      throw new IllegalArgumentException(
          "Unexpected layout manager: " + layoutManager.getClass().getSimpleName());
    }

    view.addOnScrollListener(listener);
    // Cache
    sInstance.mViews.put(view.hashCode(), view);
    sInstance.mListeners.put(view.hashCode(), listener);

    final State state;
    if (!sInstance.mStates.containsKey(view.hashCode())) {
      state = new State();
      sInstance.mStates.put(view.hashCode(), state);
    } else {
      state = sInstance.mStates.get(view.hashCode());
    }

    if (state != null && state.player != null) {
      playerManager.setPlayer(state.player);
      playerManager.saveVideoState(state.player.getVideoId(), state.position,
          state.player.getDuration());
    }

    playerManager.onRegistered();
  }

  /**
   * Unregister a registered View
   *
   * @param view which will be unregistered
   */
  public static void unregister(RecyclerView view) {
    checkNotNull();
    if (view == null) {
      throw new NullPointerException("Un-registering View must not be null");
    }

    if (sInstance.mViews.containsKey(view.hashCode())) {
      // Obtain listener which will be removed
      ToroScrollListener listener = sInstance.mListeners.remove(view.hashCode());
      // Process related View
      if (listener != null) {
        // Cleanup manager
        // 1. Save this state
        final State state;
        if (sInstance.mStates.containsKey(view.hashCode())) {
          state = sInstance.mStates.get(view.hashCode());
        } else {
          state = new State();
          sInstance.mStates.put(view.hashCode(), state);
        }

        if (listener.getManager().getPlayer() != null) {
          state.player = listener.getManager().getPlayer();
          state.position = listener.getManager().getPlayer().getCurrentPosition();
        }

        listener.getManager().onUnregistered();
        view.removeOnScrollListener(listener);
      }
      // Remove from cache
      sInstance.mViews.remove(view.hashCode());
    }
  }

  void onCompletion(ToroPlayer player, MediaPlayer mediaPlayer) {
    player.onPlaybackStopped();
    for (ToroScrollListener listener : sInstance.mListeners.values()) {
      VideoPlayerManager manager = listener.getManager();
      if (player.equals(manager.getPlayer())) {
        manager.saveVideoState(player.getVideoId(), 0, player.getDuration());
        manager.pausePlayback();
        break;
      }
    }
  }

  final void onPrepared(ToroPlayer player, View container, ViewParent parent,
      MediaPlayer mediaPlayer) {
    player.onVideoPrepared(mediaPlayer);
    for (Map.Entry<Integer, ToroScrollListener> entry : sInstance.mListeners.entrySet()) {
      Integer key = entry.getKey();
      ToroScrollListener listener = entry.getValue();
      RecyclerView view = sInstance.mViews.get(key);
      if (view != null && view == parent) { // Found the parent view in our cache
        VideoPlayerManager manager = listener.getManager();
        // 1. Check if current manager wrapped this player
        if (player.equals(manager.getPlayer())) {
          if (player.wantsToPlay() && player.isAbleToPlay() && getStrategy().allowsToPlay(player,
              parent)) {
            manager.restoreVideoState(player.getVideoId());
            manager.startPlayback();
            player.onPlaybackStarted();
          }
          break;
        } else {
          if (manager.getPlayer() == null) {
            if (player.wantsToPlay() && player.isAbleToPlay() && getStrategy().allowsToPlay(player,
                parent)) {
              manager.setPlayer(player);
              manager.restoreVideoState(player.getVideoId());
              manager.startPlayback();
              player.onPlaybackStarted();
            }
          }
          break;
        }
      }
    }
  }

  boolean onError(ToroPlayer player, MediaPlayer mp, int what, int extra) {
    player.onPlaybackError(mp, what, extra);
    for (ToroScrollListener listener : sInstance.mListeners.values()) {
      VideoPlayerManager manager = listener.getManager();
      if (player.equals(manager.getPlayer())) {
        manager.saveVideoState(player.getVideoId(), 0, player.getDuration());
        manager.pausePlayback();
        return true;
      }
    }
    return false;
  }

  boolean onInfo(ToroPlayer player, MediaPlayer mp, int what, int extra) {
    player.onPlaybackInfo(mp, what, extra);
    return true;
  }

  void onSeekComplete(ToroPlayer player, MediaPlayer mp) {
    // Do nothing
  }

  @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    if (mStates == null) {
      mStates = new StateLinkedList(3);
    }
  }

  @Override public void onActivityStarted(Activity activity) {

  }

  @Override public void onActivityResumed(Activity activity) {
    for (Map.Entry<Integer, ToroScrollListener> entry : mListeners.entrySet()) {
      ToroScrollListener listener = entry.getValue();
      State state = mStates.get(entry.getKey());
      VideoPlayerManager manager = listener.getManager();
      if (manager.getPlayer() == null) {
        if (state != null && state.player != null) {
          manager.setPlayer(state.player);
          manager.saveVideoState(state.player.getVideoId(), state.position,
              state.player.getDuration());
        }
      }

      if (manager.getPlayer() != null) {
        manager.startPlayback();
        manager.getPlayer().onActivityResumed();
      }
    }
  }

  @Override public void onActivityPaused(Activity activity) {
    for (Map.Entry<Integer, ToroScrollListener> entry : mListeners.entrySet()) {
      ToroScrollListener listener = entry.getValue();
      State state = mStates.get(entry.getKey());
      if (state == null) {
        state = new State();
        mStates.put(entry.getKey(), state);
      }

      VideoPlayerManager manager = listener.getManager();
      if (manager.getPlayer() != null) {
        // Save state
        state.player = manager.getPlayer();
        state.position = manager.getPlayer().getCurrentPosition();

        manager.saveVideoState(manager.getPlayer().getVideoId(),
            manager.getPlayer().getCurrentPosition(), manager.getPlayer().getDuration());
        if (manager.getPlayer().isPlaying()) {
          manager.pausePlayback();
          manager.getPlayer().onPlaybackPaused();
        }

        manager.getPlayer().onActivityPaused();
      }
    }
  }

  @Override public void onActivityStopped(Activity activity) {

  }

  @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

  }

  @Override public void onActivityDestroyed(Activity activity) {
    if (mStates != null) {
      for (State state : mStates.values()) {
        if (state.player != null) {
          // Release resource if there is any
          state.player.pause();
          state.player.onActivityPaused();
          // Release this player
          state.player = null;
        }
      }

      mStates.clear();
    }
  }

  public static final class Strategies {

    /**
     * Among all playable Videos, select the most visible item (Max {@link
     * ToroPlayer#visibleAreaOffset()}). In case there are more than one item, chose the first item
     * on the top
     */
    public static final ToroStrategy MOST_VISIBLE_TOP_DOWN = new ToroStrategy() {

      @Override public String getDescription() {
        return "MOST_VISIBLE_TOP_DOWN";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
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
        Rect rect = new Rect();
        player.getVideoView().getDrawingRect(rect);
        player.getVideoView().getWindowVisibleDisplayFrame(rect);
        return true;
      }

      @Override public boolean allowsImmediateReplay() {
        return false;
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
        return "MOST_VISIBLE_TOP_DOWN_KEEP_LAST";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
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
        return true;
      }

      @Override public boolean allowsImmediateReplay() {
        return false;
      }
    };

    /**
     * Scan top down of candidates, chose the first playable Video
     */
    public static final ToroStrategy FIRST_PLAYABLE_TOP_DOWN = new ToroStrategy() {
      @Override public String getDescription() {
        return "FIRST_PLAYABLE_TOP_DOWN";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
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
        return true;
      }

      @Override public boolean allowsImmediateReplay() {
        return false;
      }
    };

    /**
     * Scan top down (by layout direction) of candidates, chose the first playable Video. But if
     * current player is still playable, but not on the top, we keep using it
     */
    public static final ToroStrategy FIRST_PLAYABLE_TOP_DOWN_KEEP_LAST = new ToroStrategy() {

      @Override public String getDescription() {
        return "FIRST_PLAYABLE_TOP_DOWN_KEEP_LAST";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        if (candidates == null || candidates.size() < 1) {
          return null;
        }

        return candidates.get(0);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        return true;
      }

      @Override public boolean allowsImmediateReplay() {
        return false;
      }
    };
  }

  @Nullable static VideoViewHolderHelper getHelper(@NonNull ToroPlayer player) {
    checkNotNull();
    if (player instanceof RecyclerView.ViewHolder) {
      return RECYCLER_VIEW_HELPER;
    }

    return null;
  }

  static void checkNotNull() {
    if (sInstance == null) {
      throw new IllegalStateException(
          "Toro has not been attached to your Activity or you Application. Please refer the doc");
    }
  }

  /**
   * Used to save current playing states. Need to be cleaned after each Activity has been
   * destroyed.
   */
  private static class State {

    private ToroPlayer player;

    private Integer position;
  }

  private static class StateLinkedList extends LinkedHashMap<Integer, State> {

    private int mCapacity = 1;

    public StateLinkedList(int initialCapacity) {
      super(initialCapacity);
      mCapacity = initialCapacity;
    }

    @Override protected boolean removeEldestEntry(Entry<Integer, State> eldest) {
      return size() > mCapacity;
    }
  }
}
