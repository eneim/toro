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

package com.google.android.libraries.mediaframework.layeredvideo;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


/**
 * Contains utility functions which are used by a number of other classes.
 */
public class Util {

  /**
   * Create a LayoutParams object for the given view which enforces a given width and height.
   *
   * <p>This method is a bit complicated because the TYPE of the LayoutParams that a view must
   * receive (ex. LinearLayout.LayoutParams, RelativeLayout.LayoutParams) depends on the type of its
   * PARENT view.
   *
   * <p>Thus, in this method, we look at the parent view of the given view, determine its type,
   * and create the appropriate LayoutParams for that type.
   *
   * <p>This method only supports views which are nested inside a FrameLayout, LinearLayout, or
   * GridLayout.
   */
  public static ViewGroup.LayoutParams getLayoutParamsBasedOnParent(View view, int width, int height)
      throws IllegalArgumentException {

    // Get the parent of the given view.
    ViewParent parent = view.getParent();

    // Determine what is the parent's type and return the appropriate type of LayoutParams.
    if (parent instanceof FrameLayout) {
      return new FrameLayout.LayoutParams(width, height);
    }
    if (parent instanceof RelativeLayout) {
      return new RelativeLayout.LayoutParams(width, height);
    }
    if (parent instanceof LinearLayout) {
      return new LinearLayout.LayoutParams(width, height);
    }

    // Throw this exception if the parent is not the correct type.
    IllegalArgumentException exception = new IllegalArgumentException("The PARENT of a " +
        "FrameLayout container used by the GoogleMediaFramework must be a LinearLayout, " +
        "FrameLayout, or RelativeLayout. Please ensure that the container is inside one of these " +
        "three supported view groups.");

    // If the parent is not one of the supported types, throw our exception.
    throw exception;
  }
}
