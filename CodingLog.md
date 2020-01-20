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