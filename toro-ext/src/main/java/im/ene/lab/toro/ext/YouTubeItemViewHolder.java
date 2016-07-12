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

package im.ene.lab.toro.ext;

import android.annotation.SuppressLint;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import im.ene.lab.toro.ext.util.ViewUtil;
import im.ene.lab.toro.media.PlaybackException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static im.ene.lab.toro.ext.ToroExt.sInstance;

/**
 * Created by eneim on 4/8/16.
 *
 * A library-level Abstract ViewHolder for Youtube
 */
/* public */ abstract class YouTubeItemViewHolder extends BasePlayerViewHolder implements
    // 0. IMPORTANT: required for requesting Youtube API
    YouTubePlayer.OnInitializedListener,
    // 1. Normal playback state
    YouTubePlayer.PlayerStateChangeListener, YouTubePlayer.PlaybackEventListener,
    // 2. Optional: use Thumbnail classes
    YouTubeThumbnailLoader.OnThumbnailLoadedListener, YouTubeThumbnailView.OnInitializedListener {

  private static final String TAG = "ViewHolder:YouTube";

  /**
   * This setup will offer {@link YouTubePlayer.PlayerStyle#CHROMELESS} to youtube player
   */
  protected static final int CHROME_LESS = 0b01;

  /**
   * This setup will offer {@link YouTubePlayer.PlayerStyle#MINIMAL} to youtube player
   */
  protected static final int MINIMUM = 0b10;

  /**
   * Parent Adapter which holds some important controllers
   */
  protected final YouTubeVideosAdapter parent;

  /**
   * Id for {@link YouTubePlayerSupportFragment}, will be generated manually and must be set for
   * proper view
   */
  protected final int fragmentId;

  private final YouTubePlayerViewHelper helper;
  protected YouTubePlayerSupportFragment playerFragment;
  @Nullable protected YouTubeThumbnailView thumbnail;
  private long seekPosition = 0;
  private boolean isSeeking = false;
  private boolean isStarting = false;
  private boolean playerReleased = false;
  private boolean wantsToPlay;

  private YouTubeEventLogger logger;

  public YouTubeItemViewHolder(final View itemView, YouTubeVideosAdapter parent) {
    super(itemView);
    this.parent = parent;
    if (this.parent.fragmentManager == null) {
      throw new IllegalArgumentException(
          "This View requires a YoutubeListAdapter parent which holds a non-null FragmentManager");
    }
    this.helper = new YouTubePlayerViewHelper(this, itemView);
    this.fragmentId = ViewUtil.generateViewId();
    this.thumbnail = getThumbnailView();
  }

  @Nullable protected abstract YouTubeThumbnailView getThumbnailView();

  @Override public final boolean wantsToPlay() {
    return super.visibleAreaOffset() >= 0.99f && wantsToPlay;  // Actually Youtube wants 1.0f;
  }

  // With YouTube, we need to wait for its visibility.
  @SuppressLint("MissingSuperCall") @Override public void onAttachedToParent() {
    wantsToPlay = (thumbnail == null);
    itemView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            ViewUtil.removeOnGlobalLayoutListener(itemView.getViewTreeObserver(), this);
            YouTubeItemViewHolder.super.onAttachedToParent();
            Log.i(TAG, "Attached:Wants to play: " + wantsToPlay);
          }
        });
  }

  @Override public void onDetachedFromParent() {
    super.onDetachedFromParent();
  }

  @CallSuper @Override public void onViewHolderBound() {
    super.onViewHolderBound();
    logger = new YouTubeEventLogger(getYoutubeVideoId(), getAdapterPosition());
    if (itemView.findViewById(fragmentId) == null) {
      throw new RuntimeException("View with Id: " + fragmentId + " must be setup");
    }

    if ((playerFragment =
        (YouTubePlayerSupportFragment) parent.fragmentManager.findFragmentById(fragmentId))
        == null) {
      playerFragment = YouTubePlayerSupportFragment.newInstance();
      // Add youtube player fragment to this ViewHolder
      parent.fragmentManager.beginTransaction().replace(fragmentId, playerFragment).commit();
    }
  }

  @Override public void preparePlayer(boolean playWhenReady) {
    if (thumbnail != null) {
      thumbnail.setVisibility(View.VISIBLE);
      thumbnail.initialize(sInstance.youTube.apiKey /* nullable */, YouTubeItemViewHolder.this);
    } else {
      helper.onPrepared(itemView, itemView.getParent(), null);
    }
  }

  @Override public void releasePlayer() {
    // Release current youtube player first. Prevent resource conflict
    if (parent.youTubePlayer != null) {
      parent.youTubePlayer.release();
      playerReleased = true;
    }
  }

  @CallSuper @Override public void start() {
    Log.e("Logger", "start: ");
    isStarting = true;
    if (thumbnail != null) {
      thumbnail.setVisibility(View.GONE);
    }

    boolean requireInitialization = true;
    if (parent.lastPlayerPosition == getAdapterPosition()) {
      if (parent.youTubePlayer != null) {
        requireInitialization = false;
      }
    }

    if (!requireInitialization && !playerReleased) {
      parent.youTubePlayer.play();
    } else {
      if (parent.youTubePlayer != null) {
        parent.youTubePlayer.release();
        playerReleased = true;
      }

      // Re-setup the Player. This is annoying though.
      if (playerFragment != null) {
        playerFragment.initialize(sInstance.youTube.apiKey, this);
      }
    }

    parent.lastPlayerPosition = getAdapterPosition();
  }

  @CallSuper @Override public void pause() {
    isStarting = false;
    if (thumbnail != null) {
      thumbnail.setVisibility(View.VISIBLE);
    }
    try {
      parent.youTubePlayer.pause();
    } catch (NullPointerException | IllegalStateException er) {
      er.printStackTrace();
    }
  }

  @Override public void stop() {
    isStarting = false;
    if (thumbnail != null) {
      thumbnail.setVisibility(View.VISIBLE);
    }
    try {
      parent.youTubePlayer.pause();
      parent.youTubePlayer.release();
      playerReleased = true;
    } catch (NullPointerException | IllegalStateException er) {
      er.printStackTrace();
    }
  }

  @Override public final long getDuration() {
    try {
      return parent.youTubePlayer != null ? parent.youTubePlayer.getDurationMillis() : -1;
    } catch (IllegalStateException er) {
      er.printStackTrace();
      return -1;
    }
  }

  @Override public final long getCurrentPosition() {
    try {
      return parent.youTubePlayer != null ? parent.youTubePlayer.getCurrentTimeMillis() : 0;
    } catch (IllegalStateException er) {
      er.printStackTrace();
      return 0;
    }
  }

  @CallSuper @Override public final void seekTo(long pos) {
    isSeeking = true;
    seekPosition = pos;
  }

  @Override public final boolean isPlaying() {
    try {
      // is loading the video or playing it
      return isStarting || (parent.youTubePlayer != null && parent.youTubePlayer.isPlaying());
    } catch (IllegalStateException er) {
      er.printStackTrace();
      return isStarting;
    }
  }

  // Youtube video id for this view. This method should be used dynamically
  protected abstract String getYoutubeVideoId();

  @CallSuper @Override public void onLoading() {
    helper.onLoading();
    logger.onLoading();
  }

  @CallSuper @Override public void onLoaded(String videoId) {
    helper.onLoaded(videoId);
    logger.onLoaded(videoId);
  }

  @CallSuper @Override public void onAdStarted() {
    helper.onAdStarted();
    logger.onAdStarted();
  }

  @CallSuper @Override public final void onVideoStarted() {
    helper.onVideoStarted(this);
    logger.onVideoStarted();
  }

  @CallSuper @Override public final void onVideoEnded() {
    isStarting = false;
    helper.onCompletion(null);
    helper.onVideoEnded(this);
    logger.onVideoEnded();
  }

  @CallSuper @Override public void onError(YouTubePlayer.ErrorReason errorReason) {
    PlaybackException error =
        errorReason != null ? new PlaybackException(errorReason.name(), 0, 0) : null;
    parent.onError(errorReason);
    helper.onPlayerError(null, error);
    helper.onYoutubeError(this, errorReason);
    logger.onError(errorReason);
  }

  @CallSuper @Override public final void onPlaying() {
    parent.onPlaying();
    helper.onPlaying();
    logger.onPlaying();
  }

  // Paused by Native's button. Should not dispatch any custom behavior.
  @CallSuper @Override public final void onPaused() {
    parent.onPaused();
    helper.onPaused();
    logger.onPaused();
  }

  // The method is called once RIGHT BEFORE A VIDEO GOT LOADED (by Youtube Player API)
  // And once again after the Player completes playback.
  // !IMPORTANT Ignore this.
  @Deprecated @CallSuper @Override public final void onStopped() {
    logger.onStopped();
  }

  @CallSuper @Override public void onBuffering(boolean isBuffering) {
    helper.onBuffering(isBuffering);
    logger.onBuffering(isBuffering);
  }

  // Called internal. Youtube's Playback event is internally called by API, so User should not
  // dispatch them
  @CallSuper @Override public final void onSeekTo(int position) {
    seekPosition = position;
    isSeeking = true;
    logger.onSeekTo(position);
  }

  /**
   * This library will force user to use either {@link YouTubePlayer.PlayerStyle#MINIMAL} or {@link
   * YouTubePlayer.PlayerStyle#CHROMELESS}. User should override this to provide her expected UI
   */
  @PlayerStyle protected int getPlayerStyle() {
    return MINIMUM;
  }

  @CallSuper @Override
  public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer,
      boolean isRecover) {
    logger.onInitializationSuccess(provider, youTubePlayer, isRecover);
    // Switch youtube player
    parent.youTubePlayer = youTubePlayer;
    helper.onYoutubePlayerChanged(youTubePlayer);
    parent.youTubePlayer.setPlayerStateChangeListener(YouTubeItemViewHolder.this);
    parent.youTubePlayer.setPlaybackEventListener(YouTubeItemViewHolder.this);
    // Force player style
    parent.youTubePlayer.setPlayerStyle(
        getPlayerStyle() == CHROME_LESS ? YouTubePlayer.PlayerStyle.CHROMELESS
            : YouTubePlayer.PlayerStyle.MINIMAL);
    if (!isRecover) {
      if (isSeeking) {
        isSeeking = false;
        parent.youTubePlayer.loadVideo(getYoutubeVideoId(), (int) seekPosition);
      } else {
        parent.youTubePlayer.loadVideo(getYoutubeVideoId());
      }
      seekPosition = 0;
    }
  }

  @Override public void onInitializationFailure(YouTubePlayer.Provider provider,
      YouTubeInitializationResult youTubeInitializationResult) {
    // TODO Handle error
    logger.onInitializationFailure(provider, youTubeInitializationResult);
  }

  @Override public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView,
      YouTubeInitializationResult youTubeInitializationResult) {
    Log.d(TAG, "onInitializationFailure() called with: "
        + "youTubeInitializationResult = ["
        + youTubeInitializationResult
        + "]");
  }

  @Override public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView,
      YouTubeThumbnailLoader youTubeThumbnailLoader) {
    youTubeThumbnailLoader.setOnThumbnailLoadedListener(this);
    youTubeThumbnailLoader.setVideo(getYoutubeVideoId());
    Log.d(TAG, "onInitializationSuccess() called with: "
        + "youTubeThumbnailLoader = ["
        + youTubeThumbnailLoader
        + "]");
  }

  @Override public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView,
      YouTubeThumbnailLoader.ErrorReason errorReason) {
    Log.d(TAG, "onThumbnailError() called with: " + "errorReason = [" + errorReason + "]");
  }

  @Override public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
    wantsToPlay = true;
    helper.onPrepared(itemView, itemView.getParent(), null);
    Log.i(TAG, "Thumbnail:Wants to play: " + wantsToPlay);
  }

  @IntDef({
      CHROME_LESS, MINIMUM
  }) @Retention(RetentionPolicy.SOURCE) public @interface PlayerStyle {
  }
}
