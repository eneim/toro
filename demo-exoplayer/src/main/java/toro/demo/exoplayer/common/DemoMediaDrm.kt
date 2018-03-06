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

package toro.demo.exoplayer.common

import im.ene.toro.media.DrmMedia

data class DemoMediaDrm(    //
        private val _type: String,  //
        private val _licenseUrl: String?,   //
        private val _multiSession: Boolean) : DrmMedia { //
    override fun getLicenseUrl() = _licenseUrl
    override fun getType() = _type
    override fun getKeyRequestPropertiesArray(): Array<String>? = null
    override fun multiSession() = _multiSession
}