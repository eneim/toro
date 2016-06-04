/**
 Copyright 2014 Google Inc. All rights reserved.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.google.android.libraries.mediaframework.exoplayerextensions;

/**
 * Callback for handling the play or pause of a player control.
 */
public interface PlayerControlCallback {

  /**
   * Respond when the player has just been paused.
   */
  public void onPause();

  /**
   * Respond when the player just started playing.
   */
  public void onPlay();
}
