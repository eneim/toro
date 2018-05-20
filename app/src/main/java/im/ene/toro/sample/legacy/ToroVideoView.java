/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.legacy;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * A simple wrapper on top of {@link VideoView} to catch the start/pause actions.
 *
 * @author eneim | 6/11/17.
 */

public class ToroVideoView extends VideoView {

  private PlayerEventListener playerEventListener;

  public ToroVideoView(Context context) {
    super(context);
  }

  public ToroVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ToroVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /**
   * {@inheritDoc}
   */
  @Override public void start() {
    super.start();
    if (this.playerEventListener != null) {
      this.playerEventListener.onPlay();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override public void pause() {
    super.pause();
    if (this.playerEventListener != null) {
      this.playerEventListener.onPause();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override public void resume() {
    super.resume();
    if (this.playerEventListener != null) {
      this.playerEventListener.onPlay();
    }
  }

  public void setPlayerEventListener(PlayerEventListener playerEventListener) {
    this.playerEventListener = playerEventListener;
  }

  public interface PlayerEventListener {

    /**
     * Called when this VideoView is started from external request.
     */
    void onPlay();

    /**
     * Called when this VideoView is paused from external request.
     */
    void onPause();
  }
}
