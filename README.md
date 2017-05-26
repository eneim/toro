# Toro 

> ***Video list auto playback made simple, specially built for RecyclerView***

<a href="https://www.iconfinder.com/icons/1105270/brand_connect_shape_square_icon#size=512"><img src="/art/web_hi_res_512.png" width="256"></a>

<a href='https://ko-fi.com/A342OWW' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

[ ![Download](https://api.bintray.com/packages/eneim/Toro/toro/images/download.svg) ](https://bintray.com/eneim/Toro/toro/_latestVersion)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Toro-green.svg?style=true)](https://android-arsenal.com/details/1/3106)

[![Join the chat at https://gitter.im/eneim/Toro](https://badges.gitter.im/eneim/Toro.svg)](https://gitter.im/eneim/Toro?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 

<a href="http://www.methodscount.com/?lib=im.ene.toro2%3Atoro%3A2.2.0"><img src="https://img.shields.io/badge/Toro | Methods and size-core: 326 | deps: 10993 | 32 KB-e91e63.svg"/></a>

<a href="http://www.methodscount.com/?lib=im.ene.toro2%3Atoro-extended%3A2.2.0"><img src="https://img.shields.io/badge/Toro Extended | Methods and size-core: 186 | deps: 17172 | 17 KB-e91e63.svg"/></a>

<a href="http://www.methodscount.com/?lib=im.ene.toro2%3Atoro-ext-exoplayer2%3A2.2.0"><img src="https://img.shields.io/badge/Toro ExoPlayer 2 Extension | Methods and size-core: 321 | deps: 16851 | 31 KB-e91e63.svg"/></a>

<a href="http://www.methodscount.com/?lib=im.ene.toro2%3Atoro-ext-exoplayer%3A2.2.0"><img src="https://img.shields.io/badge/Toro ExoPlayer (1) Extension | Methods and size-core: 412 | deps: 15350 | 50 KB-e91e63.svg"/></a>

<a href="http://www.methodscount.com/?lib=im.ene.toro2%3Atoro-ext-mediaplayer%3A2.2.0"><img src="https://img.shields.io/badge/Toro Legacy MediaPlayer | Methods and size-core: 69 | deps: 11319 | 7 KB-e91e63.svg"/></a>

**!IMPORTANT: Method Count is based on 2.2.0 (due to some unknown issue, MethoudCount server doesn't process 2.3.0 ...)**

## Menu

- [0. Latest Version](#0-latest-version----)
- [1. Main Features](#1-main-features)
- [2. Toro in Action](#2-toro-in-action)
- [3. How to use](#3-how-to-use)
- [4. How to contribute to Toro](#4-how-to-contribute-to-toro)
- [5. Customize Toro for your own favor](#5-customize-toro-for-your-own-favor)
- [6. Hall of Fame](#6-hall-of-fame)

## 0. Latest Version:  [ ![Download](https://api.bintray.com/packages/eneim/Toro/toro/images/download.svg) ](https://bintray.com/eneim/Toro/toro/_latestVersion)

See [CHANGELOG.md](CHANGELOG.md) for more information.

## 1. Main features:

- Auto start media playback after RecyclerView layout has been laid out.
- Auto start/pause/resume media base on RecyclerView scroll state.
- Auto save latest media last playback position and resume from there later.
- Customizable playback behavior:
  - Selective playback by **ToroStrategy**: among many playable items, Strategy finds the best one to start playback.
  - UI-based approach: you see the Video, then it can play. **Default**: you see 75% of the Video then it can start playing. **Advance**: you decide how much the visible Video should trigger the playback. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.
  
- Powerful, flexible and highly customizable API. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Extensions: ExoPlayer (v2, v1), (legacy) Android MediaPlayer API.

  - Toro Extended: extension based on ExoPlayer 2, provides the concept of "Next Player to play" (abstract concept for loop/auto scroll, ...), and long click/press handling. See Wiki for more information.

- **Lots of built-in components, but Toro is still highly customizable.**

**See [Wiki](https://github.com/eneim/Toro/wiki) for more details.**

## 2. Toro in Action

| | |
|---|------|
| <img src="/art/sample_6.gif" width="648"> | <img src="/art/sample_1.gif" width="162"> <img src="/art/sample_2.gif" width="162"> <img src="/art/sample_3.gif" width="162"> <img src="/art/sample_4.gif" width="162"> <img src="/art/sample_5.gif" width="162"> <img src="/art/sample_7.gif" width="162"> |

## 3. How to use

##### 0. Pre-requirement

- Min support version: Android SDK level 16 (4.1.x)

##### 1. Add Toro to your project

- Make sure to have this in Project's top level ```build.gradle```

```groovy
allprojects {
	repositories {
    // Android Studio has this by default.
		jcenter()
	}
}
```

- Add this to dependencies

```groovy
ext {
	toroVersion = '2.3.1'
}

dependencies {

  // TODO !IMPORTANT: use only one of the following
  
  // include in your module (app or library)
  // include only core Toro library
  compile "im.ene.toro2:toro:${toroVersion}"

  // include Toro extended helper (Toro and ExoPlayer v2 extension is included already)
  compile "im.ene.toro2:toro-extended:${toroVersion}"
  
  // include extension for ExoPlayer v2 (Toro is included already)
  compile "im.ene.toro2:toro-ext-exoplayer2:${toroVersion}"
  
  // include extension for ExoPlayer v1 (Toro is included already)
  compile "im.ene.toro2:toro-ext-exoplayer:${toroVersion}"
  
  // include extension for Legacy MediaPlayer (Toro is included already)
  compile "im.ene.toro2:oro-ext-mediaplayer:${toroVersion}"
}
```

Latest version is always visible here: [![Download](https://api.bintray.com/packages/eneim/Toro/toro/images/download.svg) ](https://bintray.com/eneim/Toro/toro/_latestVersion)

##### **TL,DR**: **toro-sample** module from this library comes with several good practice of this library. Please take a look.

##### 2. Create ViewHolder to use with **Toro**: see [This Wiki](https://github.com/eneim/Toro/wiki/1.-Toro-in-Practice---A-Beginner-Guide) to see how to start implementing.

##### 3. Register/Unregister a RecyclerView to get support from **Toro**: see [Register/Unregister RecyclerView to Toro](https://github.com/eneim/Toro/wiki/0.-Toro-starting-guide#registerunregister-recyclerview-to-toro)

##### 4. Core concepts and components of **Toro**: see [Wiki](https://github.com/eneim/Toro/wiki)

## 4. How to contribute to Toro

- Issue report and PRs are welcome.

- Buy me some coffee for shorter update cycle ...

<a href='https://ko-fi.com/A342OWW' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

## 5. Customize Toro for your own favor

Toro is licensed under Apache License 2.0, so you are free to clone and modify it. Here is how:

##### 1. Clone this repository.
##### 2. Change name **gradle.properties-sample** to **gradle.properties**, open it and fill the dummy value by your own.

Please note that, sample App uses Fabric to catch crash at runtime, so you must provide a valid Fabric token. Also **make sure to not expose your token else where**.

##### 3. Run ```./gradlew :toro-sample:build``` to confirm your clone works.

##### 4. If you are cloning this on CI server, make sure to provide it a valid **gradle.properties** file at compile time.

## 6. Hall of Fame

*Use Toro in your App? <a href="mailto:nam@ene.im?subject=Hi Nam">Email me</a> with your **App name/link** to get promoted here.*

### License

> Copyright 2016 eneim@Eneim Labs, nam@ene.im

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at

>        http://www.apache.org/licenses/LICENSE-2.0

> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
