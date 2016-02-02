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
  static Toro sInstance;

  private ToroStrategy mStrategy = Strategies.MOST_VISIBLE_TOP_DOWN;  // Default policy
  // It requires client to detach Activity/unregister View to prevent Memory leak
  private final ConcurrentHashMap<RecyclerView, ToroScrollListener> mMm = new ConcurrentHashMap<>();

  /**
   * Attach an activity to Toro. Toro register activity's life cycle to properly handle Screen
   * visibility: free necessary resource if User doesn't need it anymore
   *
   * @param activity the Activity to which Toro gonna attach to
   */
  public static void attach(@NonNull Activity activity) {
    init(activity.getApplication());
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
    Application application = activity.getApplication();
    if (application != null) {
      application.unregisterActivityLifecycleCallbacks(sInstance);
    }

    // Cleanup
    for (RecyclerView view : sInstance.mMm.keySet()) {
      unregister(view);
    }
  }

  /**
   * Support custom playing policy
   *
   * @param policy requested policy from client
   */
  public static void setStrategy(@NonNull ToroStrategy policy) {
    sInstance.mStrategy = policy;
  }

  public static ToroStrategy getStrategy() {
    return sInstance.mStrategy;
  }

  /**
   * Register a View (currently, must be one of RecyclerView or ListView) to listen to its Videos
   *
   * @param view which will be registered
   */
  public static void register(RecyclerView view) {
    if (sInstance == null) {
      throw new IllegalStateException(
          "Toro has not been attached to your Activity or you Application. Please refer the doc");
    }

    if (view == null) {
      throw new NullPointerException("Registering View must not be null");
    }

    // Remove old images
    if (sInstance.mMm.containsKey(view)) {
      synchronized (LOCK) {
        sInstance.mMm.remove(view);
      }
    }

    synchronized (LOCK) {
      // 1. retrieve current TotoManager instance
      ToroManager manager = null;
      RecyclerView.Adapter adapter = view.getAdapter();
      if (adapter instanceof ToroManager) {
        manager = (ToroManager) adapter;
      }

      // Fallback to default Manager
      if (manager == null) {
        manager = new ToroManagerImpl();
      }

      RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
      ToroScrollListener listener;
      if (layoutManager instanceof LinearLayoutManager) {
        listener = new LinearLayoutScrollListener(manager);
      } else if (layoutManager instanceof StaggeredGridLayoutManager) {
        listener = new StaggeredGridLayoutScrollListener(manager);
      } else {
        throw new IllegalArgumentException("Unexpected layout manager: " + layoutManager);
      }

      view.addOnScrollListener(listener);
      // Cache
      // sInstance.mManagers.add(manager);
      sInstance.mMm.put(view, listener);
    }
  }

  /**
   * Unregister a registered View
   *
   * @param view which will be unregistered
   */
  public static void unregister(RecyclerView view) {
    if (sInstance == null) {
      throw new IllegalStateException(
          "Toro has not been attached to your Activity or you Application. Please refer the doc");
    }

    if (view == null) {
      throw new NullPointerException("Un-registering View must not be null");
    }

    synchronized (LOCK) {
      // Obtain listener which will be removed
      ToroScrollListener object = sInstance.mMm.remove(view);
      // Process related View
      if (object != null) {
        // Remove from Manager list
        // sInstance.mManagers.remove(object.getManager());
        // there is a set of <View, Listener> is removed
        view.removeOnScrollListener(object);
      }
    }
  }

  static void onCompletion(ToroPlayer player, MediaPlayer mediaPlayer) {
    for (ToroScrollListener listener : sInstance.mMm.values()) {
      ToroManager manager = listener.getManager();
      if (player.equals(manager.getPlayer())) {
        manager.saveVideoState(player.getVideoId(), 0, player.getDuration());
        break;
      }
    }
  }

  static void onPrepared(ToroPlayer player, View container, ViewParent parent,
      MediaPlayer mediaPlayer) {
    for (ToroScrollListener listener : sInstance.mMm.values()) {
      ToroManager manager = listener.getManager();
      if (player.equals(manager.getPlayer())) {
        manager.restoreVideoState(player, player.getVideoId());
        Rect containerRect = new Rect();
        Rect parentRect = null;
        container.getLocalVisibleRect(containerRect);
        if (parent != null && parent instanceof View) {
          parentRect = new Rect();
          ((View) parent).getLocalVisibleRect(parentRect);
        }
        if (player.wantsToPlay(parentRect, containerRect)) {
          manager.startVideo(player);
        }
        break;
      }
    }
  }

  static boolean onError(ToroPlayer player, MediaPlayer mp, int what, int extra) {
    return false;
  }

  static boolean onInfo(ToroPlayer player, MediaPlayer mp, int what, int extra) {
    return false;
  }

  static void onSeekComplete(ToroPlayer player, MediaPlayer mp) {

  }

  @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

  }

  @Override public void onActivityStarted(Activity activity) {

  }

  @Override public void onActivityResumed(Activity activity) {
    for (ToroScrollListener listener : sInstance.mMm.values()) {
      ToroManager manager = listener.getManager();
      if (manager.getPlayer() != null) {
        manager.getPlayer().onActivityResumed();
      }
    }
  }

  @Override public void onActivityPaused(Activity activity) {
    for (ToroScrollListener listener : sInstance.mMm.values()) {
      ToroManager manager = listener.getManager();
      if (manager.getPlayer() != null) {
        manager.getPlayer().onActivityPaused();
      }
    }
  }

  @Override public void onActivityStopped(Activity activity) {

  }

  @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

  }

  @Override public void onActivityDestroyed(Activity activity) {

  }

  public static final class Strategies {

    /**
     * Among all playable Videos, select the most visible item (Max {@link
     * ToroPlayer#visibleAreaOffset()}). In case there are more than one item, chose the first item
     * on the top
     */
    public static final ToroStrategy MOST_VISIBLE_TOP_DOWN = new ToroStrategy() {
      @Override public String getDescription() {
        return "Most visible item from top";
      }

      @Override public ToroPlayer getPlayer(List<ToroPlayer> candidates) {
        if (candidates == null || candidates.size() < 1) {
          return null;
        }

        // 1. Sort candidates by the order of player
        Collections.sort(candidates, new Comparator<ToroPlayer>() {
          @Override public int compare(ToroPlayer lhs, ToroPlayer rhs) {
            return lhs.getItemPosition() - rhs.getItemPosition();
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

      @Override public boolean allowsToPlay(ToroPlayer player, @Nullable Rect parentRect,
          @NonNull Rect childRect) {
        return true;
      }

      @Override public boolean requireCompletelyVisible() {
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
        return "Most visible item in list of candidates, chosen from top to bottom. "
            + "But if current player is not on the top but still playable, keep it";
      }

      @Override public ToroPlayer getPlayer(List<ToroPlayer> candidates) {
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

      @Override public boolean allowsToPlay(ToroPlayer player, @Nullable Rect parentRect,
          @NonNull Rect childRect) {
        return true;
      }

      @Override public boolean requireCompletelyVisible() {
        return false;
      }
    };

    /**
     * Scan top down of candidates, chose the first playable Video
     */
    public static final ToroStrategy FIRST_PLAYABLE_TOP_DOWN = new ToroStrategy() {
      @Override public String getDescription() {
        return "Scan top down of candidates, chose the first playable Video";
      }

      @Override public ToroPlayer getPlayer(List<ToroPlayer> candidates) {
        if (candidates == null || candidates.size() < 1) {
          return null;
        }

        // 1. Sort candidates by the order of player
        Collections.sort(candidates, new Comparator<ToroPlayer>() {
          @Override public int compare(ToroPlayer lhs, ToroPlayer rhs) {
            return lhs.getItemPosition() - rhs.getItemPosition();
          }
        });

        return candidates.get(0);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, @Nullable Rect parentRect,
          @NonNull Rect childRect) {
        return true;
      }

      @Override public boolean requireCompletelyVisible() {
        return false;
      }
    };

    /**
     * Scan top down of candidates, chose the first playable Video. But if current player is still
     * playable, but not on the top, we keep using it
     */
    public static final ToroStrategy FIRST_PLAYABLE_TOP_DOWN_KEEP_LAST = new ToroStrategy() {

      @Override public String getDescription() {
        return "Scan top down of candidates, chose the first playable Video. But if current player "
            + "is still playable, but not on the top, we keep using it";
      }

      @Override public ToroPlayer getPlayer(List<ToroPlayer> candidates) {
        return candidates.get(0);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, @Nullable Rect parentRect,
          @NonNull Rect childRect) {
        return true;
      }

      @Override public boolean requireCompletelyVisible() {
        return false;
      }
    };
  }

  static ToroItemViewHelper RECYCLER_VIEW_HELPER = new ToroItemViewHelper() {
    @Override public void onAttachedToParent(ToroPlayer player, View itemView, ViewParent parent) {
      for (Map.Entry<RecyclerView, ToroScrollListener> entry : sInstance.mMm.entrySet()) {
        RecyclerView key = entry.getKey();
        if (key == parent) {
          ToroScrollListener value = entry.getValue();
          if (value != null && value.getManager().getPlayer() == null) {
            value.getManager().setPlayer(player);
            value.getManager().restoreVideoState(player, player.getVideoId());
            // Check playing state
            Rect containerRect = new Rect();
            Rect parentRect = null;
            itemView.getLocalVisibleRect(containerRect);
            if (parent != null && parent instanceof View) {
              parentRect = new Rect();
              ((View) parent).getLocalVisibleRect(parentRect);
            }

            if (player.wantsToPlay(parentRect, containerRect)) {
              value.getManager().startVideo(player);
            }
          }
        }
      }
    }

    @Override
    public void onDetachedFromParent(ToroPlayer player, View itemView, ViewParent parent) {
      for (Map.Entry<RecyclerView, ToroScrollListener> entry : sInstance.mMm.entrySet()) {
        RecyclerView key = entry.getKey();
        if (key == parent) {
          ToroScrollListener value = entry.getValue();
          // Manually save Video state
          if (value != null && player.equals(value.getManager().getPlayer())) {
            value.getManager()
                .saveVideoState(player.getVideoId(), player.getCurrentPosition(),
                    player.getDuration());
          }
        }
      }
    }

    @Override public boolean onItemLongClick(ToroPlayer player, View itemView, ViewParent parent) {
      RecyclerView recyclerView = null;
      ToroScrollListener listener = null;
      for (Map.Entry<RecyclerView, ToroScrollListener> entry : sInstance.mMm.entrySet()) {
        recyclerView = entry.getKey();
        if (recyclerView == parent) {
          listener = entry.getValue();
          break;
        }
      }

      // Important components are missing, return
      if (recyclerView == null || listener == null) {
        return false;
      }

      Rect containerRect = new Rect();
      Rect parentRect = new Rect();
      itemView.getLocalVisibleRect(containerRect);
      recyclerView.getLocalVisibleRect(parentRect);

      // Being pressed player is not be able to play, return
      if (!player.wantsToPlay(parentRect, containerRect) || !getStrategy().allowsToPlay(player,
          parentRect, containerRect)) {
        return false;
      }

      ToroManager manager = listener.getManager();
      ToroPlayer currentPlayer = manager.getPlayer();

      if (!player.equals(currentPlayer)) {
        // All condition to switch players has passed, process the switching
        // Manually save Video state
        // Not the current player, and new player wants to play, so switch players
        if (currentPlayer != null) {
          manager.saveVideoState(currentPlayer.getVideoId(), currentPlayer.getCurrentPosition(),
              currentPlayer.getDuration());
          if (currentPlayer.isPlaying()) {
            manager.pauseVideo(currentPlayer);
          }
        }

        // Trigger new player
        manager.setPlayer(player);
        manager.restoreVideoState(player, player.getVideoId());
        manager.startVideo(player);

        return true;
      }

      return false;
    }
  };
}
