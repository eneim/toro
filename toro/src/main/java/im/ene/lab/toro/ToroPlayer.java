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

package im.ene.lab.toro;

import android.graphics.Rect;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.MediaController;

/**
 * Created by eneim on 1/29/16.
 */
public interface ToroPlayer extends MediaController.MediaPlayerControl {

  /**
   * @param parentRect parent rect, get from {@link android.view.View#getLocalVisibleRect(Rect)}
   * @param childRect child rect,  get from {@link android.view.View#getLocalVisibleRect(Rect)}
   * @return true if current Video is eager to play its video, or false otherwise
   */
  boolean wantsToPlay(@Nullable Rect parentRect, @NonNull Rect childRect);

  /**
   * @return value from 0.0 ~ 1.0 the visible offset of current Video
   */
  @FloatRange(from = 0.0, to = 1.0) float visibleAreaOffset();

  /**
   * Support save/restore Video state (last played/paused position)
   *
   * @return current Video's id. !IMPORTANT this ID must be unique, and avoid using Video's
   * filename, url String or any object which depends on the Video object itself. There will be
   * the case user uses same Video in multiple place. User of this library should better use the
   * main object (which holds the Video as member) as key to generate this Id.
   * <p/>
   * Furthermore, ToroPlayer would be recycled, so it requires a separated, resource-depended Id
   */
  @Nullable Long getVideoId();

  /**
   * Position in Adapter
   */
  int getPlayerPosition();

  /**
   * Host Activity paused
   */
  void onActivityPaused();

  /**
   * Host Activity resumed
   */
  void onActivityResumed();
}
