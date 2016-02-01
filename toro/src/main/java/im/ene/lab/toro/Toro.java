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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewParent;
import im.ene.lab.toro.widget.ToroListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by eneim on 1/31/16.
 * <p/>
 * Control Application's lifecycle to properly handling callbacks, prevent Memory leak and
 * unexpected behavior;
 *
 * @<code> </code>
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public final class Toro implements Application.ActivityLifecycleCallbacks {

  private static final Object LOCK = new Object();

  // Singleton
  static Toro sInstance = new Toro();

  ToroPolicy mPolicy = Policies.FIRST_VISIBLE;  // Default policy
  // It requires client to detach Activity/unregister View to prevent Memory leak
  WeakHashMap<View, ToroScrollHelper> mEntries = new WeakHashMap<>();
  ArrayList<ToroManager> mManagers = new ArrayList<>();

  public static void attach(@NonNull Activity activity) {
    init(activity.getApplication());
  }

  public static void init(Application application) {
    if (application != null) {
      application.registerActivityLifecycleCallbacks(sInstance);
    }
  }

  public static void detach(Activity activity) {
    Application application = activity.getApplication();
    if (application != null) {
      application.unregisterActivityLifecycleCallbacks(sInstance);
    }
  }

  public static void policy(@NonNull ToroPolicy policy) {
    sInstance.mPolicy = policy;
  }

  public static void register(View view) {
    // Remove old images
    if (sInstance.mEntries.containsKey(view)) {
      synchronized (LOCK) {
        sInstance.mEntries.remove(view);
      }
    }

    if (view instanceof RecyclerView) {
      synchronized (LOCK) {
        // 1. retrieve current TotoManager instance
        ToroManager manager = null;
        RecyclerView.Adapter adapter = ((RecyclerView) view).getAdapter();
        if (adapter instanceof ToroManager) {
          manager = (ToroManager) adapter;
        }

        // Fallback to default Manager
        if (manager == null) {
          manager = new ToroManagerImpl();
        }

        RecyclerView.LayoutManager layoutManager = ((RecyclerView) view).getLayoutManager();
        RecyclerViewScrollListener listener;
        if (layoutManager instanceof LinearLayoutManager) {
          listener = new RecyclerViewLinearScrollListener(manager);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
          listener = new RecyclerViewStaggeredGridScrollListener(manager);
        } else {
          throw new IllegalArgumentException("Unexpected layout manager: " + layoutManager);
        }

        ((RecyclerView) view).addOnScrollListener(listener);
        // Cache
        sInstance.mManagers.add(manager);
        sInstance.mEntries.put(view, listener);
      }
    } else if (view instanceof ToroListView) {
      // TODO implement for ListView
      synchronized (LOCK) {
        ListViewScrollListener scrollListener = new ListViewScrollListener(new ToroManagerImpl());
        ((ToroListView) view).addOnScrollListener(scrollListener);
        sInstance.mEntries.put(view, scrollListener);
      }
    }
  }

  public static void unregister(View view) {
    synchronized (LOCK) {
      // Obtain listener which will be removed
      ToroScrollHelper object = sInstance.mEntries.remove(view);
      // Process related View
      if (object != null) {
        // Remove from Manager list
        sInstance.mManagers.remove(object.getManager());
        // there is a set of <View, Listener> is removed
        if (view instanceof RecyclerView && object instanceof RecyclerViewScrollListener) {
          ((RecyclerView) view).removeOnScrollListener((RecyclerView.OnScrollListener) object);
        } else if (view instanceof ToroListView && object instanceof ListViewScrollListener) {
          ((ToroListView) view).removeOnScrollListener((ListViewScrollListener) object);
        }
      }
    }
  }

  static void onAttachedToParent(ToroPlayer player, View container, ViewParent parent) {
    for (View view : sInstance.mEntries.keySet()) {
      if (view == parent) {
        ToroScrollHelper scrollHelper = sInstance.mEntries.get(view);
        if (scrollHelper != null &&
            sInstance.mManagers.contains(scrollHelper.getManager()) &&
            scrollHelper.getManager().getPlayer() == null) {
          scrollHelper.getManager().setPlayer(player);
          scrollHelper.getManager().restoreVideoState(player, player.getVideoId());
          // Check playing state
          Rect containerRect = new Rect();
          Rect parentRect = null;
          container.getLocalVisibleRect(containerRect);
          if (parent != null && parent instanceof View) {
            parentRect = new Rect();
            ((View) parent).getLocalVisibleRect(parentRect);
          }

          if (player.wantsToPlay(parentRect, containerRect)) {
            scrollHelper.getManager().startVideo(player);
          }
        }
      }
    }
  }

  static void onDetachedFromParent(ToroPlayer player, View container, ViewParent parent) {
    for (View view : sInstance.mEntries.keySet()) {
      // Find ToroManager for current ViewParent
      if (view == parent) {
        ToroScrollHelper scrollHelper = sInstance.mEntries.get(view);
        // Manually save Video state
        if (scrollHelper != null &&
            sInstance.mManagers.contains(scrollHelper.getManager()) &&
            player.equals(scrollHelper.getManager().getPlayer())) {
          scrollHelper.getManager()
              .saveVideoState(player.getVideoId(), player.getCurrentPosition(),
                  player.getDuration());
        }
      }
    }
  }

  static void onCompletion(ToroPlayer player, MediaPlayer mediaPlayer) {
    for (ToroManager manager : sInstance.mManagers) {
      if (player.equals(manager.getPlayer())) {
        manager.saveVideoState(player.getVideoId(), 0, player.getDuration());
        break;
      }
    }
  }

  static void onPrepared(ToroPlayer player, View container, ViewParent parent,
      MediaPlayer mediaPlayer) {
    for (ToroManager manager : sInstance.mManagers) {
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

  @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

  }

  @Override public void onActivityStarted(Activity activity) {

  }

  @Override public void onActivityResumed(Activity activity) {
    for (ToroManager manager : mManagers) {
      if (manager.getPlayer() != null) {
        manager.getPlayer().onActivityResumed();
      }
    }
  }

  @Override public void onActivityPaused(Activity activity) {
    for (ToroManager manager : mManagers) {
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

  static final class Policies {

    static ToroPolicy FIRST_VISIBLE = new ToroPolicy() {
      @Override public ToroPlayer getPlayer(List<ToroPlayer> candidates) {
        if (candidates == null || candidates.size() < 1) {
          return null;
        }

        Collections.sort(candidates, new Comparator<ToroPlayer>() {
          @Override public int compare(ToroPlayer lhs, ToroPlayer rhs) {
            return lhs.compare(rhs);
          }
        });

        return candidates.get(0);
      }

      @Override public boolean requireCompletelyVisible() {
        return false;
      }
    };

    static ToroPolicy LAST_VISIBLE = new ToroPolicy() {
      @Override public ToroPlayer getPlayer(List<ToroPlayer> candidates) {
        if (candidates == null || candidates.size() < 1) {
          return null;
        }

        Collections.sort(candidates, new Comparator<ToroPlayer>() {
          @Override public int compare(ToroPlayer lhs, ToroPlayer rhs) {
            return rhs.compare(lhs);
          }
        });

        return candidates.get(0);
      }

      @Override public boolean requireCompletelyVisible() {
        return false;
      }
    };

    // Each of this policy's candidate must hold a completely visible Video playable view.
    // Note that it is different to RecyclerView's completely Visible child
    static ToroPolicy FIRST_COMPLETELY_VISIBLE = new ToroPolicy() {
      @Override public ToroPlayer getPlayer(List<ToroPlayer> candidates) {
        return FIRST_VISIBLE.getPlayer(candidates);
      }

      @Override public boolean requireCompletelyVisible() {
        return true;
      }
    };

    // Each of this policy's candidate must hold a completely visible Video playable view.
    // Note that it is different to RecyclerView's completely Visible child
    static ToroPolicy LAST_COMPLETELY_VISIBLE = new ToroPolicy() {
      @Override public ToroPlayer getPlayer(List<ToroPlayer> candidates) {
        return LAST_VISIBLE.getPlayer(candidates);
      }

      @Override public boolean requireCompletelyVisible() {
        return true;
      }
    };
  }
}
