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

package im.ene.lab.toro.media;

/**
 * Created by eneim on 6/2/16.
 */
public interface OnInfoListener /* extends MediaPlayer.OnInfoListener */ {

  /**
   * Called to indicate an info or a warning.
   *
   * @return True if the method handled the info, false if it didn't.
   * Returning false, or not having an OnErrorListener at all, will
   * cause the info to be discarded.
   */
  boolean onInfo(Cineer mp, PlaybackInfo info);
}
