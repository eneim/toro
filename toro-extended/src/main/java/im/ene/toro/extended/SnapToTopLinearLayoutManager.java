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

package im.ene.toro.extended;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by eneim on 10/7/16.
 */
// Ref: http://stackoverflow.com/a/32819067/1553254
public class SnapToTopLinearLayoutManager extends LinearLayoutManager {

  public SnapToTopLinearLayoutManager(Context context) {
    super(context);
  }

  public SnapToTopLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
    super(context, orientation, reverseLayout);
  }

  public SnapToTopLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
      int position) {
    RecyclerView.SmoothScroller smoothScroller =
        new TopSnappedSmoothScroller(recyclerView.getContext());
    smoothScroller.setTargetPosition(position);
    startSmoothScroll(smoothScroller);
  }
}
