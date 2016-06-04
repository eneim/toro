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

package im.ene.lab.toro.player.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import im.ene.lab.toro.player.MediaSource;
import im.ene.lab.toro.player.TrMediaPlayer;

/**
 * Created by eneim on 6/3/16.
 */
public class PlayerViewImpl extends TextureView implements PlayerView, TrMediaPlayer.IMediaPlayer {

  /**
   * The surface view will not resize itself if the fractional difference
   * between its default aspect ratio and the aspect ratio of the video falls
   * below this threshold.
   */
  private static final float MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0.01f;

  /**
   * The ratio of the width and height of the video.
   */
  private float videoAspectRatio;

  private Surface surface;

  private PlayerPresenter presenter;

  public PlayerViewImpl(Context context) {
    this(context, null);
  }

  public PlayerViewImpl(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PlayerViewImpl(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public PlayerViewImpl(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    setSurfaceTextureListener(surfaceTextureListener);
    initialize(context);
  }

  private SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
    @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
      PlayerViewImpl.this.surface = new Surface(surface);
      start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
      PlayerViewImpl.this.surface = new Surface(surface);
    }

    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
      PlayerViewImpl.this.surface = null;
      return false;
    }

    @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
  };

  private void initialize(Context context) {
    presenter = new PlayerPresenterImpl(context, this);
    presenter.onCreate();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    presenter.onDestroy();
  }

  @Override public void updatePlaybackState(boolean isPlaying, int state) {

  }

  @Override public void updateSize(int width, int height, int unAppliedRotationDegrees,
      float pixelWidthAspectRatio) {
    setVideoWidthHeightRatio(height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
  }

  @Override public void updatePosition(long position, long duration) {

  }

  @Override public void updateProgress(int progress) {

  }

  @Override public void updateBuffer(int buffer) {

  }

  @Override public void showError(Exception error) {

  }

  @Override public Surface getSurface() {
    return surface;
  }

  @Override public void setVideoUri(Uri uri) {
    presenter.setMediaSource(new MediaSource(uri, MediaSource.VideoType.MP4));
  }

  @Override public boolean surfaceAvailable() {
    return surface != null;
  }

  @Override public void start() {
    presenter.startPlayback(0);
  }

  @Override public void start(long position) {
    presenter.startPlayback((int) position);
  }

  @Override public void pause() {
    presenter.pausePlayback();
  }

  @Override public long getDuration() {
    return presenter.getDuration();
  }

  @Override public long getCurrentPosition() {
    return presenter.getCurrentPosition();
  }

  @Override public void seekTo(long pos) {
    presenter.seekTo(pos);
  }

  @Override public boolean isPlaying() {
    return presenter.isPlaying();
  }

  @Override public int getBufferPercentage() {
    return presenter.getBufferPercentage();
  }

  @Override public boolean canPause() {
    return true;
  }

  @Override public boolean canSeekBackward() {
    return true;
  }

  @Override public boolean canSeekForward() {
    return true;
  }

  @Override public int getAudioSessionId() {
    return presenter.getAudioSessionId();
  }

  /**
   * Resize the view based on the width and height specifications.
   *
   * @param widthMeasureSpec The specified width.
   * @param heightMeasureSpec The specified height.
   */
  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = getMeasuredWidth();
    int height = getMeasuredHeight();
    if (videoAspectRatio != 0) {
      float viewAspectRatio = (float) width / height;
      float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
      if (aspectDeformation > MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
        height = (int) (width / videoAspectRatio);
      } else if (aspectDeformation < -MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
        width = (int) (height * videoAspectRatio);
      }
    }
    setMeasuredDimension(width, height);
  }

  /**
   * Set the aspect ratio that this {@link PlayerViewImpl} should satisfy.
   *
   * @param widthHeightRatio The width to height ratio.
   */
  public void setVideoWidthHeightRatio(float widthHeightRatio) {
    if (this.videoAspectRatio != widthHeightRatio) {
      this.videoAspectRatio = widthHeightRatio;
      requestLayout();
    }
  }
}
