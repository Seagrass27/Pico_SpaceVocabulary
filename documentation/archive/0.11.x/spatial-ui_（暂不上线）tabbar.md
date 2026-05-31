TabBar is a navigation control derived from the system widget Augment and extended via a container, helping users switch between different modules within the application. You can choose to place it at the top or on the left based on application requirements. Eye-tracking interaction is supported by default. Viewpoint switching is available in the Volume window, which rotates to follow the observer's orientation and keeps the navigation bar facing the user.
![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/f8b9f37ea38446f09dbadbe187ea1d01~tplv-goo7wpa0wc-image.image)
## API Surface

* TabBar
   * `placement`: TabBar position setting: can be set to Top, Left, or Right.
   * `followViewpoints`: TabBar supports a set of viewpoints. When the window is of Form type, the supported viewpoints are Front, Right, Back, and Left.
   * `content`: TabBar content, limited to TabBarScope.
* TabBarScope
   TabBarScope includes the item method, with the following parameters:
   * `item`: The item title.
   * `Selected`: Whether the item is selected.
   * `icon`: Item icon, Icon type. Displayed only when the TabBar is positioned at the side of the window.
   * `unreadCount`: Displays the unread count under this item. The count is shown when it is greater than 0, and hidden when it is less than or equal to 0.

## Basic usage
```Kotlin
@Composable
fun TabBarSamples() {
    val items = listOf("Home", "Favourite", "Settings", "Mine", "More")
    var currentIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    TabBar(
        // Specify TabBar position
        placement = TabBarPlacement.Left,
        // Set viewpoint
        followViewpoints = setOf(ViewPoint.Front, ViewPoint.Left),
    ) {
        items.forEachIndexed { index, item ->
            // Declare information for a single item
            item(
                text = item,
                selected = currentIndex == index,
                //S et the click handler for a single item to toggle the selected item
                modifier = Modifier.clickable(onClick = { currentIndex = index }),
                icon = { painterResource(R.drawable.ic_sample_listitem_leading) }, 
                unreadCount = 9
            )
        }
    }
}
```

<div style="text-align: center"><img src="https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/a258c00e67614a339ff99a096aff3268~tplv-goo7wpa0wc-image.image" width="2560px" /></div>


