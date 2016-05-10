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

package im.ene.lab.toro.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import im.ene.lab.toro.R;
import java.io.IOException;
import java.util.Map;

/**
 * Displays a video file.  The TextureVideoView class
 * can load images from various sources (such as resources or content
 * providers), takes care of computing its measurement from the video so that
 * it can be used in any layout manager, and provides various display options
 * such as scaling and tinting.<p>
 * <p/>
 * <em>Note: VideoView does not retain its full state when going into the
 * background.</em>  In particular, it does not restore the current play state,
 * play position or selected tracks.  Applications should
 * save and restore these on their own in
 * {@link android.app.Activity#onSaveInstanceState} and
 * {@link android.app.Activity#onRestoreInstanceState}.<p>
 * Also note that the audio session id (from {@link #getAudioSessionId}) may
 * change from its previously returned value when the VideoView is restored.<p>
 * <p/>
 * This code is based on the official Android sources for 6.0.1_r10 with the following differences:
 * <ol>
 * <li>extends {@link android.view.TextureView} instead of a {@link android.view.SurfaceView}
 * allowing proper view animations</li>
 * <li>removes code that uses hidden APIs and thus is not available (e.g. subtitle support)</li>
 * </ol>
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) public class ToroVideoView
    extends TextureView implements MediaPlayerControl {
  // all possible internal states
  private static final int STATE_ERROR = -1;
  private static final int STATE_IDLE = 0;
  private static final int STATE_PREPARING = 1;
  private static final int STATE_PREPARED = 2;
  private static final int STATE_PLAYING = 3;
  private static final int STATE_PAUSED = 4;
  private static final int STATE_PLAYBACK_COMPLETED = 5;
  private String TAG = "TextureVideoView";
  // settable by the client
  private Uri mUri;
  private Map<String, String> mHeaders;
  // mCurrentState is a TextureVideoView object's current state.
  // mTargetState is the state that a method caller intends to reach.
  // For instance, regardless the TextureVideoView object's current state,
  // calling pause() intends to bring the object to a target state
  // of STATE_PAUSED.
  private int mCurrentState = STATE_IDLE;
  private int mTargetState = STATE_IDLE;

  // Scale mode
  // 0. Width: view width is fixed (or full fill parent), we re-calculate height by width
  private static final int SCALE_MODE_DEFAULT = 0;
  // 0. Width: view width is fixed (or full fill parent), we re-calculate height by width
  private static final int SCALE_MODE_FIT_WIDTH = 1;
  // 1. Height: view height is fixed (or full fill parent), we re-calculate width by height
  private static final int SCALE_MODE_FIT_HEIGHT = 2;
  // 2. Fit inside: scale to fit the most visible area. Don't use on large screen :trollface:
  private static final int SCALE_MODE_FIT_INSIDE = 3;

  // Scale mode
  private int mScaleMode = SCALE_MODE_FIT_HEIGHT;

  // All the stuff we need for playing and showing a video
  private Surface mSurface = null;
  private MediaPlayer mMediaPlayer = null;
  private int mAudioSession;
  private int mVideoWidth;
  private int mVideoHeight;
  MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
      new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
          mVideoWidth = mp.getVideoWidth();
          mVideoHeight = mp.getVideoHeight();
          if (mVideoWidth != 0 && mVideoHeight != 0) {
            switch (mScaleMode) {
              case SCALE_MODE_FIT_WIDTH:
                if (getWidth() > 0 && mVideoWidth != getWidth()) {
                  mVideoHeight = (int) (getWidth() / (float) mVideoWidth * mVideoHeight);
                  mVideoWidth = getWidth();
                }
                break;
              case SCALE_MODE_FIT_HEIGHT:
                if (getHeight() > 0 && mVideoHeight != getHeight()) {
                  mVideoWidth = (int) (getHeight() / (float) mVideoHeight * mVideoWidth);
                  mVideoHeight = getHeight();
                }
                break;
              case SCALE_MODE_DEFAULT:
              default:
                // Keep default, do nothing
                break;
            }

            getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
            requestLayout();
          }
        }
      };
  private MediaController mMediaController;
  private OnCompletionListener mOnCompletionListener;
  private MediaPlayer.OnPreparedListener mOnPreparedListener;
  private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
  MediaPlayer.OnSeekCompleteListener mSeekCompleteListener =
      new MediaPlayer.OnSeekCompleteListener() {
        @Override public void onSeekComplete(MediaPlayer mp) {
          Log.d(TAG, "onSeekComplete() called with: " + "mp = [" + mp + "]");
          if (mOnSeekCompleteListener != null) {
            mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
          }
        }
      };
  private int mCurrentBufferPercentage;
  private OnErrorListener mOnErrorListener;
  private OnInfoListener mOnInfoListener;
  private int mSeekWhenPrepared;  // recording the seek position while preparing
  private boolean mCanPause;
  private boolean mCanSeekBack;
  private boolean mCanSeekForward;
  private boolean mRequestAudioFocus = true;
  MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
    public void onPrepared(MediaPlayer mp) {
      mCurrentState = STATE_PREPARED;

      mCanPause = mCanSeekBack = mCanSeekForward = true;

      if (mOnPreparedListener != null) {
        mOnPreparedListener.onPrepared(mMediaPlayer);
      }
      if (mMediaController != null) {
        mMediaController.setEnabled(true);
      }

      mVideoWidth = mp.getVideoWidth();
      mVideoHeight = mp.getVideoHeight();

      // mSeekWhenPrepared may be changed after seekTo() call
      int seekToPosition = mSeekWhenPrepared;

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
          if (mMediaController != null) {
            mMediaController.show();
          }
        } else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0)) {
          if (mMediaController != null) {
            // Show the media controls when we're paused into a video and make 'em stick.
            mMediaController.show(0);
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
  private MediaPlayer.OnCompletionListener mCompletionListener =
      new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
          mCurrentState = STATE_PLAYBACK_COMPLETED;
          mTargetState = STATE_PLAYBACK_COMPLETED;
          if (mMediaController != null) {
            mMediaController.hide();
          }
          if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mMediaPlayer);
          }
        }
      };
  private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
    public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
      if (mOnInfoListener != null) {
        mOnInfoListener.onInfo(mp, arg1, arg2);
      }
      return true;
    }
  };
  private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
    public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
      Log.d(TAG, "Error: " + framework_err + "," + impl_err);
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      if (mMediaController != null) {
        mMediaController.hide();
      }

            /* If an error handler has been supplied, use it and finish. */
      if (mOnErrorListener != null) {
        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
          return true;
        }
      }

            /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
      if (getWindowToken() != null) {
        int messageId;

        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
          messageId = android.R.string.VideoView_error_text_invalid_progressive_playback;
        } else {
          messageId = android.R.string.VideoView_error_text_unknown;
        }

        if (mOnPlaybackErrorListener != null) {
          mOnPlaybackErrorListener.onError(messageId);
        }
      }
      return true;
    }
  };
  private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
      new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
          mCurrentBufferPercentage = percent;
        }
      };

  // TODO Apply this
  private OnPlaybackError mOnPlaybackErrorListener;
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
      if (mMediaController != null) mMediaController.hide();
      release(true);
      return true;
    }

    @Override public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
      // do nothing
    }
  };

  public ToroVideoView(Context context) {
    super(context);
    initVideoView();
  }

  public ToroVideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
    initVideoView();
  }

  public ToroVideoView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToroVideoView);
    try {
      mScaleMode = a.getInteger(R.styleable.ToroVideoView_videoScaleMode, SCALE_MODE_DEFAULT);
    } finally {
      a.recycle();
    }
    initVideoView();
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

  @Override public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
    super.onInitializeAccessibilityEvent(event);
    event.setClassName(ToroVideoView.class.getName());
  }

  @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(ToroVideoView.class.getName());
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
        keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
        keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
        keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
        keyCode != KeyEvent.KEYCODE_MENU &&
        keyCode != KeyEvent.KEYCODE_CALL &&
        keyCode != KeyEvent.KEYCODE_ENDCALL;
    if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
      if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
        if (mMediaPlayer.isPlaying()) {
          pause();
          mMediaController.show();
        } else {
          start();
          mMediaController.hide();
        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
        if (!mMediaPlayer.isPlaying()) {
          start();
          mMediaController.hide();
        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
          || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
        if (mMediaPlayer.isPlaying()) {
          pause();
          mMediaController.show();
        }
        return true;
      } else {
        toggleMediaControlsVisibility();
      }
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override public boolean onTrackballEvent(MotionEvent ev) {
    if (isInPlaybackState() && mMediaController != null) {
      toggleMediaControlsVisibility();
    }
    return false;
  }

  @Override public boolean onTouchEvent(MotionEvent ev) {
    if (isInPlaybackState() && mMediaController != null) {
      toggleMediaControlsVisibility();
    }
    return false;
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
        switch (mScaleMode) {
          case SCALE_MODE_FIT_WIDTH:
            height = width * mVideoHeight / mVideoWidth;
            break;
          case SCALE_MODE_FIT_HEIGHT:
            width = height * mVideoWidth / mVideoHeight;
            break;
          case SCALE_MODE_DEFAULT:
          default:
            if (mVideoWidth * height < width * mVideoHeight) {
              //Log.i("@@@", "image too wide, correcting");
              width = height * mVideoWidth / mVideoHeight;
            } else if (mVideoWidth * height > width * mVideoHeight) {
              //Log.i("@@@", "image too tall, correcting");
              height = width * mVideoHeight / mVideoWidth;
            }
            break;
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

  private boolean isInPlaybackState() {
    return (mMediaPlayer != null &&
        mCurrentState != STATE_ERROR &&
        mCurrentState != STATE_IDLE &&
        mCurrentState != STATE_PREPARING);
  }

  @Override public void start() {
    if (isInPlaybackState()) {
      mMediaPlayer.start();
      mCurrentState = STATE_PLAYING;
    }
    mTargetState = STATE_PLAYING;
  }

  private void toggleMediaControlsVisibility() {
    if (mMediaController.isShowing()) {
      mMediaController.hide();
    } else {
      mMediaController.show();
    }
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

  @Override public int getDuration() {
    if (isInPlaybackState()) {
      return mMediaPlayer.getDuration();
    }

    return -1;
  }

  @Override public int getCurrentPosition() {
    if (isInPlaybackState()) {
      return mMediaPlayer.getCurrentPosition();
    }
    return 0;
  }

  @Override public void seekTo(int msec) {
    if (isInPlaybackState()) {
      mMediaPlayer.seekTo(msec);
      mSeekWhenPrepared = 0;
    } else {
      mSeekWhenPrepared = msec;
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

  @Override public boolean canPause() {
    return mCanPause;
  }

  @Override public boolean canSeekBackward() {
    return mCanSeekBack;
  }

  @Override public boolean canSeekForward() {
    return mCanSeekForward;
  }

  public int getAudioSessionId() {
    if (mAudioSession == 0) {
      MediaPlayer foo = new MediaPlayer();
      mAudioSession = foo.getAudioSessionId();
      foo.release();
    }
    return mAudioSession;
  }

  public int resolveAdjustedSize(int desiredSize, int measureSpec) {
    return getDefaultSize(desiredSize, measureSpec);
  }

  /**
   * Sets video path.
   *
   * @param path the path of the video.
   */
  public void setVideoPath(String path) {
    setVideoURI(Uri.parse(path));
  }

  /**
   * Sets video URI.
   *
   * @param uri the URI of the video.
   */
  public void setVideoURI(Uri uri) {
    setVideoURI(uri, null);
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
  public void setVideoURI(Uri uri, Map<String, String> headers) {
    mUri = uri;
    mHeaders = headers;
    mSeekWhenPrepared = 0;
    openVideo();
    requestLayout();
    invalidate();
  }

  private void openVideo() {
    if (mUri == null || mSurface == null) {
      // not ready for playback just yet, will try again later
      return;
    }
    // we shouldn't clear the target state, because somebody might have
    // called start() previously
    release(false);

    AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    if (mRequestAudioFocus) {
      am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    try {
      mMediaPlayer = new MediaPlayer();

      if (mAudioSession != 0) {
        mMediaPlayer.setAudioSessionId(mAudioSession);
      } else {
        mAudioSession = mMediaPlayer.getAudioSessionId();
      }
      mMediaPlayer.setOnPreparedListener(mPreparedListener);
      mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
      mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
      mMediaPlayer.setOnCompletionListener(mCompletionListener);
      mMediaPlayer.setOnErrorListener(mErrorListener);
      mMediaPlayer.setOnInfoListener(mInfoListener);
      mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
      mCurrentBufferPercentage = 0;
      mMediaPlayer.setDataSource(getContext().getApplicationContext(), mUri, mHeaders);
      mMediaPlayer.setSurface(mSurface);
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
      mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
      return;
    } catch (IllegalArgumentException ex) {
      Log.w(TAG, "Unable to open content: " + mUri, ex);
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
      return;
    }
  }

  /*
   * release the media player in any state
   */
  private void release(boolean cleartargetstate) {
    if (mMediaPlayer != null) {
      mMediaPlayer.reset();
      mMediaPlayer.release();
      mMediaPlayer = null;
      mCurrentState = STATE_IDLE;
      if (cleartargetstate) {
        mTargetState = STATE_IDLE;
      }
      AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
      am.abandonAudioFocus(null);
    }
  }

  private void attachMediaController() {
    if (mMediaPlayer != null && mMediaController != null) {
      mMediaController.setMediaPlayer(this);
      View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
      mMediaController.setAnchorView(anchorView);
      mMediaController.setEnabled(isInPlaybackState());
    }
  }

  public void stopPlayback() {
    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
      mCurrentState = STATE_IDLE;
      mTargetState = STATE_IDLE;
      AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
      am.abandonAudioFocus(null);
    }
  }

  public void setMediaController(MediaController controller) {
    if (mMediaController != null) {
      mMediaController.hide();
    }
    mMediaController = controller;
    attachMediaController();
  }

  public void shouldRequestAudioFocus(boolean should) {
    this.mRequestAudioFocus = should;
  }

  /**
   * Register a callback to be invoked when the media file
   * is loaded and ready to go.
   *
   * @param l The callback that will be run
   */
  public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
    mOnPreparedListener = l;
  }

  /**
   * Register a callback to be invoked when user seeks the video
   *
   * @param l The callback that will be called
   */
  public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener l) {
    mOnSeekCompleteListener = l;
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

  public void suspend() {
    release(false);
  }

  public void resume() {
    openVideo();
  }

  public interface OnPlaybackError {

    void onError(int resourceId);
  }
}
