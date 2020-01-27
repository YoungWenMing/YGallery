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

Jan 27th, 2020