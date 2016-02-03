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

package im.ene.lab.toro;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by eneim on 1/30/16.
 */
public abstract class ToroAdapter<VH extends ToroAdapter.ViewHolder>
    extends RecyclerView.Adapter<VH> {

  @CallSuper @Override public void onViewRecycled(VH holder) {
    holder.onRecycled();
  }

  @CallSuper @Override public boolean onFailedToRecycleView(VH holder) {
    return holder.onFailedToRecycle();
  }

  @CallSuper @Override public void onViewAttachedToWindow(VH holder) {
    holder.onAttachedToParent();
  }

  @CallSuper @Override public void onViewDetachedFromWindow(VH holder) {
    holder.onDetachedFromParent();
  }

  @CallSuper @Override public void onBindViewHolder(VH holder, int position) {
    holder.bind(getItem(position));
    holder.onViewHolderBound();
  }

  /**
   * Require client to feed data. Actually object returned from this method could be null.
   */
  @Nullable protected abstract Object getItem(int position);

  /**
   *
   */
  public abstract static class ViewHolder extends RecyclerView.ViewHolder {

    private final String TAG = getClass().getSimpleName();

    public ViewHolder(View itemView) {
      super(itemView);
    }

    /**
     * Client can setup long click listener from inside viewHolder
     */
    public void setOnItemLongClickListener(View.OnLongClickListener listener) {
      itemView.setOnLongClickListener(listener);
    }

    /**
     * Called by {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
     */
    @CallSuper public void onAttachedToParent() {

    }

    /**
     * Called by {@link RecyclerView.Adapter#onDetachedFromRecyclerView(RecyclerView)}
     */
    @CallSuper public void onDetachedFromParent() {

    }

    /**
     * Called by {@link RecyclerView.Adapter#onViewRecycled(RecyclerView.ViewHolder)}
     */
    @CallSuper public void onRecycled() {

    }

    /**
     * Called by {@link RecyclerView.Adapter#onFailedToRecycleView(RecyclerView.ViewHolder)}
     */
    @CallSuper public boolean onFailedToRecycle() {
      return false;
    }

    /**
     * Called response to {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder,
     * int)}
     */
    @CallSuper public void onViewHolderBound() {

    }

    /**
     * Accept null object, but client must acknowledge this, and try to supply valid object to
     * ViewHolder
     */
    public abstract void bind(@Nullable Object object);
  }
}
