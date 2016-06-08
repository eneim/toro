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

package im.ene.lab.toro.player.listener;

import im.ene.lab.toro.player.TrMediaPlayer;

/**
 * Created by eneim on 6/2/16.
 */
public interface OnVideoSizeChangedListener /* extends MediaPlayer.OnVideoSizeChangedListener */ {

  /**
   * Called to indicate the video size
   *
   * The video size (width and height) could be 0 if there was no video,
   * no display surface was set, or the value was not determined yet.
   *
   * @param mp        the MediaPlayer associated with this callback
   * @param width     the width of the video
   * @param height    the height of the video
   */
  void onVideoSizeChanged(TrMediaPlayer mp, int width, int height);
}
