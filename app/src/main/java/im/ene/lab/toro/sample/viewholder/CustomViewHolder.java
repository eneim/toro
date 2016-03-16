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

package im.ene.lab.toro.sample.viewholder;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import im.ene.lab.toro.RecyclerViewItemHelper;
import im.ene.lab.toro.ToroPlayer;

/**
 * Created by eneim on 3/7/16.
 */
public class CustomViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {

  ViewHolderHelper mHelper;

  public CustomViewHolder(View itemView) {
    super(itemView);
    mHelper = new ViewHolderHelper();
  }

  @Override public boolean wantsToPlay() {
    return false;
  }

  @Override public boolean isAbleToPlay() {
    return false;
  }

  @Override public boolean isLoopAble() {
    return true;
  }

  @Override public float visibleAreaOffset() {
    return 0;
  }

  @Nullable @Override public String getVideoId() {
    return null;
  }

  @Override public int getPlayOrder() {
    return 0;
  }

  @NonNull @Override public View getVideoView() {
    return null;
  }

  @Override public void onActivityPaused() {

  }

  @Override public void onActivityResumed() {

  }

  @Override public void onVideoPrepared(MediaPlayer mp) {

  }

  @Override public void onPlaybackStarted() {

  }

  @Override public void onPlaybackPaused() {

  }

  @Override public void onPlaybackStopped() {

  }

  @Override public boolean onPlaybackError(MediaPlayer mp, int what, int extra) {
    return true;
  }

  @Override public void onPlaybackInfo(MediaPlayer mp, int what, int extra) {

  }

  @Override public void onPlaybackProgress(int position, int duration) {

  }

  @Override public void start() {

  }

  @Override public void pause() {

  }

  @Override public int getDuration() {
    return 0;
  }

  @Override public int getCurrentPosition() {
    return 0;
  }

  @Override public void seekTo(int pos) {

  }

  @Override public boolean isPlaying() {
    return false;
  }

  @Override public int getBufferPercentage() {
    return 0;
  }

  @Override public boolean canPause() {
    return true;
  }

  @Override public boolean canSeekBackward() {
    return true;
  }

  @Override public boolean canSeekForward() {
    return true;
  }

  @Override public int getAudioSessionId() {
    return 0;
  }

  @Override public void onCompletion(MediaPlayer mp) {
    mHelper.onCompletion(this, mp);
  }

  @Override public boolean onError(MediaPlayer mp, int what, int extra) {
    return mHelper.onError(this, mp, what, extra);
  }

  @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
    return mHelper.onInfo(this, mp, what, extra);
  }

  @Override public void onPrepared(MediaPlayer mp) {
    mHelper.onPrepared(this, itemView, itemView.getParent(), mp);
  }

  @Override public void onSeekComplete(MediaPlayer mp) {
    mHelper.onSeekComplete(this, mp);
  }

  private static class ViewHolderHelper extends RecyclerViewItemHelper {

  }
}
