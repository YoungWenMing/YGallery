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