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

import android.view.ViewParent;
import java.util.List;

/**
 * Created by eneim on 2/1/16.
 */
public interface ToroStrategy {

  /**
   * @return Description of current Strategy
   * @hide debug use only
   */
  String getDescription();

  /**
   * Each item of candidates returns true for {@link ToroPlayer#wantsToPlay()}. A
   * Strategy gives the best fit Player to start playing
   */
  ToroPlayer findBestPlayer(List<ToroPlayer> candidates);

  /**
   * Called after {@link ToroPlayer#wantsToPlay()} to verify that current player is
   * allowed to play by current Strategy
   *
   * @param player ToroPlayer object which wants to play, and wait for permission
   */
  boolean allowsToPlay(ToroPlayer player, ViewParent parent);

  /**
   * After a video completely played, our Strategy needs to knows if User wants to replay it or not
   *
   * @return true if we allow the player to replay, false otherwise
   */
  @SuppressWarnings("unused")
  boolean allowsImmediateReplay();
}
