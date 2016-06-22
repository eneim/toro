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

package im.ene.lab.toro.ext.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

/**
 * Created by eneim on 6/23/16.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)  //
public class SeekBarICS extends AppCompatSeekBar {

  private Drawable thumbDrawable;

  public SeekBarICS(Context context) {
    super(context);
  }

  public SeekBarICS(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SeekBarICS(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override public void setThumb(Drawable thumb) {
    super.setThumb(thumb);
    this.thumbDrawable = thumb;
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)  //
  public Drawable getThumbV15() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      return this.thumbDrawable;
    }
    // Still need to fallback to super.
    return super.getThumb();
  }
}
