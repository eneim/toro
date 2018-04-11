/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package toro.demo.exoplayer.basic

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import im.ene.toro.CacheManager
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers.io
import kotlinx.android.synthetic.main.activity_basic_list.toolbar
import kotlinx.android.synthetic.main.activity_basic_list.toolbar_layout
import kotlinx.android.synthetic.main.content_basic_list.container
import toro.demo.exoplayer.R

class BasicListActivity : AppCompatActivity() {

  private val adapter = SectionContentAdapter()
  private val disposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_basic_list)
    setSupportActionBar(toolbar)
    toolbar_layout.title = "Material Motion"

    container.apply {
      adapter = this@BasicListActivity.adapter
      layoutManager = LinearLayoutManager(this@BasicListActivity)
      cacheManager = CacheManager.DEFAULT
    }

    disposable.add(Motion.contents()
        .subscribeOn(io()).observeOn(mainThread())
        .doOnNext { it -> adapter.updateElements(it) }
        .subscribe()
    )
  }

  override fun onDestroy() {
    super.onDestroy()
    disposable.clear()
  }
}
