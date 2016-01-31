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

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewParent;

/**
 * Created by eneim on 1/31/16.
 */
final class ToroPlayerHelper {

  private final ToroPlayer mPlayer;

  ToroPlayerHelper(@NonNull ToroPlayer player) {
    this.mPlayer = player;
  }

  final void onAttachedToParent(View itemView, ViewParent parent) {
    Toro.onAttachedToParent(mPlayer, itemView, parent);
  }

  final void onDetachedFromParent(View itemView, ViewParent parent) {
    Toro.onDetachedFromParent(mPlayer, itemView, parent);
  }

  final void onCompletion(MediaPlayer mp) {
    Toro.onCompletion(mPlayer, mp);
  }

  final boolean onError(MediaPlayer mp, int what, int extra) {
    return false;
  }

  final boolean onInfo(MediaPlayer mp, int what, int extra) {
    return false;
  }

  final void onPrepared(View itemView, MediaPlayer mp) {
    Toro.onPrepared(mPlayer, itemView, itemView.getParent(), mp);
  }

  final void onSeekComplete(MediaPlayer mp) {

  }
}
