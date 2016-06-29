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

package im.ene.lab.toro.sample.base;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.VideoPlayerManager;
import im.ene.lab.toro.VideoPlayerManagerImpl;
import im.ene.lab.toro.ViewHolderCallback;

/**
 * Created by eneim on 5/20/16.
 */
public abstract class BaseCustomAdapter<VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH> implements VideoPlayerManager {

  protected final VideoPlayerManager delegate;

  public BaseCustomAdapter() {
    super();
    delegate = new VideoPlayerManagerImpl();
    setHasStableIds(true);
  }

  @Override public ToroPlayer getPlayer() {
    return delegate.getPlayer();
  }

  @Override public void setPlayer(ToroPlayer player) {
    delegate.setPlayer(player);
  }

  @Override public void onRegistered() {
    delegate.onRegistered();
  }

  @Override public void onUnregistered() {
    delegate.onUnregistered();
  }

  @Override public void startPlayback() {
    delegate.startPlayback();
  }

  @Override public void pausePlayback() {
    delegate.pausePlayback();
  }

  @Override public void stopPlayback() {
    delegate.stopPlayback();
  }

  @Override public void saveVideoState(String videoId, @Nullable Long position, long duration) {
    delegate.saveVideoState(videoId, position, duration);
  }

  @Override public void restoreVideoState(String videoId) {
    delegate.restoreVideoState(videoId);
  }

  @Nullable @Override public Long getSavedPosition(String videoId) {
    return delegate.getSavedPosition(videoId);
  }

  @Override public void onViewAttachedToWindow(VH holder) {
    if (holder instanceof ViewHolderCallback) {
      ((ViewHolderCallback) holder).onAttachedToParent();
    }
  }

  @Override public void onViewDetachedFromWindow(VH holder) {
    if (holder instanceof ViewHolderCallback) {
      ((ViewHolderCallback) holder).onDetachedFromParent();
    }
  }
}
