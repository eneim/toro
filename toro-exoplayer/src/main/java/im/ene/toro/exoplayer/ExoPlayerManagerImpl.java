/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package im.ene.toro.exoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.util.Pools;
import android.util.Log;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.UUID;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * @author eneim (2018/01/05).
 */

final class ExoPlayerManagerImpl implements ExoPlayerManager {

  private static final String TAG = "Toro:ExoPlayer";
  private static final int MAX_POOL_SIZE = 8;

  @SuppressLint("StaticFieldLeak")  //
  static ExoPlayerManagerImpl singleton;

  static ExoPlayerManager with(Context context) {
    if (singleton == null) {
      synchronized (ExoPlayerManager.class) {
        if (singleton == null) singleton = new ExoPlayerManagerImpl(context);
      }
    }

    return singleton;
  }

  @SuppressWarnings("WeakerAccess") //
  @NonNull final CleanupThread cleanupThread;
  @NonNull final ReferenceQueue<Object> referenceQueue;
  @NonNull final ExoPlayerPool[] playerPools;

  @NonNull private final Context context; // Application context

  private ExoPlayerManagerImpl(@NonNull Context context) {
    this.context = context.getApplicationContext();
    this.playerPools = new ExoPlayerPool[6];

    this.referenceQueue = new ReferenceQueue<>();
    this.cleanupThread = new CleanupThread(referenceQueue, HANDLER);
    this.cleanupThread.start();
  }

  ExoPlayerPool getPool(int index) {
    if (playerPools[index] == null) playerPools[index] = new ExoPlayerPool(MAX_POOL_SIZE);
    return playerPools[index];
  }

  @NonNull @Override
  public Playback prepare(@NonNull Playback.Resource resource, @Nullable PlaybackInfo playbackInfo,
      DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
      @ExtensionRendererMode int extensionMode, EventListener listener) {
    int idxShift = drmSessionManager == null ? 0 : 3;
    int poolIndex = idxShift + extensionMode;
    if (poolIndex >= playerPools.length) {
      throw new IllegalStateException("Bad extension mode: " + extensionMode);
    }
    SimpleExoPlayer player = getPool(poolIndex).acquirePlayer();
    Log.i(TAG, "prepare: " + player);
    if (player == null) {
      BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
      TrackSelection.Factory adaptiveTrackSelectionFactory =
          new AdaptiveTrackSelection.Factory(bandwidthMeter);
      DefaultTrackSelector trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
      RenderersFactory renderersFactory =
          new DefaultRenderersFactory(context, drmSessionManager, extensionMode);
      player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
      if (listener != null) player.addListener(listener);
    }

    if (player != resource.playerView.getPlayer()) {
      resource.playerView.setPlayer(player);
    }

    boolean haveResumePosition =
        playbackInfo != null && playbackInfo.getResumeWindow() != C.INDEX_UNSET;
    if (haveResumePosition) {
      player.seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
    }

    player.prepare(resource.mediaSource, !haveResumePosition, false);
    return new PlaybackImpl(poolIndex, player, resource.playerView, listener);
  }

  void destroy() {
    this.cleanupThread.shutdown();
  }

  //// Additional implementations

  @RequiresApi(18)  //
  private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
      String licenseUrl, String[] keyRequestPropertiesArray, boolean multiSession, Handler handler)
      throws UnsupportedDrmException {
    HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
        new DefaultHttpDataSourceFactory(Util.getUserAgent(context, ToroUtil.LIB_NAME)));
    if (keyRequestPropertiesArray != null) {
      for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
        drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
            keyRequestPropertiesArray[i + 1]);
      }
    }
    return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback,
        null, handler, null, multiSession);
  }

  private static UUID getDrmUuid(String drmScheme) throws UnsupportedDrmException {
    switch (Util.toLowerInvariant(drmScheme)) {
      case "widevine":
        return C.WIDEVINE_UUID;
      case "playready":
        return C.PLAYREADY_UUID;
      case "clearkey":
        return C.CLEARKEY_UUID;
      default:
        try {
          return UUID.fromString(drmScheme);
        } catch (RuntimeException e) {
          throw new UnsupportedDrmException(UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME);
        }
    }
  }

  static final class ExoPlayerPool extends Pools.SimplePool<WeakReference<SimpleExoPlayer>> {

    /**
     * Creates a new instance.
     *
     * @param maxPoolSize The max pool size.
     * @throws IllegalArgumentException If the max pool size is less than zero.
     */
    ExoPlayerPool(int maxPoolSize) {
      super(maxPoolSize);
    }

    @Nullable SimpleExoPlayer acquirePlayer() {
      WeakReference<SimpleExoPlayer> player = super.acquire();
      return player != null ? player.get() : null;
    }

    boolean releasePlayer(SimpleExoPlayer player) {
      return super.release(new RequestWeakReference<>(player, player, singleton.referenceQueue));
    }
  }

  static final class PlaybackImpl implements Playback {

    final SimpleExoPlayer player;
    final SimpleExoPlayerView playerView;
    final EventListener listener;
    final PlaybackInfo playbackInfo = new PlaybackInfo();
    final int poolIndex;

    PlaybackImpl(int poolIndex, SimpleExoPlayer player, SimpleExoPlayerView playerView,
        EventListener listener) {
      this.poolIndex = poolIndex;
      this.player = player;
      this.playerView = playerView;
      this.listener = listener;
    }

    @Override public void play() {
      this.player.setPlayWhenReady(true);
    }

    @Override public void pause() {
      this.player.setPlayWhenReady(false);
    }

    @Override public boolean isPlaying() {
      return this.player.getPlayWhenReady();
    }

    @Override public float getVolume() {
      return this.player.getVolume();
    }

    @Override public void setVolume(float volume) {
      this.player.setVolume(volume);
    }

    @Override public PlaybackInfo getPlaybackInfo() {
      updateResumePosition();
      return this.playbackInfo;
    }

    @Override public void setPlaybackInfo(PlaybackInfo playbackInfo) {
      if (playbackInfo != null) {
        this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
        this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
      }

      if (player != null) {
        boolean haveResumePosition = this.playbackInfo.getResumeWindow() != C.INDEX_UNSET;
        if (haveResumePosition) {
          player.seekTo(this.playbackInfo.getResumeWindow(), this.playbackInfo.getResumePosition());
        }
      }
    }

    @Override public void release() {
      this.player.removeListener(listener);
      this.playerView.setPlayer(null);
      singleton.getPool(poolIndex).releasePlayer(this.player);
    }

    void updateResumePosition() {
      if (player.getPlaybackState() != 1) {
        playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
        playbackInfo.setResumePosition(Math.max(0, player.getContentPosition()));
      }
    }

    void clearResumePosition() {
      this.playbackInfo.reset();
    }
  }

  /**
   * When the target of an action is weakly reachable but the request hasn't been canceled, it
   * gets added to the reference queue. This thread empties the reference queue and cancels the
   * request.
   */
  static final int REQUEST_GCED = 3;
  static final String THREAD_PREFIX = "ToroExo-";
  static final int THREAD_LEAK_CLEANING_MS = 1000;

  @SuppressWarnings("WeakerAccess") //
  static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        case REQUEST_GCED: {
          Log.w(TAG, "handleMessage: GCED");
          SimpleExoPlayer player = (SimpleExoPlayer) msg.obj;
          if (player != null) player.release();
          break;
        }
        default:
          throw new AssertionError("Unknown handler message received: " + msg.what);
      }
    }
  };

  private static class RequestWeakReference<M> extends WeakReference<M> {
    final SimpleExoPlayer player;

    RequestWeakReference(SimpleExoPlayer player, M referent, ReferenceQueue<? super M> q) {
      super(referent, q);
      this.player = player;
    }
  }

  private static class CleanupThread extends Thread {
    private final ReferenceQueue<Object> referenceQueue;
    private final Handler handler;

    CleanupThread(ReferenceQueue<Object> referenceQueue, Handler handler) {
      this.referenceQueue = referenceQueue;
      this.handler = handler;
      setDaemon(true);
      setName(THREAD_PREFIX + "refQueue");
    }

    @Override public void run() {
      Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
      while (true) {
        try {
          // Prior to Android 5.0, even when there is no local variable, the result from
          // remove() & obtainMessage() is kept as a stack local variable.
          // We're forcing this reference to be cleared and replaced by looping every second
          // when there is nothing to do.
          // This behavior has been tested and reproduced with heap dumps.
          RequestWeakReference<?> remove =
              (RequestWeakReference<?>) referenceQueue.remove(THREAD_LEAK_CLEANING_MS);
          Message message = handler.obtainMessage();
          if (remove != null) {
            message.what = REQUEST_GCED;
            message.obj = remove.player;
            handler.sendMessage(message);
          } else {
            message.recycle();
          }
        } catch (InterruptedException e) {
          break;
        } catch (final Exception e) {
          handler.post(new Runnable() {
            @Override public void run() {
              throw new RuntimeException(e);
            }
          });
          break;
        }
      }
    }

    void shutdown() {
      interrupt();
    }
  }
}
