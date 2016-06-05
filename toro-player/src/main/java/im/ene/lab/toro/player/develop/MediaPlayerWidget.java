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

package im.ene.lab.toro.player.develop;

import android.net.Uri;
import android.support.annotation.FloatRange;
import im.ene.lab.toro.player.MediaSource;

/**
 * Created by eneim on 6/4/16.
 *
 * Define some core actions of a MediaPlayer. Those actions are supposed to be used by User
 * Interface.
 */
public interface MediaPlayerWidget {

  void play();

  void pause();

  void stop();

  void seekTo(long milliSec);

  void setVolume(@FloatRange(from = 0.f, to = 1.f) float factor);

  /* void mute(); */ // this is equal to setVolume(0), so it will be ignored

  void setMediaSource(MediaSource source);

  void setMediaUri(Uri uri);
}
