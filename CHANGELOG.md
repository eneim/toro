# Release note

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
