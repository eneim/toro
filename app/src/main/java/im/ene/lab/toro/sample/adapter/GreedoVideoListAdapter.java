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

package im.ene.lab.toro.sample.adapter;

import android.support.annotation.Nullable;
import com.fivehundredpx.greedolayout.GreedoLayoutSizeCalculator;
import im.ene.lab.toro.sample.data.VideoSource;

/**
 * Created by eneim on 2/3/16.
 */
public class GreedoVideoListAdapter extends BaseSampleAdapter
    implements GreedoLayoutSizeCalculator.SizeCalculatorDelegate {

  public GreedoVideoListAdapter() {
    super();
  }

  private static final double[] RATIOS = new double[VideoSource.SOURCES.length];

  static {
    RATIOS[0] = 16 / (double) 9;
    RATIOS[1] = 1;
    RATIOS[2] = 16 / (double) 9;
    RATIOS[3] = 9 / (double) 16;
  }

  @Nullable @Override protected Object getItem(int position) {
    return mVideos.get(position % mVideos.size());
  }

  @Override public int getItemViewType(int position) {
    return VIEW_TYPE_VIDEO;
  }

  @Override public double aspectRatioForIndex(int i) {
    return RATIOS[i % RATIOS.length];
  }
}
