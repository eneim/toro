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

package com.google.android.libraries.mediaframework.layeredvideo;

import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;
import com.google.android.libraries.mediaframework.exoplayerextensions.ExoplayerWrapper;
import im.ene.lab.toro.player.R;

/**
 * Creates a view which can render video.
 */
public class VideoSurfaceLayer implements Layer {

  /**
   * Whether the video starts playing as soon as the surface is created.
   */
  private boolean autoplay;

  /**
   * The {@link LayerManager} whcih is responsible for creating this layer's view and adding it to
   * the video player.
   */
  private LayerManager layerManager;

  /**
   * When a size change occurs, change the size of the surface view.
   */
  private ExoplayerWrapper.PlaybackListener playbackListener
      = new ExoplayerWrapper.PlaybackListener() {
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
      // Do nothing. VideoSurfaceLayer doesn't care about state changes.
    }

    @Override
    public void onError(Exception e) {
      // Do nothing. VideoSurfaceLayer doesn't care about errors here.
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthAspectRatio) {
      surfaceView.setVideoWidthHeightRatio(
          height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }
  };

  /**
   * Bind the surface view to the {@link ExoplayerWrapper} and unbind them when the surface view is
   * destroyed.
   */
  private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
      ExoplayerWrapper wrapper = layerManager.getExoplayerWrapper();
      if (wrapper != null) {
        wrapper.setSurface(surfaceHolder.getSurface());
        if (wrapper.getSurface().isValid() ||
            wrapper.getStateForTrackType(ExoplayerWrapper.TYPE_VIDEO)
                == ExoplayerWrapper.DISABLED_TRACK) {
          wrapper.setPlayWhenReady(autoplay);
        }
      }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
      if (layerManager.getExoplayerWrapper() != null) {
        layerManager.getExoplayerWrapper().blockingClearSurface();
      }
    }
  };

  /**
   * This is where the video is displayed.
   */
  private VideoSurfaceView surfaceView;

  /**
   * This is created by the {@link VideoSurfaceLayer#createView(LayerManager)} function.
   */
  private FrameLayout view;

  /**
   * @param autoplay Whether the video should start playing as soon as the surface view is created.
   */
  public VideoSurfaceLayer(boolean autoplay) {
    this.autoplay = autoplay;
  }

  @Override
  public FrameLayout createView(LayerManager layerManager) {
    this.layerManager = layerManager;

    LayoutInflater inflater = layerManager.getActivity().getLayoutInflater();
    view = (FrameLayout) inflater.inflate(R.layout.video_surface_layer, null);

    layerManager.getExoplayerWrapper().addListener(playbackListener);

    surfaceView = (VideoSurfaceView) view.findViewById(R.id.surface_view);
    if (surfaceView != null) {
      SurfaceHolder holder = surfaceView.getHolder();
      holder.addCallback(surfaceHolderCallback);
    }

    return view;
  }

  @Override
  public void onLayerDisplayed(LayerManager layerManager) {

  }

  /**
   * When mutliple surface layers are used (ex. in the case of ad playback), one layer must be
   * overlaid on top of another. This method sends this player's surface layer to the background
   * so that other surface layers can be overlaid on top of it.
   */
  public void moveSurfaceToBackground() {
    surfaceView.setZOrderMediaOverlay(false);
  }

  /**
   * When mutliple surface layers are used (ex. in the case of ad playback), one layer must be
   * overlaid on top of another. This method sends this player's surface layer to the foreground
   * so that it is overlaid on top of all layers which are in the background.
   */
  public void moveSurfaceToForeground() {
    surfaceView.setZOrderMediaOverlay(true);
  }

  /**
   * Sets whether the video should start playing as soon as the surface is created.
   * @param autoplay If true, the video starts playing as soon as the surface is created. If false,
   *                 then the video must be started programmatically.
   */
  public void setAutoplay(boolean autoplay) {
    this.autoplay = autoplay;
  }

  /**
   * When you are finished using this object, call this method.
   */
  public void release() {
    ExoplayerWrapper wrapper = layerManager.getExoplayerWrapper();
    if (wrapper != null) {
      wrapper.removeListener(playbackListener);
    }
  }
}
