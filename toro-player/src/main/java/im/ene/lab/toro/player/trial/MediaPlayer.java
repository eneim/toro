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

package im.ene.lab.toro.player.trial;

import android.content.Context;
import android.net.Uri;

/**
 * Created by eneim on 6/4/16.
 *
 * The controller for {@link MediaPlayerWidget}, which supposes to take the responsibility to
 * control the whole playback cycle. {@link android.media.MediaPlayer} is a real example for this.
 */
// TODO Rename this interface
public interface MediaPlayer {

  void create();

  void release();

  void start();

  void restart();

  void stop();

  void pause();

  void resume();

  void seekTo(long milliSec);

  void setMediaSource(Context context, Uri uri);
}
