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

package im.ene.toro.sample.features.stateful;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import im.ene.toro.widget.Container;

/**
 * @author eneim | 6/10/17.
 */

public class MyContainer extends Container {

  public MyContainer(Context context) {
    super(context);
  }

  public MyContainer(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public MyContainer(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override protected Parcelable onSaveInstanceState() {
    SavedState savedState = new SavedState(super.onSaveInstanceState());
    LayoutManager layoutManager = getLayoutManager();
    int pos = -1;
    if (layoutManager instanceof LinearLayoutManager) {
      pos = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
      int spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
      pos = ((StaggeredGridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPositions(
          new int[spanCount + 1])[0];
    } else {
      if (getChildCount() > 0) {
        pos = 0;
      }
    }
    savedState.position = pos;
    return savedState;
  }

  @Override protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    int position = savedState.position;
    scrollToPosition(position);
  }

  @SuppressWarnings("WeakerAccess") public static class SavedState extends AbsSavedState {

    int position;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    SavedState(Parcel source, ClassLoader loader) {
      super(source, loader);
      position = source.readInt();
    }

    public static Creator<SavedState> CREATOR =
        ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
          @Override public SavedState createFromParcel(Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Override public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        });
  }
}
