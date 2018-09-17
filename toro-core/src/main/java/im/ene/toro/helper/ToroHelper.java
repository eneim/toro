/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package im.ene.toro.helper;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

/**
 * @author eneim (2018/09/16).
 */
public abstract class ToroHelper {

  // This instance should be setup from #initialize and cleared from #release
  protected Container container;

  @CallSuper
  public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
    this.container = container;
  }
}
