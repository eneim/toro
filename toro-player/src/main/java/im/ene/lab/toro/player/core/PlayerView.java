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

import android.net.Uri;
import android.view.Surface;

/**
 * Created by eneim on 6/3/16.
 */
public interface PlayerView {

  void updatePlaybackState(boolean isPlaying, int state);

  void updateSize(int width, int height, int unAppliedRotationDegrees,
      float pixelWidthAspectRatio);

  void updatePosition(long position, long duration);

  // Playback progress in percent
  void updateProgress(int progress);

  // Buffering percent
  void updateBuffer(int buffer);

  void showError(Exception error);

  Surface getSurface();

  void setVideoUri(Uri uri);

  boolean surfaceAvailable();
}
