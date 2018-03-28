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

import com.squareup.moshi.Json

/**
 * @author eneim (2018/03/05).
 */
data class VideoItem(   //
    @Json(name = "name") val name: String?,                         // name
    @Json(name = "uri") val uri: String?,                           // uri
    @Json(name = "extension") val extension: String?,               // extension
    @Json(name = "drm_scheme") val drmScheme: String?,              // drm_scheme
    @Json(name = "drm_license_url") val drmLicenseUrl: String?,     // drm_license_url
    @Json(name = "ad_tag_uri") val adTagUri: String?,               // ad_tag_uri
    @Json(name = "playlist") val playlist: List<VideoItem>?         // playlist
)