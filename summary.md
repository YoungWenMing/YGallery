# 总结
### 阶段性小结 

当前的照片墙应用完成了以下的功能：

1. 通过Cursor和MediaStore的url，获取手机存储上的所有图片;
2. 在Fragment中利用网格视图，显示所有图片;
3. 利用AsyncTask对图片进行高效异步加载;
4. 利用ViewPager实现大图浏览模式;
5. 通过GestureDetector检测手势控制输入;
6. 构建ScaleAnimator实现基于Matrix的放缩动画;

遇到的难点和解决方法：

* 上下滑动照片墙卡顿现象严重:**将同步加载方式改变成异步加载方式**，这样在inflate一个imageView的时候
不必等待图片加载，而是暂时显示过渡图片，这样就不会卡顿。
* 上下滑动时重新加载图片:**添加图片缓存**，在图片抓取器中添加一级LRU缓存，这样近期显示过的图片不用在滑重复加载，
加快显示的速率。
