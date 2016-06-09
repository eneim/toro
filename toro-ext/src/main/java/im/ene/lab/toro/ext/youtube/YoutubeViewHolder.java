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

package im.ene.lab.toro.ext.youtube;

import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import im.ene.lab.toro.ToroViewHolder;
import im.ene.lab.toro.ext.util.Util;
import im.ene.lab.toro.player.MediaSource;
import im.ene.lab.toro.player.PlaybackException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by eneim on 4/8/16.
 *
 * A library-level Abstract ViewHolder for Youtube
 */
public abstract class YoutubeViewHolder extends ToroViewHolder implements
    // 0. IMPORTANT: required for requesting Youtube API
    YouTubePlayer.OnInitializedListener,
    // 1. Normal playback state
    YouTubePlayer.PlayerStateChangeListener, YouTubePlayer.PlaybackEventListener,
    // 2. Optional: use Thumbnail classes
    YouTubeThumbnailLoader.OnThumbnailLoadedListener, YouTubeThumbnailView.OnInitializedListener {

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
  protected final YoutubeVideosAdapter mParent;

  /**
   * Id for {@link YouTubePlayerSupportFragment}, will be generated manually and must be set for
   * proper view
   */
  protected final int mFragmentId;

  private final YoutubeViewItemHelper mHelper;
  protected YouTubePlayerSupportFragment mPlayerFragment;
  @Nullable protected YouTubeThumbnailView mThumbnail;
  private long seekPosition = 0;
  private boolean isSeeking = false;
  private boolean isStarting = false;

  private EventLogger mLogger;

  public YoutubeViewHolder(final View itemView, YoutubeVideosAdapter parent) {
    super(itemView);
    this.mParent = parent;
    if (this.mParent.mFragmentManager == null) {
      throw new IllegalArgumentException(
          "This View requires a YoutubeListAdapter parent which holds a non-null FragmentManager");
    }
    this.mHelper = YoutubeViewItemHelper.getInstance();
    this.mFragmentId = Util.generateViewId();
  }

  @Nullable protected abstract YouTubeThumbnailView getThumbnailView();

  @Override public final boolean wantsToPlay() {
    return super.visibleAreaOffset() >= 0.99f;  // Actually Youtube wants 1.0f;
  }

  @Override public void onAttachedToParent() {
    super.onAttachedToParent();
    itemView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            if ((mThumbnail = getThumbnailView()) != null) {
              mThumbnail.initialize(Youtube.API_KEY, YoutubeViewHolder.this);
            } else {
              mHelper.onPrepared(YoutubeViewHolder.this, itemView, itemView.getParent(), null);
            }
          }
        });
  }

  @CallSuper @Override public void onViewHolderBound() {
    super.onViewHolderBound();
    mLogger = new EventLogger(getYoutubeVideoId(), getAdapterPosition());
    if (itemView.findViewById(mFragmentId) == null) {
      throw new RuntimeException("View with Id: " + mFragmentId + " must be setup");
    }

    if ((mPlayerFragment =
        (YouTubePlayerSupportFragment) mParent.mFragmentManager.findFragmentById(mFragmentId))
        == null) {
      mPlayerFragment = YouTubePlayerSupportFragment.newInstance();
      // Add youtube player fragment to this ViewHolder
      mParent.mFragmentManager.beginTransaction().replace(mFragmentId, mPlayerFragment).commit();
    }
  }

  @Override public void onActivityActive() {
    super.onActivityActive();
  }

  @Override public void onActivityInactive() {
    super.onActivityInactive();
  }

  @CallSuper @Override public void start() {
    Log.e("Logger", "start: ");
    isStarting = true;
    // Release current youtube player first. Prevent resource conflict
    if (mParent.mYoutubePlayer != null) {
      mParent.mYoutubePlayer.release();
    }
    // Re-setup the Player. This is annoying though.
    if (mPlayerFragment != null) {
      mPlayerFragment.initialize(Youtube.API_KEY, this);
    }
  }

  @CallSuper @Override public void pause() {
    isStarting = false;
    try {
      mParent.mYoutubePlayer.pause();
    } catch (NullPointerException | IllegalStateException er) {
      er.printStackTrace();
    }
  }

  @Override public void stop() {
    isStarting = false;
    try {
      mParent.mYoutubePlayer.pause();
      mParent.mYoutubePlayer.release();
    } catch (NullPointerException | IllegalStateException er) {
      er.printStackTrace();
    }
  }

  @Override public final long getDuration() {
    try {
      return mParent.mYoutubePlayer != null ? mParent.mYoutubePlayer.getDurationMillis() : -1;
    } catch (IllegalStateException er) {
      er.printStackTrace();
      return -1;
    }
  }

  @Override public final long getCurrentPosition() {
    try {
      return mParent.mYoutubePlayer != null ? mParent.mYoutubePlayer.getCurrentTimeMillis() : 0;
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
      return isStarting || (mParent.mYoutubePlayer != null && mParent.mYoutubePlayer.isPlaying());
    } catch (IllegalStateException er) {
      er.printStackTrace();
      return isStarting;
    }
  }

  // Youtube video id for this view. This method should be used dynamically
  protected abstract String getYoutubeVideoId();

  @CallSuper @Override public void onLoading() {
    mHelper.onLoading();
    mLogger.onLoading();
  }

  @CallSuper @Override public void onLoaded(String videoId) {
    mHelper.onLoaded(videoId);
    mLogger.onLoaded(videoId);
  }

  @CallSuper @Override public void onAdStarted() {
    mHelper.onAdStarted();
    mLogger.onAdStarted();
  }

  @CallSuper @Override public final void onVideoStarted() {
    mHelper.onVideoStarted(this);
    mLogger.onVideoStarted();
  }

  @CallSuper @Override public final void onVideoEnded() {
    isStarting = false;
    mHelper.onCompletion(this, null);
    mHelper.onVideoEnded(this);
    mLogger.onVideoEnded();
  }

  @CallSuper @Override public void onError(YouTubePlayer.ErrorReason errorReason) {
    PlaybackException error =
        errorReason != null ? new PlaybackException(errorReason.name(), 0, 0) : null;
    mParent.onError(errorReason);
    mHelper.onError(this, null, error);
    mHelper.onYoutubeError(this, errorReason);
    mLogger.onError(errorReason);
  }

  @CallSuper @Override public final void onPlaying() {
    mParent.onPlaying();
    mHelper.onPlaying();
    mLogger.onPlaying();
  }

  // Paused by Native's button. Should not dispatch any custom behavior.
  @CallSuper @Override public final void onPaused() {
    mParent.onPaused();
    mHelper.onPaused();
    mLogger.onPaused();
  }

  // The method is called once RIGHT BEFORE A VIDEO GOT LOADED (by Youtube Player API)
  // And once again after the Player completes playback.
  // !IMPORTANT Ignore this.
  @Deprecated @CallSuper @Override public final void onStopped() {
    mLogger.onStopped();
  }

  @CallSuper @Override public void onBuffering(boolean isBuffering) {
    mHelper.onBuffering(isBuffering);
    mLogger.onBuffering(isBuffering);
  }

  // Called internal. Youtube's Playback event is internally called by API, so User should not
  // dispatch them
  @CallSuper @Override public final void onSeekTo(int position) {
    seekPosition = position;
    isSeeking = true;
    mLogger.onSeekTo(position);
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
    mLogger.onInitializationSuccess(provider, youTubePlayer, isRecover);
    // Switch youtube player
    mParent.mYoutubePlayer = youTubePlayer;
    mHelper.onYoutubePlayerChanged(youTubePlayer);
    mParent.mYoutubePlayer.setPlayerStateChangeListener(YoutubeViewHolder.this);
    mParent.mYoutubePlayer.setPlaybackEventListener(YoutubeViewHolder.this);
    // Force player style
    mParent.mYoutubePlayer.setPlayerStyle(
        getPlayerStyle() == CHROME_LESS ? YouTubePlayer.PlayerStyle.CHROMELESS
            : YouTubePlayer.PlayerStyle.MINIMAL);
    if (!isRecover) {
      if (isSeeking) {
        isSeeking = false;
        mParent.mYoutubePlayer.loadVideo(getYoutubeVideoId(), (int) seekPosition);
      } else {
        mParent.mYoutubePlayer.loadVideo(getYoutubeVideoId());
      }
      seekPosition = 0;
    }
  }

  @Override public void onInitializationFailure(YouTubePlayer.Provider provider,
      YouTubeInitializationResult youTubeInitializationResult) {
    // TODO Handle error
    mLogger.onInitializationFailure(provider, youTubeInitializationResult);
  }

  @Override public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView,
      YouTubeInitializationResult youTubeInitializationResult) {

  }

  @Override public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView,
      YouTubeThumbnailLoader youTubeThumbnailLoader) {
    youTubeThumbnailLoader.setOnThumbnailLoadedListener(this);
    youTubeThumbnailLoader.setVideo(getYoutubeVideoId());
  }

  @Override public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView,
      YouTubeThumbnailLoader.ErrorReason errorReason) {

  }

  @Override public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
    mHelper.onPrepared(this, itemView, itemView.getParent(), null);
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {

  }

  @Override public void setMediaUri(Uri uri) {

  }

  @Override public void setMediaSource(@NonNull MediaSource source) {

  }

  @IntDef({
      CHROME_LESS, MINIMUM
  }) @Retention(RetentionPolicy.SOURCE) public @interface PlayerStyle {
  }
}
