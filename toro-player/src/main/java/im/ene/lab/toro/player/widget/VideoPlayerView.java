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

package im.ene.lab.toro.player.widget;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import im.ene.lab.toro.player.MediaSource;
import im.ene.lab.toro.player.PlaybackException;
import im.ene.lab.toro.player.PlaybackInfo;
import im.ene.lab.toro.player.TrMediaPlayer;
import im.ene.lab.toro.player.internal.ExoMediaPlayer;
import im.ene.lab.toro.player.internal.RendererBuilderFactory;
import im.ene.lab.toro.player.listener.OnBufferingUpdateListener;
import im.ene.lab.toro.player.listener.OnCompletionListener;
import im.ene.lab.toro.player.listener.OnErrorListener;
import im.ene.lab.toro.player.listener.OnInfoListener;
import im.ene.lab.toro.player.listener.OnPreparedListener;
import im.ene.lab.toro.player.listener.OnVideoSizeChangedListener;
import java.io.IOException;
import java.util.Map;

/**
 * Displays a video file.  The TextureVideoView class
 * can load images from various sources (such as resources or content
 * providers), takes care of computing its measurement from the video so that
 * it can be used in any layout manager, and provides various display options
 * such as scaling and tinting.<p>
 *
 * <em>Note: VideoView does not retain its full state when going into the
 * background.</em>  In particular, it does not restore the current play state,
 * play position or selected tracks.  Applications should
 * save and restore these on their own in
 * {@link android.app.Activity#onSaveInstanceState} and
 * {@link android.app.Activity#onRestoreInstanceState}.<p>
 * Also note that the audio session id (from {@link #getAudioSessionId}) may
 * change from its previously returned value when the VideoView is restored.<p>
 *
 * This code is based on the official Android sources for 6.0.1_r10 with the following differences:
 * <ol>
 * <li>extends {@link android.view.TextureView} instead of a {@link android.view.SurfaceView}
 * allowing proper view animations</li>
 * <li>removes code that uses hidden APIs and thus is not available (e.g. subtitle support)</li>
 * </ol>
 */
@Deprecated
public class VideoPlayerView extends TextureView implements TrMediaPlayer.IMediaPlayer {

  public interface OnReleasedListener {

    /**
     * Called right before {@link #release(boolean)} )} get called with true
     * parameter
     *
     * @param video current Video Uri
     * @param position latest playback position right before releasing
     * @param duration latest playback video's duration right before releasing
     */
    void onReleased(@Nullable Uri video, long position, long duration);
  }

  private String TAG = "TextureVideoView";
  // settable by the client
  private Uri mUri;
  private Map<String, String> mHeaders;

  // all possible internal states
  private static final int STATE_ERROR = -1;
  private static final int STATE_IDLE = 0;
  private static final int STATE_PREPARING = 1;
  private static final int STATE_PREPARED = 2;
  private static final int STATE_PLAYING = 3;
  private static final int STATE_PAUSED = 4;
  private static final int STATE_PLAYBACK_COMPLETED = 5;

  // mCurrentState is a TextureVideoView object's current state.
  // mTargetState is the state that a method caller intends to reach.
  // For instance, regardless the TextureVideoView object's current state,
  // calling pause() intends to bring the object to a target state
  // of STATE_PAUSED.
  private int mCurrentState = STATE_IDLE;
  private int mTargetState = STATE_IDLE;

  // All the stuff we need for playing and showing a video
  private Surface mSurface = null;
  private TrMediaPlayer mMediaPlayer = null;
  private int mAudioSession;
  private int mVideoWidth;
  private int mVideoHeight;

  private boolean mBackgroundAudioEnabled = false;

  private TrMediaPlayer.Controller mController;

  private OnCompletionListener mOnCompletionListener;
  private OnPreparedListener mOnPreparedListener;
  private OnErrorListener mOnErrorListener;
  private OnInfoListener mOnInfoListener;
  private OnReleasedListener mOnReleaseListener;

  private int mCurrentBufferPercentage;
  private long mSeekWhenPrepared;  // recording the seek position while preparing

  public VideoPlayerView(Context context) {
    this(context, null);
  }

  public VideoPlayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public VideoPlayerView(Context context, AttributeSet attrs, int defStyle) {
    this(context, attrs, defStyle, 0);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initVideoView();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
    //        + MeasureSpec.toString(heightMeasureSpec) + ")");

    int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
    int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
    if (mVideoWidth > 0 && mVideoHeight > 0) {

      int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
      int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
      int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
      int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

      if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
        // the size is fixed
        width = widthSpecSize;
        height = heightSpecSize;

        // for compatibility, we adjust size based on aspect ratio
        if (mVideoWidth * height < width * mVideoHeight) {
          //Log.i("@@@", "image too wide, correcting");
          width = height * mVideoWidth / mVideoHeight;
        } else if (mVideoWidth * height > width * mVideoHeight) {
          //Log.i("@@@", "image too tall, correcting");
          height = width * mVideoHeight / mVideoWidth;
        }
      } else if (widthSpecMode == MeasureSpec.EXACTLY) {
        // only the width is fixed, adjust the height to match aspect ratio if possible
        width = widthSpecSize;
        height = width * mVideoHeight / mVideoWidth;
        if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
          // couldn't match aspect ratio within the constraints
          height = heightSpecSize;
        }
      } else if (heightSpecMode == MeasureSpec.EXACTLY) {
        // only the height is fixed, adjust the width to match aspect ratio if possible
        height = heightSpecSize;
        width = height * mVideoWidth / mVideoHeight;
        if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
          // couldn't match aspect ratio within the constraints
          width = widthSpecSize;
        }
      } else {
        // neither the width nor the height are fixed, try to use actual video size
        width = mVideoWidth;
        height = mVideoHeight;
        if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
          // too tall, decrease both width and height
          height = heightSpecSize;
          width = height * mVideoWidth / mVideoHeight;
        }
        if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
          // too wide, decrease both width and height
          width = widthSpecSize;
          height = width * mVideoHeight / mVideoWidth;
        }
      }
    } else {
      // no size yet, just adopt the given spec sizes
    }
    setMeasuredDimension(width, height);
  }

  @Override public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
    super.onInitializeAccessibilityEvent(event);
    event.setClassName(VideoPlayerView.class.getName());
  }

  @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(VideoPlayerView.class.getName());
  }

  public int resolveAdjustedSize(int desiredSize, int measureSpec) {
    return getDefaultSize(desiredSize, measureSpec);
  }

  private void initVideoView() {
    mVideoWidth = 0;
    mVideoHeight = 0;
    setSurfaceTextureListener(mSurfaceTextureListener);
    setFocusable(true);
    setFocusableInTouchMode(true);
    requestFocus();
    mCurrentState = STATE_IDLE;
    mTargetState = STATE_IDLE;
  }

  /**
   * Sets video path.
   *
   * @param path the path of the video.
   */
  public void setMediaPath(String path) {
    setMediaUri(Uri.parse(path));
  }

  /**
   * Sets video URI.
   *
   * @param uri the URI of the video.
   */
  public void setMediaUri(Uri uri) {
    setMediaUri(uri, null);
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    if (mMediaPlayer != null) {
      mMediaPlayer.setVolume(volume);
    }
  }

  /**
   * Sets video URI using specific headers.
   *
   * @param uri the URI of the video.
   * @param headers the headers for the URI request.
   * Note that the cross domain redirection is allowed by default, but that can be
   * changed with key/value pairs through the headers parameter with
   * "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
   * to disallow or allow cross domain redirection.
   */
  public void setMediaUri(Uri uri, Map<String, String> headers) {
    mUri = uri;
    mHeaders = headers;
    mSeekWhenPrepared = 0;
    openVideo();
    requestLayout();
    invalidate();
  }

  public void stopPlayback() {
    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
      mCurrentState = STATE_IDLE;
      mTargetState = STATE_IDLE;
      AudioManager am = (AudioManager) getContext().getApplicationContext()
          .getSystemService(Context.AUDIO_SERVICE);
      am.abandonAudioFocus(null);
    }
  }

  private void openVideo() {
    if (mUri == null || mSurface == null) {
      // not ready for playback just yet, will try again later
      return;
    }
    // we shouldn't clear the target state, because somebody might have
    // called start() previously
    release(false);

    if (!mBackgroundAudioEnabled) {
      AudioManager am = (AudioManager) getContext().getApplicationContext()
          .getSystemService(Context.AUDIO_SERVICE);
      am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    try {
      //ExoMediaPlayer.RendererBuilder builder =
      //    RendererBuilderFactory.createRendererBuilder(getContext(), mUri);
      //mMediaPlayer = TrMediaPlayer.Factory.createExoPlayer(builder);
      mMediaPlayer = TrMediaPlayer.Factory.createNativePlayer();

      if (mAudioSession != 0) {
        mMediaPlayer.setAudioSessionId(mAudioSession);
      } else {
        mAudioSession = mMediaPlayer.getAudioSessionId();
      }

      mMediaPlayer.setOnPreparedListener(mPreparedListener);
      mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
      mMediaPlayer.setOnCompletionListener(mCompletionListener);
      mMediaPlayer.setOnErrorListener(mErrorListener);
      mMediaPlayer.setOnInfoListener(mInfoListener);
      mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);

      mCurrentBufferPercentage = 0;
      mMediaPlayer.setDataSource(getContext().getApplicationContext(), mUri, mHeaders);
      mMediaPlayer.setSurface(mSurface);

      // NOTE ExoPlayer's already dealt with this by MediaCodecAudioTrackRenderer
      mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mMediaPlayer.setScreenOnWhilePlaying(true);
      mMediaPlayer.prepareAsync();

      // we don't set the target state here either, but preserve the
      // target state that was there before.
      mCurrentState = STATE_PREPARING;
      attachMediaController();
    } catch (IOException ex) {
      Log.w(TAG, "Unable to open content: " + mUri, ex);
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      mErrorListener.onError(mMediaPlayer,
          new PlaybackException(MediaPlayer.MEDIA_ERROR_UNKNOWN, 0));
      return;
    } catch (IllegalArgumentException ex) {
      Log.w(TAG, "Unable to open content: " + mUri, ex);
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      mErrorListener.onError(mMediaPlayer,
          new PlaybackException(MediaPlayer.MEDIA_ERROR_UNKNOWN, 0));
      return;
    }
  }

  public void setMediaController(TrMediaPlayer.Controller controller) {
    if (mController != null) {
      mController.hide();
    }
    mController = controller;
    attachMediaController();
  }

  private void attachMediaController() {
    if (mMediaPlayer != null && mController != null) {
      mController.setMediaPlayer(this);
      View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
      mController.setAnchorView(anchorView);
      mController.setEnabled(isInPlaybackState());
    }
  }

  OnVideoSizeChangedListener mSizeChangedListener = new OnVideoSizeChangedListener() {
    public void onVideoSizeChanged(TrMediaPlayer mp, int width, int height) {
      mVideoWidth = mp.getVideoWidth();
      mVideoHeight = mp.getVideoHeight();
      if (mVideoWidth != 0 && mVideoHeight != 0) {
        getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
        requestLayout();
      }
    }
  };

  OnPreparedListener mPreparedListener = new OnPreparedListener() {
    public void onPrepared(TrMediaPlayer mp) {
      mCurrentState = STATE_PREPARED;

      if (mOnPreparedListener != null) {
        mOnPreparedListener.onPrepared(mMediaPlayer);
      }
      if (mController != null) {
        mController.setEnabled(true);
      }

      mVideoWidth = mp.getVideoWidth();
      mVideoHeight = mp.getVideoHeight();

      long seekToPosition =
          mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
      if (seekToPosition != 0) {
        seekTo(seekToPosition);
      }
      if (mVideoWidth != 0 && mVideoHeight != 0) {
        //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
        getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
        // We won't get a "surface changed" callback if the surface is already the right size, so
        // start the video here instead of in the callback.
        if (mTargetState == STATE_PLAYING) {
          start();
          if (mController != null) {
            mController.show();
          }
        } else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0)) {
          if (mController != null) {
            // Show the media controls when we're paused into a video and make 'em stick.
            mController.show(0);
          }
        }
      } else {
        // We don't know the video size yet, but should start anyway.
        // The video size might be reported to us later.
        if (mTargetState == STATE_PLAYING) {
          start();
        }
      }
    }
  };

  OnCompletionListener mCompletionListener = new OnCompletionListener() {
    public void onCompletion(TrMediaPlayer mp) {
      mCurrentState = STATE_PLAYBACK_COMPLETED;
      mTargetState = STATE_PLAYBACK_COMPLETED;
      if (mController != null) {
        mController.hide();
      }
      if (mOnCompletionListener != null) {
        mOnCompletionListener.onCompletion(mMediaPlayer);
      }
    }
  };

  OnInfoListener mInfoListener = new OnInfoListener() {
    public boolean onInfo(TrMediaPlayer mp, PlaybackInfo info) {
      if (mOnInfoListener != null) {
        mOnInfoListener.onInfo(mp, info);
      }
      return true;
    }
  };

  OnErrorListener mErrorListener = new OnErrorListener() {
    public boolean onError(TrMediaPlayer mp, PlaybackException er) {
      Log.d(TAG, "Error: " + er.toString());
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      if (mController != null) {
        mController.hide();
      }

            /* If an error handler has been supplied, use it and finish. */
      if (mOnErrorListener != null) {
        if (mOnErrorListener.onError(mMediaPlayer, er)) {
          return true;
        }
      }

            /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
      if (getWindowToken() != null) {
        Resources r = getContext().getResources();
        int messageId;

        if (er.what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
          messageId = android.R.string.VideoView_error_text_invalid_progressive_playback;
        } else {
          messageId = android.R.string.VideoView_error_text_unknown;
        }

        new AlertDialog.Builder(getContext()).setMessage(messageId)
            .setPositiveButton(android.R.string.VideoView_error_button,
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                                        /* If we get here, there is no onError listener, so
                                         * at least inform them that the video is over.
                                         */
                    if (mOnCompletionListener != null) {
                      mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                  }
                })
            .setCancelable(false)
            .show();
      }
      return true;
    }
  };

  OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
    public void onBufferingUpdate(TrMediaPlayer mp, int percent) {
      Log.d(TAG,
          "onBufferingUpdate() called with: " + "mp = [" + mp + "], percent = [" + percent + "]");
      mCurrentBufferPercentage = percent;
    }
  };

  /**
   * Register a callback to be invoked when the media file
   * is loaded and ready to go.
   *
   * @param l The callback that will be run
   */
  public void setOnPreparedListener(OnPreparedListener l) {
    mOnPreparedListener = l;
  }

  /**
   * Register a callback to be invoked when the end of a media file
   * has been reached during playback.
   *
   * @param l The callback that will be run
   */
  public void setOnCompletionListener(OnCompletionListener l) {
    mOnCompletionListener = l;
  }

  /**
   * Register a callback to be invoked when an error occurs
   * during playback or setup.  If no listener is specified,
   * or if the listener returned false, TextureVideoView will inform
   * the user of any errors.
   *
   * @param l The callback that will be run
   */
  public void setOnErrorListener(OnErrorListener l) {
    mOnErrorListener = l;
  }

  /**
   * Register a callback to be invoked when an informational event
   * occurs during playback or setup.
   *
   * @param l The callback that will be run
   */
  public void setOnInfoListener(OnInfoListener l) {
    mOnInfoListener = l;
  }

  public void setOnReleasedListener(OnReleasedListener listener) {
    this.mOnReleaseListener = listener;
  }

  TextureView.SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
    @Override public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width,
        final int height) {
      boolean isValidState = (mTargetState == STATE_PLAYING);
      boolean hasValidSize = (width > 0 && height > 0);
      if (mMediaPlayer != null && isValidState && hasValidSize) {
        if (mSeekWhenPrepared != 0) {
          seekTo(mSeekWhenPrepared);
        }
        start();
      }
    }

    @Override public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width,
        final int height) {
      mSurface = new Surface(surface);
      openVideo();
    }

    @Override public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
      // after we return from this we can't use the surface any more
      if (mSurface != null) {
        mSurface.release();
        mSurface = null;
      }
      if (mController != null) mController.hide();
      release(true);
      return true;
    }

    @Override public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
      // do nothing
    }
  };

  /*
   * release the media player in any state
   */
  private void release(boolean clearTargetState) {
    if (mMediaPlayer != null) {
      if (this.mOnReleaseListener != null) {
        mOnReleaseListener.onReleased(mUri, getCurrentPosition(), getDuration());
      }
      mMediaPlayer.reset();
      mMediaPlayer.release();
      mMediaPlayer = null;
      mCurrentState = STATE_IDLE;
      if (clearTargetState) {
        mTargetState = STATE_IDLE;
      }
      AudioManager am = (AudioManager) getContext().getApplicationContext()
          .getSystemService(Context.AUDIO_SERVICE);
      am.abandonAudioFocus(null);
    }
  }

  @Override public boolean onTouchEvent(MotionEvent ev) {
    if (isInPlaybackState() && mController != null) {
      toggleMediaControlsVisibility();
    }
    return false;
  }

  @Override public boolean onTrackballEvent(MotionEvent ev) {
    if (isInPlaybackState() && mController != null) {
      toggleMediaControlsVisibility();
    }
    return false;
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
        keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
        keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
        keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
        keyCode != KeyEvent.KEYCODE_MENU &&
        keyCode != KeyEvent.KEYCODE_CALL &&
        keyCode != KeyEvent.KEYCODE_ENDCALL;
    if (isInPlaybackState() && isKeyCodeSupported && mController != null) {
      if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
        if (mMediaPlayer.isPlaying()) {
          pause();
          mController.show();
        } else {
          start();
          mController.hide();
        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
        if (!mMediaPlayer.isPlaying()) {
          start();
          mController.hide();
        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
          || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
        if (mMediaPlayer.isPlaying()) {
          pause();
          mController.show();
        }
        return true;
      } else {
        toggleMediaControlsVisibility();
      }
    }

    return super.onKeyDown(keyCode, event);
  }

  private void toggleMediaControlsVisibility() {
    if (mController.isShowing()) {
      mController.hide();
    } else {
      mController.show();
    }
  }

  @Override public void start() {
    if (isInPlaybackState()) {
      mMediaPlayer.start();
      mCurrentState = STATE_PLAYING;
    }
    mTargetState = STATE_PLAYING;
  }

  @Override public void start(long position) {
    if (isInPlaybackState()) {
      seekTo(position);
      mMediaPlayer.start();
      mCurrentState = STATE_PLAYING;
    }
    mTargetState = STATE_PLAYING;
  }

  @Override public void pause() {
    if (isInPlaybackState()) {
      if (mMediaPlayer.isPlaying()) {
        mMediaPlayer.pause();
        mCurrentState = STATE_PAUSED;
      }
    }
    mTargetState = STATE_PAUSED;
  }

  @Override public void stop() {
    // FIXME
    pause();
  }

  public void suspend() {
    release(false);
  }

  public void resume() {
    openVideo();
  }

  @Override public long getDuration() {
    if (isInPlaybackState()) {
      return mMediaPlayer.getDuration();
    }

    return -1;
  }

  @Override public long getCurrentPosition() {
    if (isInPlaybackState()) {
      return mMediaPlayer.getCurrentPosition();
    }
    return 0;
  }

  @Override public void seekTo(long milliSec) {
    if (isInPlaybackState()) {
      mMediaPlayer.seekTo(milliSec);
      mSeekWhenPrepared = 0;
    } else {
      mSeekWhenPrepared = milliSec;
    }
  }

  @Override public boolean isPlaying() {
    return isInPlaybackState() && mMediaPlayer.isPlaying();
  }

  @Override public int getBufferPercentage() {
    if (mMediaPlayer != null) {
      return mCurrentBufferPercentage;
    }
    return 0;
  }

  private boolean isInPlaybackState() {
    return (mMediaPlayer != null &&
        mCurrentState != STATE_ERROR &&
        mCurrentState != STATE_IDLE &&
        mCurrentState != STATE_PREPARING);
  }

  public int getAudioSessionId() {
    if (mAudioSession == 0) {
      MediaPlayer player = new MediaPlayer();
      mAudioSession = player.getAudioSessionId();
      player.release();
    }
    return mAudioSession;
  }

  @Override public void setBackgroundAudioEnabled(boolean enabled) {
    mBackgroundAudioEnabled = enabled;
  }

  @Override public void setMediaSource(@NonNull MediaSource source) {
    setMediaUri(source.mediaUri);
  }
}
