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

import im.ene.lab.toro.player.MediaSource;
import im.ene.lab.toro.player.TrMediaPlayer;
import im.ene.lab.toro.player.internal.ExoMediaPlayer;

/**
 * Created by eneim on 6/3/16.
 *
 * An interface which demonstrate a State-ful MediaPlayer, mainly support Toro's concept
 */
public interface PlayerPresenter extends TrMediaPlayer.IMediaPlayer {

  String TAG = "PlayerPresenter";

  void onCreate();

  void setMediaSource(MediaSource source);

  // start from a specific position. 0 by default
  void startPlayback(int position);

  void pausePlayback();

  void stopPlayback();

  void release();

  void onDestroy();

  // get the use of Listeners
  void addListener(ExoMediaPlayer.Listener listener);

  // player stuff
  long getDuration();

  long getCurrentPosition();
}
