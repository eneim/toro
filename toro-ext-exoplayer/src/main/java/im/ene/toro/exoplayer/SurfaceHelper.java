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

package im.ene.toro.exoplayer;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

/**
 * Created by eneim on 10/1/16.
 */

abstract class SurfaceHelper
    implements TextureView.SurfaceTextureListener, SurfaceHolder.Callback {

  // Place Holders

  @Override public void surfaceCreated(SurfaceHolder holder) {
    // Do nothing
  }

  @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    // Do nothing
  }

  @Override public void surfaceDestroyed(SurfaceHolder holder) {
    // Do nothing
  }

  @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    // Do nothing
  }

  @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    // Do nothing
  }

  @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    return false;
  }

  @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    // Do nothing
  }

  final void setupForView(View view) {
    if (view instanceof SurfaceView) {
      ((SurfaceView) view).getHolder().addCallback(this);
    } else if (view instanceof TextureView) {
      ((TextureView) view).setSurfaceTextureListener(this);
    }
  }

  static class Factory {

    static SurfaceHelper getInstance(final ExoVideoView parent, View view) {
      return view instanceof TextureView ? new SurfaceHelper() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
          Log.d("TEXTURE", "AVAILABLE");
          parent.mSurface = new Surface(surface);
          if (parent.mMediaPlayer != null) {
            parent.mMediaPlayer.setSurface(parent.mSurface);
            if (!parent.mPlayerNeedsPrepare) {
              parent.mMediaPlayer.seekTo(parent.mPlayerPosition);
              parent.mMediaPlayer.setPlayWhenReady(parent.mPlayRequested);
            }
          } else {
            parent.preparePlayer(parent.mPlayRequested);
          }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
          Log.d("TEXTURE", "SIZE CHANGED");
        }

        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
          Log.d("TEXTURE", "DESTROYED");
          if (!parent.mBackgroundAudioEnabled) {
            parent.releasePlayer();
          } else {
            if (parent.mMediaPlayer != null) {
              parent.mMediaPlayer.setBackgrounded(true);
            }
          }

          if (parent.mMediaPlayer != null) {
            parent.mMediaPlayer.blockingClearSurface();
          }

          parent.mPlayerNeedsPrepare = true;
          parent.mSurface = null;
          return true;
        }

        @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
          Log.d("TEXTURE", "UPDATED");
        }
      } : new SurfaceHelper() {
        @Override public void surfaceCreated(SurfaceHolder holder) {
          Log.d("SURFACE", "CREATED");
          parent.mSurface = holder.getSurface();
          if (parent.mMediaPlayer != null) {
            parent.mMediaPlayer.setSurface(parent.mSurface);
            if (!parent.mPlayerNeedsPrepare) {
              parent.mMediaPlayer.seekTo(parent.mPlayerPosition);
              parent.mMediaPlayer.setPlayWhenReady(parent.mPlayRequested);
            }
          } else {
            parent.preparePlayer(parent.mPlayRequested);
          }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
          Log.d("SURFACE", "CHANGED");
        }

        @Override public void surfaceDestroyed(SurfaceHolder holder) {
          Log.d("SURFACE", "DESTROYED");
          if (!parent.mBackgroundAudioEnabled) {
            parent.releasePlayer();
          } else {
            if (parent.mMediaPlayer != null) {
              parent.mMediaPlayer.setBackgrounded(true);
            }
          }

          if (parent.mMediaPlayer != null) {
            parent.mMediaPlayer.blockingClearSurface();
          }

          parent.mPlayerNeedsPrepare = true;
          parent.mSurface = null;
        }
      };
    }
  }
}
