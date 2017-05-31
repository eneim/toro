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
import im.ene.toro.widget.Container;

/**
 * @author eneim | 5/31/17.
 */

public final class ToroHelper {

  @NonNull private final PlayerManager manager;
  private Strategy strategy;
  private Container container;

  public ToroHelper(@NonNull PlayerManager manager) {
    this(manager, null);
  }

  public ToroHelper(@NonNull PlayerManager manager, Strategy strategy) {
    this.manager = manager;
    this.strategy = strategy;
    this.container = null;
  }

  public void setStrategy(@NonNull Strategy strategy) {
    //noinspection ConstantConditions
    if (strategy == null) {
      throw new IllegalArgumentException("Strategy must not be null");
    }
    if (this.strategy == strategy) return;
    this.strategy = strategy;
    if (this.container != null) {
      this.container.setStrategy(this.strategy);
    }
  }

  public void registerContainer(@Nullable Container container) {
    if (this.strategy == null) {
      throw new IllegalStateException(
          "It is required to have a Strategy before registering any Container.");
    }

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
      this.container.setStrategy(this.strategy);
      this.container.setManager(this.manager);
    }
  }
}
