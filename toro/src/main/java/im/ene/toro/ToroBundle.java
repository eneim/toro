/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro;

/**
 * Created by eneim on 1/22/17.
 */

final class ToroBundle {

  private OnScrollListenerImpl scrollListener;

  private PlayerManager manager;

  private ToroStrategy strategy;

  OnScrollListenerImpl getScrollListener() {
    return scrollListener;
  }

  void setScrollListener(OnScrollListenerImpl scrollListener) {
    this.scrollListener = scrollListener;
  }

  PlayerManager getManager() {
    return manager;
  }

  void setManager(PlayerManager manager) {
    this.manager = manager;
  }

  ToroStrategy getStrategy() {
    return strategy;
  }

  void setStrategy(ToroStrategy strategy) {
    this.strategy = strategy;
  }

  void remove() throws Exception {
    this.manager.remove();
    this.scrollListener.remove();
  }
}
