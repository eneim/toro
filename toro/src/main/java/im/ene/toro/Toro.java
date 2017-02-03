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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by eneim on 1/31/16.
 *
 * Control Application's lifecycle to properly handling callbacks, prevent Memory leak and
 * unexpected behavior;
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)  //
public final class Toro implements Application.ActivityLifecycleCallbacks {

  private static final String TAG = "ToroLib";

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
  private final LinkedHashMap<RecyclerView, ToroBundle> bundles = new LinkedHashMap<>();

  // Default strategy
  private ToroStrategy mStrategy = Strategies.MOST_VISIBLE_TOP_DOWN;

  /**
   * Same purpose to {@link Toro#attach(Activity)}, but support overall the Application
   *
   * @param application the Application where Toro will apply for.
   */
  public static void init(Application application) {
    if (sInstance == null) {
      synchronized (Toro.class) {
        sInstance = new Toro();
      }

      application.registerActivityLifecycleCallbacks(sInstance);
      application.registerActivityLifecycleCallbacks(new LifeCycleDebugger());
    }
  }

  private static ToroStrategy getDefaultStrategy() {
    return sInstance.mStrategy;
  }

  static ToroStrategy getStrategy(ViewParent viewParent) {
    return getBundle(viewParent).getStrategy();
  }

  static ToroBundle getBundle(ViewParent viewParent) {
    if (viewParent == null) {
      throw new NullPointerException("View is null");
    }

    if (!(viewParent instanceof RecyclerView)) {
      throw new IllegalArgumentException("Only RecyclerView is accepted here");
    }

    return sInstance.bundles.get(viewParent);
  }

  /**
   * Support custom playing policy
   *
   * @param strategy requested policy from client
   */
  @Deprecated public static void setStrategy(@NonNull ToroStrategy strategy) {
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
  @Deprecated public static void register(RecyclerView view) {
    if (view == null) {
      throw new NullPointerException("Registering View must not be null");
    }

    if (sInstance.bundles.containsKey(view)) {
      sInstance.bundles.get(view).getManager().onRegistered();
      ;
      return;
    }

    ToroBundle bundle = new ToroBundle();
    bundle.setStrategy(Strategies.FIRST_PLAYABLE_TOP_DOWN);
    register(view, bundle);
  }

  static void register(RecyclerView view, @NonNull ToroBundle bundle) {
    if (view == null) {
      throw new NullPointerException("Registering View must not be null");
    }

    if (sInstance.bundles.containsKey(view)) {
      sInstance.bundles.get(view).getManager().onRegistered();
      return;
    }

    //noinspection ConstantConditions
    if (bundle == null || bundle.getStrategy() == null) {
      throw new IllegalArgumentException("Bundle must be non-null and has a Strategy");
    }

    // 1. Retrieve current PlayerManager instance
    final PlayerManager playerManager;
    RecyclerView.Adapter adapter = view.getAdapter();
    // Client of this API should implement PlayerManager to its Adapter.
    if (adapter instanceof PlayerManager) {
      playerManager = (PlayerManager) adapter;
    } else {
      // Toro 3+ will force the implementation of PlayerManager. Of course, there is delegation
      throw new RuntimeException("Adapter must be a PlayerManager");
    }
    bundle.setManager(playerManager);

    // setup new scroll listener
    OnScrollListenerImpl listener = new OnScrollListenerImpl();
    view.addOnScrollListener(listener);
    bundle.setScrollListener(listener);

    sInstance.bundles.put(view, bundle);

    // Done registering new View
    playerManager.onRegistered();
    // in case the Manager/Adapter has a preset Player and a saved playback state
    // (either coming back from Stopped state or a predefined one)
    if (playerManager.getPlayer() != null
        && playerManager.getSavedState(playerManager.getPlayer().getMediaId()) != null) {
      ToroPlayer player = playerManager.getPlayer();
      if (player.wantsToPlay() && player.wantsToPlay() && //
          bundle.getStrategy().allowsToPlay(player, view)) {
        if (!player.isPrepared()) {
          player.preparePlayer(false);
        } else if (!player.isPlaying()) {
          playerManager.restoreVideoState(player.getMediaId());
          playerManager.startPlayback();
        }
      }
    }
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

    ToroBundle bundle = sInstance.bundles.remove(view);

    PlayerManager manager = bundle.getManager();
    if (manager.getPlayer() != null) {
      final ToroPlayer player = manager.getPlayer();
      manager.saveVideoState(player.getMediaId(), //
          player.getCurrentPosition(), player.getDuration());
      if (player.isPlaying()) {
        manager.pausePlayback();
      }
    }

    manager.onUnregistered();
    view.removeOnScrollListener(bundle.getScrollListener());
  }

  static PlayerManager getManager(RecyclerView recyclerView) {
    return sInstance.bundles.get(recyclerView).getManager();
  }

  static PlayerManager getManager(OnScrollListenerImpl listener) {
    for (Map.Entry<RecyclerView, ToroBundle> entry : sInstance.bundles.entrySet()) {
      if (listener.equals(entry.getValue().getScrollListener())) {
        return entry.getValue().getManager();
      }
    }

    return null;
  }

  public static void resume() {
    rest(false);
  }

  public static void pause() {
    rest(true);
  }

  public static boolean isActive() {
    return !isResting();  // not resting!!
  }

  @Deprecated public static void rest(boolean willPause) {
    if (willPause) {
      cachedStrategy = getDefaultStrategy();
      setStrategy(REST);
    } else {
      // Don't allow to unrest if Toro has not been in rested state. Be careful.
      if (getDefaultStrategy() != REST) {
        throw new IllegalStateException("Toro has already waken up.");
      }

      if (cachedStrategy != null) { // Actually, cachedStrategy would not be null here.
        setStrategy(cachedStrategy);
        cachedStrategy = null;  // release
      }
    }
  }

  // Experiment
  @Deprecated public static boolean isResting() {
    return getDefaultStrategy() == REST;
  }

  private static void dispatchStrategyChanged(ToroStrategy newStrategy) {
    for (Map.Entry<RecyclerView, ToroBundle> entry : sInstance.bundles.entrySet()) {
      entry.getValue()
          .getScrollListener()
          .onScrollStateChanged(entry.getKey(), RecyclerView.SCROLL_STATE_IDLE);
    }
  }

  @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    // Do nothing here
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
    for (ToroBundle bundle : bundles.values()) {
      try {
        bundle.getManager().remove();
        bundle.getScrollListener().remove();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // Cleanup
    for (RecyclerView view : sInstance.bundles.keySet()) {
      unregister(view);
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
    ToroBundle bundle = null;
    PlayerManager manager = null;
    // Find correct Player manager for this player
    for (Map.Entry<RecyclerView, ToroBundle> entry : sInstance.bundles.entrySet()) {
      if (entry.getKey() == parent) {
        bundle = entry.getValue();
        manager = bundle.getManager();
        break;
      }
    }

    if (manager == null) {
      return;
    }

    // 1. Check if current manager wrapped this player
    if (player.equals(manager.getPlayer())) {
      if (player.wantsToPlay() && bundle.getStrategy().allowsToPlay(player, parent)) {
        // player.isPlaying() is always false here
        manager.restoreVideoState(player.getMediaId());
        manager.startPlayback();
      }
    } else {
      // There is no current player, but this guy is prepared, so let's him go ...
      if (manager.getPlayer() == null) {
        // ... if it's possible
        if (player.wantsToPlay() && bundle.getStrategy().allowsToPlay(player, parent)) {
          manager.setPlayer(player);
          // player.isPrepared() is always true here
          manager.restoreVideoState(player.getMediaId());
          manager.startPlayback();
        }
      }
    }
  }

  void onPlaybackCompletion(@NonNull ToroPlayer player) {
    // 1. Internal jobs
    PlayerManager manager = null;
    for (ToroBundle bundle : sInstance.bundles.values()) {
      if (player == bundle.getManager().getPlayer()) {
        manager = bundle.getManager();
        break;
      }
    }

    // Update video position as 0
    if (manager != null) {
      manager.saveVideoState(player.getMediaId(), 0L, player.getDuration());
    }
  }

  boolean onPlaybackError(@NonNull ToroPlayer player, @NonNull Exception error) {
    for (ToroBundle bundle : sInstance.bundles.values()) {
      if (player == bundle.getManager().getPlayer()) {
        bundle.getManager().saveVideoState(player.getMediaId(), 0L, player.getDuration());
        bundle.getManager().pausePlayback();
      }
    }

    return true;
  }

  // Update to correctly support API 24+
  private void dispatchOnActivityInactive(Activity activity) {
    for (Map.Entry<RecyclerView, ToroBundle> entry : bundles.entrySet()) {
      if (entry.getKey().getContext() == activity) {
        PlayerManager manager = entry.getValue().getManager();
        if (manager.getPlayer() != null) {
          if (manager.getPlayer().isPlaying()) {
            manager.saveVideoState(manager.getPlayer().getMediaId(),
                manager.getPlayer().getCurrentPosition(), manager.getPlayer().getDuration());
            manager.pausePlayback();
          }
          manager.getPlayer().releasePlayer();
          manager.getPlayer().onActivityInactive();
        }
      }
    }
  }

  private void dispatchOnActivityActive(Activity activity) {
    for (Map.Entry<RecyclerView, ToroBundle> entry : bundles.entrySet()) {
      if (entry.getKey().getContext() == activity) {  // reference equality
        PlayerManager manager = entry.getValue().getManager();
        if (manager.getPlayer() != null) {
          manager.getPlayer().onActivityActive();
          if (!manager.getPlayer().isPrepared()) {
            manager.getPlayer().preparePlayer(false);
          } else {
            manager.restoreVideoState(manager.getPlayer().getMediaId());
            manager.startPlayback();
          }
        }
      }
    }
  }

  // New implementation

  public static Builder with(Activity activity) {
    init(activity.getApplication());
    return new Builder();
  }

  public static class Builder {

    private final ToroBundle bundles = new ToroBundle();

    public Builder strategy(ToroStrategy strategy) {
      this.bundles.setStrategy(strategy);
      return this;
    }

    public void register(RecyclerView recyclerView) {
      if (bundles.getStrategy() == null) {
        // apply default strategy
        bundles.setStrategy(Strategies.FIRST_PLAYABLE_TOP_DOWN);
      }
      Toro.register(recyclerView, bundles);
    }
  }
}
