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

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by eneim on 1/30/16.
 *
 * Basic implementation/extension of {@link RecyclerView.Adapter} to have support from Toro.
 */
public abstract class ToroAdapter<VH extends ToroAdapter.ViewHolder>
    extends RecyclerView.Adapter<VH> {

  public ToroAdapter() {
    super();
    setHasStableIds(true);
  }

  @CallSuper @Override public void onViewRecycled(VH holder) {
    holder.onRecycled();
  }

  @CallSuper @Override public boolean onFailedToRecycleView(VH holder) {
    return holder.onFailedToRecycle();
  }

  @CallSuper @Override public void onViewAttachedToWindow(VH holder) {
    holder.onAttachedToWindow();
  }

  @CallSuper @Override public void onViewDetachedFromWindow(VH holder) {
    holder.onDetachedFromWindow();
  }

  @CallSuper @Override public void onBindViewHolder(VH holder, int position) {
    holder.bind(this, getItem(position));
    holder.onViewHolderBound();
  }

  // Toro requires this method to return item's unique Id.
  @Override public long getItemId(int position) {
    return position;
  }

  /**
   * Require client to feed data. Actually object returned from this method could be null.
   *
   * @param position position of the object this Adapter will obtain
   * @return the Object at the position of {@code position} in this Adapter, can be {@code null}.
   */
  @Nullable protected abstract Object getItem(int position);

  public abstract static class ViewHolder extends RecyclerView.ViewHolder {

    public ViewHolder(View itemView) {
      super(itemView);
    }

    /**
     * Required for {@link PlayerViewHelper#onAttachedToWindow()}. See {@link
     * RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
     */
    public abstract void onAttachedToWindow();

    /**
     * Required for {@link PlayerViewHelper#onDetachedFromWindow()}. See {@link
     * RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)}
     */
    public abstract void onDetachedFromWindow();

    /**
     * Client can setup long click listener from inside viewHolder
     *
     * @param listener the long click event listener
     */
    public void setOnItemLongClickListener(View.OnLongClickListener listener) {
      itemView.setOnLongClickListener(listener);
    }

    /**
     * Setup on click listener to current ViewHolder's components.
     *
     * @param listener the click event listener
     */
    public void setOnItemClickListener(View.OnClickListener listener) {
      itemView.setOnClickListener(listener);
    }

    /**
     * Called by {@link RecyclerView.Adapter#onViewRecycled(RecyclerView.ViewHolder)}
     */
    protected void onRecycled() {
    }

    /**
     * Called by {@link RecyclerView.Adapter#onFailedToRecycleView(RecyclerView.ViewHolder)}
     *
     * @return True if the View should be recycled, false otherwise. Note that if this method
     * returns <code>true</code>, RecyclerView <em>will ignore</em> the transient state of
     * the View and recycle it regardless. If this method returns <code>false</code>,
     * RecyclerView will check the View's transient state again before giving a final decision.
     * Default implementation returns false.
     */
    protected boolean onFailedToRecycle() {
      return false;
    }

    /**
     * Called response to {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder,
     * int)}
     */
    protected void onViewHolderBound() {
    }

    /**
     * Accept null object, but client must acknowledge this, and try to supply valid object to
     * ViewHolder
     *
     * @param adapter the adapter that is using this ViewHolder
     * @param object the item to be bound to this ViewHolder
     */
    public abstract void bind(RecyclerView.Adapter adapter, @Nullable Object object);
  }
}
