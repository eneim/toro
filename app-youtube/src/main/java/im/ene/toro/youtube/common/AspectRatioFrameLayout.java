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

package im.ene.toro.youtube.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.IntDef;
import im.ene.toro.youtube.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Copy from ExoPlayer source code. Because we don't want to depend on the lib just for this class.
 *
 * @author eneim (2017/11/23).
 */

public class AspectRatioFrameLayout extends FrameLayout {

  /**
   * Resize modes for {@link AspectRatioFrameLayout}.
   */
  @SuppressWarnings("WeakerAccess") @Retention(RetentionPolicy.SOURCE) @IntDef({
      RESIZE_MODE_FIT, RESIZE_MODE_FIXED_WIDTH, RESIZE_MODE_FIXED_HEIGHT, RESIZE_MODE_FILL,
      RESIZE_MODE_ZOOM
  }) public @interface ResizeMode {
  }

  /**
   * Either the width or height is decreased to obtain the desired aspect ratio.
   */
  public static final int RESIZE_MODE_FIT = 0;
  /**
   * The width is fixed and the height is increased or decreased to obtain the desired aspect ratio.
   */
  public static final int RESIZE_MODE_FIXED_WIDTH = 1;
  /**
   * The height is fixed and the width is increased or decreased to obtain the desired aspect ratio.
   */
  public static final int RESIZE_MODE_FIXED_HEIGHT = 2;
  /**
   * The specified aspect ratio is ignored.
   */
  public static final int RESIZE_MODE_FILL = 3;
  /**
   * Either the width or height is increased to obtain the desired aspect ratio.
   */
  public static final int RESIZE_MODE_ZOOM = 4;

  /**
   * The {@link FrameLayout} will not resize itself if the fractional difference between its natural
   * aspect ratio and the requested aspect ratio falls below this threshold.
   * <p>
   * This tolerance allows the view to occupy the whole of the screen when the requested aspect
   * ratio is very close, but not exactly equal to, the aspect ratio of the screen. This may reduce
   * the number of view layers that need to be composited by the underlying system, which can help
   * to reduce power consumption.
   */
  private static final float MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f;

  private float videoAspectRatio;
  private int resizeMode;

  public AspectRatioFrameLayout(Context context) {
    this(context, null);
  }

  public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    resizeMode = RESIZE_MODE_FIT;
    if (attrs != null) {
      TypedArray a = context.getTheme()
          .obtainStyledAttributes(attrs, R.styleable.AspectRatioFrameLayout, 0, 0);
      try {
        resizeMode = a.getInt(R.styleable.AspectRatioFrameLayout_resize_mode, RESIZE_MODE_FIT);
      } finally {
        a.recycle();
      }
    }
  }

  /**
   * Sets the aspect ratio that this view should satisfy.
   *
   * @param widthHeightRatio The width to height ratio.
   */
  public void setAspectRatio(float widthHeightRatio) {
    if (this.videoAspectRatio != widthHeightRatio) {
      this.videoAspectRatio = widthHeightRatio;
      requestLayout();
    }
  }

  /**
   * Returns the resize mode.
   */
  public @ResizeMode int getResizeMode() {
    return resizeMode;
  }

  /**
   * Sets the resize mode.
   *
   * @param resizeMode The resize mode.
   */
  public void setResizeMode(@ResizeMode int resizeMode) {
    if (this.resizeMode != resizeMode) {
      this.resizeMode = resizeMode;
      requestLayout();
    }
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (resizeMode == RESIZE_MODE_FILL || videoAspectRatio <= 0) {
      // Aspect ratio not set.
      return;
    }

    int width = getMeasuredWidth();
    int height = getMeasuredHeight();
    float viewAspectRatio = (float) width / height;
    float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
    if (Math.abs(aspectDeformation) <= MAX_ASPECT_RATIO_DEFORMATION_FRACTION) {
      // We're within the allowed tolerance.
      return;
    }

    switch (resizeMode) {
      case RESIZE_MODE_FIXED_WIDTH:
        height = (int) (width / videoAspectRatio);
        break;
      case RESIZE_MODE_FIXED_HEIGHT:
        width = (int) (height * videoAspectRatio);
        break;
      case RESIZE_MODE_ZOOM:
        if (aspectDeformation > 0) {
          width = (int) (height * videoAspectRatio);
        } else {
          height = (int) (width / videoAspectRatio);
        }
        break;
      default:
        if (aspectDeformation > 0) {
          height = (int) (width / videoAspectRatio);
        } else {
          width = (int) (height * videoAspectRatio);
        }
        break;
    }
    super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
  }
}
