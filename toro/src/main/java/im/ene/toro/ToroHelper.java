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

package im.ene.toro;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import im.ene.toro.widget.Container;

/**
 * @author eneim | 5/31/17.
 */

public final class ToroHelper {

  private static final String TAG = "ToroLib:Helper";

  @NonNull private final PlayerManager manager;
  @Nullable private Selector selector;
  private Container container;

  @SuppressWarnings("unused") public ToroHelper(@NonNull PlayerManager manager) {
    this(manager, null);
  }

  public ToroHelper(@NonNull PlayerManager manager, @Nullable Selector selector) {
    this.manager = manager;
    this.selector = selector;
  }

  public void registerContainer(@Nullable Container container) {
    Log.e(TAG, "registerContainer() called with: container = [" + container + "]");
    if (this.container == container) return;
    if (this.container != null) {
      this.container.setManager(null);
    }

    this.container = container;
    if (this.container != null) {
      if (this.container.getManager() != null) {
        throw new IllegalStateException(
            "This Container has already been registered to another Helper.");
      }
      this.container.setSelector(this.selector);
      this.container.setManager(this.manager);
    }
  }

  public void setSelector(@Nullable Selector selector) {
    if (this.selector == selector) return;
    this.selector = selector;
    if (this.container != null) {
      this.container.setSelector(this.selector);
    }
  }

  @Nullable public Selector getSelector() {
    return selector;
  }

  @NonNull public PlayerManager getManager() {
    return manager;
  }
}
