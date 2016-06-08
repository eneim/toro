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
public interface OnBufferingUpdateListener /* extends MediaPlayer.OnBufferingUpdateListener */ {

  /**
   * Called to update status in buffering a media stream received through
   * progressive HTTP download. The received buffering percentage
   * indicates how much of the content has been buffered or played.
   * For example a buffering update of 80 percent when half the content
   * has already been played indicates that the next 30 percent of the
   * content to play has been buffered.
   *
   * @param mp the MediaPlayer the update pertains to
   * @param percent the percentage (0-100) of the content
   * that has been buffered or played thus far
   */
  void onBufferingUpdate(TrMediaPlayer mp, int percent);
}
