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

package im.ene.toro;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;

/**
 * Created by eneim on 6/8/16.
 *
 * Support ViewPager with Pages as 'Video List', not Single Video item.
 * 
 * The following snippet from {@link ViewPager#onLayout(boolean, int, int, int, int)}
 *
 * <code>
 * if (mFirstLayout) {
 *    scrollToItem(mCurItem, false, 0, false);
 * }
 * </code>
 *
 * Gives us a clue that on very first layout pass,
 * {@link ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)} will be triggered.
 */
public class ToroViewPagerHelper extends ViewPager.SimpleOnPageChangeListener
    implements Removable, Handler.Callback {

  private static final int MSG_LAYOUT_STABLE = -100;  // Negative, to prevent conflict
  private static final int MSG_DELAY = 50;  // Reasonable and relatively short delay

  private Handler handler;
  boolean firstScroll = true;

  public ToroViewPagerHelper() {
    this.handler = new Handler(Looper.getMainLooper(), this);
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    if (firstScroll) {
      handler.removeMessages(MSG_LAYOUT_STABLE);
      handler.sendEmptyMessageDelayed(MSG_LAYOUT_STABLE, MSG_DELAY);
      firstScroll = false;
    }
  }

  @Override public void onPageScrollStateChanged(int state) {
    if (state == ViewPager.SCROLL_STATE_IDLE) {
      handler.removeMessages(MSG_LAYOUT_STABLE);
      handler.sendEmptyMessageDelayed(MSG_LAYOUT_STABLE, MSG_DELAY);
    }
  }

  @Override public void remove() {
    this.handler.removeCallbacksAndMessages(null);
    this.handler = null;
  }

  @Override public boolean handleMessage(Message msg) {
    if (msg.what == MSG_LAYOUT_STABLE) {
      // Question: Is this a bad practice?
      Toro.rest(true);
      Toro.rest(false);
      return true;
    }

    return false;
  }
}
