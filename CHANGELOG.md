# Release note

### 1.2.0 (2016.04.06)

Breaking changes. Clients should take care of this release.

- **[CHANGE]** Better error handling, see [this PR](https://github.com/eneim/Toro/pull/26). ToroPlayer#onPlaybackError(...) will now require a ***boolean response***.
- **[CHANGE]** **[NEW FEATURE]** Support ***loop playback***. Current playing **ToroPlayer** could automatically repeat **immediately** after it completes latest playback. Loop playback is disable by default. Setup by extending ```ToroPlayer#isLoopAble()``` and return ***true***. Sample usage:

```java
public class MyLoopAblePlayerViewHolder extends ToroViewHolder {

  // Do stuff
  
  // Support loop on this ViewHolder
  @Override public boolean isLoopAble() {
    return true;
    // You may want to control it better like following implementation
    // return getAdapterPosition() % 2 == 0;
  }
}
```
- **[NEW FEATURE]** Ability to temporary pause all playing players by new **ToroStrategy**: ***REST*** (*internal API, user has no direct access to this*). This Strategy will disable all player. This is useful for the cases when Client would like to open a Dialog (Eg: A big floating playback window) on top of current window, then it wants to pause the player until that Dialog dismisses. Usage (see sample App for more information):

```java
Toro.rest(true);  // Tell Toro you want to take a rest
// Do stuff which requires the player to be silent.
Toro.rest(false); // Done stuff, resume the playback.
```
Please note that using this feature should be careful. It's really simple implementation, for really simple usecase. 2 calls should always be coupled. So please don't screw up your App.

- **[NEW FEATURE]** Custom LayoutManager: Client can now add their own LayoutManager. The requirement is that LayoutManager must implement **ToroLayoutManager** interface to help Toro knows how to travel throw the Children. *Sample is coming soon*.

- **[INTERNAL CHANGE]** Custom ViewHolder with RecyclerViewItemHelper: RecyclerViewItemHelper is now public and non-final. This allows client to create their own ViewHolder just by implementing **ToroPlayer** interface, and still get support from Toro's core. Just use/extend RecyclerViewItemHelper wisely. *Sample is coming soon*.

- Better UI for sample App.

- **Many internal improvement**.

- **MIGRATING FROM 1.1.0 TO 1.2.0**: fix/re-code all your ```ToroPlayer#onPlaybackError```. If you have any problem, don't hesitate to create new issue or even notify my via [Gitter](https://gitter.im/eneim/Toro).

### 1.1.0 (2016.02.09)

**Happy Vietnam New Lunar Year!**

This release contains some changes from internal API as well as code improvement:

- Narrow down visibility of some classes and object. Clients should test carefully for side-affect. So sorry for this inconvenient. Normally you would not be affect, unless you have some custom components.
  - VideoPlayerManagerImpl is now package private. Don't use it else where. Instead, implement to your Adapter.
  - ToroScrollListener is now package private. Don't use it else where. 

- Built-in strategies is strengthen by more accurate decision on ```#allowsToPlay()``` method.

- Some other code improvement.
- 
### 1.0.0 (2016.02.06)

- First stable release
