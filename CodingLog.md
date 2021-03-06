## Coding Log
The first problem worth recording is that how to load images while sample the images
to reduce memory consumption. The first phase of this gallery app is to provide a photo 
wall with images from the eternal space. The main problem is to make an adaptive adapter to 
supply image views with proper size which we called thumbnail size.  The absolute values of the image view's width
and height is specified by hard coding, thus **the quantity of columns** is dependent on the size of the 
screen which will be measured before the inflation of each image view. 

Before the global layout process, image views will be inflated once. The height and width of our image view 
are still both zero because their exact value is not determined. Therefore, the image views must be 
correctly inflated with target image sampled after the global layout process is ended. I didn't figure out this relationship at first and sample
the target images before the global layout and make all images become **pure color**.  

Jan 20th, 2020

The next step of this project is to add image source: network and external storage.
Firstly we consider how to load all images on the external storage and build appropriate image
loader.

现在考虑如何从本地存储设备上加载所有图片，还是应该用CursorAdapater ，给出合理的loader，使用
异步任务去执行具体的加载，加载完成后调用notification。

此时的Fragment需要实现某个Callback的接口。

Jan 23th, 2020

The former CursorAdapter does not fit this situation because it can not handle the image stuff. Instead, I create
a new adapter extends BaseAdapter to provide views. To acquire all images in external storage, I just use a simple 
cursor with MediaStore.Images.Media.EXTERNAL_CONTENT_URI. Through the cursor, each bitmap's id can be acquired
and thus its specific uri can be built.
通过每张图片的uri，可以获得其输入流，进而利用BitmapFactory.decodeByteArray解析从输入流获得的字节数组，因此
得到Bitmap。

>这里遇到一个问题是，Android的BitmapFactory.decodeStream不能直接解析从uri获得的输入流，得到的总是null。
而通过输入流获取字节数组在进行解析才可以正常解码。 这里是framework层的一个bug。
>

接下来的任务，解决加载时间过长的问题，这个可以用异步加载来解决。具体的实现方案是，暂时放置一个中间过程图，然后通过线程池和异步任务
的组合来完成这件事情。

当前app存在的问题是**上下滑动会很卡**，这个问题如何解决？

>遭遇问题：在commit一个fragment之前，假如调用了addToBackStack方法，意味着宿主Activity要保存其状态。commit之后会丢失状态，因此必须在此
之前保存状态。所以，如果有保存状态的需求，就不能调用commitNow方法，否则会抛出异常。

Jan 26th, 2020

解决之前**上下滑动会很卡**的问题，通过采用异步加载的方式，是避免同步加载的动作在UI线程中花费太多的
时间。在图片较多，ImageView数量同样增长的情况下，使用同步加载很可能会导致app的响应时间过长。

在这里的异步加载实现方式主要就是将加载和压缩的工作交由ImageFetcher和ImageLoader来执行，其中
ImageFetcher是留给Activity的加载接口。 Fetcher会首先设置ImageView的图片为一张空白图，然后将
加载图片的实际任务交给AsyncTask对象来执行。在其中的doInBackground调用ImageLoader执行加载，在onPostExecute
中设置Bitmap。

值得注意的是AsyncTask要获取对相应ImageView的引用才能设置图片，而AsyncTask可能被取消，所以避免
使用*强引用*而采用WeakReference获取ImageView。

还要注意这里的AsyncTask是一个非静态内部类，可能会影响垃圾回收。

>遭遇问题：首次加载所有图片时，会有重排序的现象出现——占据屏幕的视图对象会重新排列一次，之前的位置会被打乱。
这个原因需要找到。
问题已经解决，就是adapter中的判断条件的问题，两次触发了对image的加载工作。但问题是为什么会重新排序？

Jan 27th, 2020

>遭遇问题：怎么复用ImageView，避免上下滑动的时候重新加载Bitmap？

接下来的工作要实现ImageView的复用，并且增加放大图的功能。
先处理增加一级缓存的功能，这样可以避免每次都通过uri到外部存储去加载图片。

增加了一级缓存之后效果好了很多，跟不需要重新从磁盘上加载图片，速度快了很多。

Jan 28th, 2020

当前子任务是研究放大图的呈现功能，点击缩略图显示放大之后的图片。首先看示例是如何工作的。

Jan 29th, 2020

ViewPager和ActionBar通常配合使用。一般而言，用ViewPager将每一页设置为全屏模式，通过点击将ActionBar
显示或者隐藏。在Activity中设置点击监听，只要发生点击事件，那么就检查Pager.SystemUiVisibility的状态，
将其设置为相反的可见或隐藏。

>遭遇问题：
1、首次进入照片墙，有的照片位置留白，上下刷几次之后才能正常加载;
2、导航栏的返回按钮效果不佳，要重新加载，应该修改成跟按实际的返回栏按钮一个效果;
3、进入大图模式之后不能左右滑，否则会退出大图模式。

今天主要实现了ImageDetailActivity和ImageDetailFragment的基本功能。完善了ImageFetcher的
监听器，扩展了Fetcher得到专门用于External storage的fetcher。

特别注意到，fragment的参数传递方式是setArguments方法。由于fragment在遇到类似于横屏之类的事件重绘
时，会调用默认构造器，使用构造器传入的参数会被抹去，而之前的参数就保留在savedInstanceStates中。

Jan 30th, 2020

先尝试解决用异步方法加载图片会出现有的照片加载不出来导致对应位置留白的问题。将加载方式换成同步加载，这个
问题就得到解决，因此问题应该在于异步的图片加载任务有的被放弃了。

记录：当前排查发现没有任务被cancel

>遭遇问题，点击小图与随后显示的大图不一致。

problem solved: the grid activity's cursor do not match the detail activity's one
因为用到了两个Cursor，所以要保持大图与小图的一致性，必须保证两个cursor相同！尤其是**排序方式**！

进入大图不能左右滑动的问题同样得到了解决。原因在于ViewPager在调用属于ViewGroup的addViewInner
方法为自己添加一个View的时候，会检查这个view是否有父View，如果有则抛出异常，即使这个父View就是这个
ViewGroup本身。在viewPager的左右滑动过程中，会涉及view的从属关系变化，应该将要返回的view的父view
挂空，才能够避免在viewGroup检查的时候出错。
这个过程还值得深入探究。

Jan 31st, 2020

>问题网格视图有空洞还没有解决，总结一下尝试过的点：弱引用丢失、ImageView不可见、是否加入缓存。
这些问题都不影响。可以看到，这两个view是存在的，因为将它的背景颜色设置后可以看到颜色变化。

<u>Feb 4th, 2020</u>

之前的bug先放下，今天处理新问题——手势的处理。给大图添加手势的管理和操作。

子任务1：监听双击大图的手势;

子任务2：将大图再放大;

子任务3：双击放大图片。

<u>Feb 5th, 2020</u>

研究缩放动画。

<u>Feb 6th, 2020</u>

卡在一个接口的问题上，忽略了图片的缩放模式设置。想要利用matrix进行缩放，必须将缩放模式设置为
ScaleType.Matrix。之后通过动画生成器，不断改变matrix的值，并不断调用invalidate. invalidate()
之后，onDraw会被不断调用，绘制中间过程的view，直到到达最后的结果view。

这次花费的巨额时间告诉自己，应该全流程分析某个功能，因为自己对整个周期不熟悉，忽略任何一环都会导致失败。

<u>Feb 8th, 2020</u>