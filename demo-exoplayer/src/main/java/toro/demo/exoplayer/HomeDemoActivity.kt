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

package toro.demo.exoplayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * @author eneim (2018/02/07).
 *
 * Demo Activity, entry point for user to select which one he/she wants to try.
 * Selectable choices include a demo that uses @see [im.ene.toro.exoplayer.Playable], a demo
 * that use @see [im.ene.toro.exoplayer.ExoCreator] and one demo uses @see [im.ene.toro.widget.Container].
 */
class HomeDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling_view)
    }
}