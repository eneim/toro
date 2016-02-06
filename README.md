# Toro

<img src="https://github.com/eneim/Toro/blob/master/art/web_hi_res_512.png" width="256">

[![](https://jitpack.io/v/eneim/Toro.svg)](https://jitpack.io/#eneim/Toro)


> ***Video List by RecyclerView made simple. Auto play/pause supported with smart caching last playing position.***

## Introduction and motivation

- I wanted to apply ***Auto playback on scrolling*** features like Facebook App or Twitter App into my app. But there's not any good solution out there, and even those which are trying to solve the same problem, I did not find them useful, so I create this with the following targets:
  - Easy to use, support modern components from Android's support libraries. Target Android SDK from 16 up (I hate the rest, sorry).
  - Support RecyclerView by adding support layers on tops of its LayoutManager/Adapter/ViewHolder.
  - **Keep the code base transparent with original RecyclerView**. Provide useful methods set on top of them. See [ToroAdapter](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/ToroAdapter.java), [ToroAdapter$ViewHolder](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/ToroAdapter.java#L59).
  - **OUT-OF-THE-BOX support**: User can just extends my pre-code classes, I have prepared the rest for them. See [ToroVideoViewHolder]() and its relatives
  - Highly cusomizeable.
  - Take care of possible issues: Video playing is a pain in Android. I wish Toro could help its users feel good. So Toro listen to Activity's life cycle to init/release its resource. Further more, Toro creates and supports **Playback Lifecycle** which is super helpful for those who want to catch up with the playback progress as well as update UI before/after playing. All of those features are usable out of the box.
  - Super easy to setup: see usage section.

- There are some other [working ideas](https://github.com/danylovolokh/VideoPlayerManager), but those libraries have different approaches, which are in **my oppinion**, not as good as I expected: User of those libraries need to re-write a lot of code to match their implementation. I try to make our components as close to official **RecyclerView/Adapter/ViewHolder/LayoutManager** as possible, and just provide some useful methods on top of them. 

- I drop supporting for ListView/GridView since those components are obsolete and hard to maintaince. But I made a on-going branch which provide some code base with the same thinking with the rest of this library. So any one can take a look and create their own helpers.

## Main features:

- Out of the box support for RecyclerView, either being used with LinearLayoutManager or StaggeredGridLayoutManager.

- Powerful, yet flexible and highly customizable: there are a lot of powerful built-in API, but the core interfaces are easy to customize. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Powerful built-in APIs, support auto play/pause and caching last played position. (**note that: in Android default Media Player, depend on Video's format and codec, the resume timestamp may varies**). 

- Smart, flexible [Strategies](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/ToroStrategy.java) to decide when and how a player should start playing. Toro comes with optimized built-in [Strategies](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/Toro.java#L516), but user could always create their own. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Smartly support Grid (with many Video in one window) by built-in [Long press listener](https://github.com/eneim/Toro/blob/master/toro/src/main/java/im/ene/lab/toro/Toro.java#L108). Turning ON/OFF in one line of code. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.
 
- **I create lots of built-in code, but keep Toro highly customizable.** You are free to decide how you want to start your player, but if you don't know, just let Toro help you to decide.

**See [Wiki](https://github.com/eneim/Toro/wiki) for more details.**

## Toro in Action

<img src="https://github.com/eneim/Toro/blob/master/art/sample_1.gif" width="180">
<img src="https://github.com/eneim/Toro/blob/master/art/sample_2.gif" width="180">
<img src="https://github.com/eneim/Toro/blob/master/art/sample_4.gif" width="180">
<img src="https://github.com/eneim/Toro/blob/master/art/sample_5.gif" width="180">

<img src="https://github.com/eneim/Toro/blob/master/art/sample_3.gif" width="360">

## How to use

##### 0. Add Toro to your project

- Add this to Project's top level ```build.gradle```

```
allprojects {
	repositories {
		maven { url "https://jitpack.io" }
	}
}
```

- Add this to dependencies

```
dependencies {
	compile 'com.github.eneim:Toro:1.0.0'
}
```

##### **TL,DR**: **app** module from this library comes with several good practice of this library. Please take a look.

##### 1. Integrate **Toro** into your Application: see [Toro starting guide](https://github.com/eneim/Toro/wiki/1.-Toro-starting-guide)

##### 2. Register/Unregister a RecyclerView to get support from **Toro**: see [Register/Unregister RecyclerView to Toro](https://github.com/eneim/Toro/wiki/1.-Toro-starting-guide#registerunregister-recyclerview-to-toro)

##### 3. Create ViewHolder to use with **Toro**: by default, just simply extend one of ```ToroVideoViewHolder```, ```TextureVideoViewHolder``` or ```AbsVideoViewHolder```. See [ToroViewHolder](https://github.com/eneim/Toro/wiki/2.-ToroPlayer,-ToroAdapter,-ToroViewHolder:-Toro's-heart(s)#toroviewholder) for more information.

  - A sample ViewHolder's code (see Sample app for more):
  
```
public class DeadlySimpleToroVideoViewHolder extends ToroVideoViewHolder {

  public DeadlySimpleToroVideoViewHolder(View itemView) {
    super(itemView);
  }

  @Override protected ToroVideoView findVideoView(View itemView) {
    return (ToroVideoView) itemView.findViewById(R.id.video);
  }

  @Nullable @Override public String getVideoId() {
    return "my awesome video's id and its order: " + getAdapterPosition();
  }

  @Override public void bind(@Nullable Object object) {
    if (object != null && object instanceof SimpleVideoObject) {
      mVideoView.setVideoPath(((SimpleVideoObject) object).video);
    }
  }
}
```

##### 4. Core concepts and components of **Toro**: see [Wiki](https://github.com/eneim/Toro/wiki)

### Contribute to Toro

- Composing... please wait.

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
