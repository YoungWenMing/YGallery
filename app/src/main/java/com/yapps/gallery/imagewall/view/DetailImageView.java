package com.yapps.gallery.imagewall.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DetailImageView extends AppCompatImageView {

    private static final String TAG = "DIView";

    private View.OnClickListener mClickListener = null;

    /**
     * 图片缩放动画时间
     */
    public static final int SCALE_ANIMATOR_DURATION = 200;

    /**
     * 最大放大比例
     * @see #getMaxScale()
     */
    private static final float MAX_SCALE = 2f;

    /**
     * 外层变换矩阵
     * @see #getOuterMatrix(Matrix)
     */
    private Matrix mOutMatrix = new Matrix();

    public DetailImageView(Context context){
        super(context);
        initView();
    }

    public DetailImageView(Context context, AttributeSet set){
        super(context, set);
        initView();
    }

    private void initView() {
        //强制设置图片scaleType为matrix
        super.setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mClickListener = l;
        super.setOnClickListener(l);
    }

    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i(TAG, "Double Tap is catched!");
            doubleTap(e.getX(), e.getY());
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mClickListener != null){
                mClickListener.onClick(DetailImageView.this);
            }
            return super.onSingleTapConfirmed(e);
        }
    });


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * 处理双击事件
     * 考虑图片的放缩，求解放缩的比值，用于放缩的Matrix和相关量
     * @param ex    双击点横坐标
     * @param ey    双击点纵坐标
     */
    private void doubleTap(float ex, float ey){
        if (!isReady()) {
            return;
        }
//        if (getAnimationMatrix() == null){
//            setAnimationMatrix(mOutMatrix);
//        }
        Log.i(TAG, "start calculating animator");
        //获取第一层变换矩阵，从原始图片到适应控件的变换
        Matrix innerMatrix = MathUtils.matrixTake();
        getInnerMatrix(innerMatrix);
        //Matrix对象中有9个float值，其中第0个和第4个分别是x、y的缩放比例
        float innerScale = MathUtils.getMatrixScale(innerMatrix)[0];
        float outerScale = MathUtils.getMatrixScale(mOutMatrix)[0];
        float currentScale = innerScale * outerScale;
        //控件大小
        float displayWidth = getWidth();
        float displayHeight = getHeight();
        //TODO:确定接下来动画的缩放大小nextScale，可能是从小到大或者反过来
        float nextScale = calculateNextScale(innerScale, outerScale);
        //计算结果矩阵
        Matrix target = MathUtils.matrixTake(mOutMatrix);
        //计算所需放缩比例，中心点为指触点
        target.postScale(nextScale / currentScale, nextScale / currentScale, ex, ey);
        //将放大点移动到控件中心?TODO:存疑
        target.postTranslate(displayWidth/ 2f - ex, displayHeight / 2f - ey);
        //计算放缩之后的图片方框
        Matrix testM = MathUtils.matrixTake(innerMatrix);
        testM.postConcat(target);       //进行变换
        RectF bound = MathUtils.rectFTake(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        testM.mapRect(bound);           //rect bound is modified after this mapping
        //修正位置
        float postX = 0f;
        float postY = 0f;
        if (bound.right - bound.left < displayWidth){
            postX = (displayWidth - bound.right - bound.left) / 2f;
        }else if (bound.right < displayWidth){
            postX = displayWidth - bound.right;
        }else if (bound.left > 0) {
            postX = -bound.left;
        }

        if (bound.bottom - bound.top < displayHeight){
            postY = (displayHeight - bound.top - bound.bottom) / 2f;
        }else if (bound.bottom < displayHeight){
            postY = displayHeight - bound.bottom;
        }else if (bound.top > 0){
            postY = -bound.top;
        }

        target.postTranslate(postX, postY);
        //停止正在执行的动画
        cancelAllAnimations();
//        if (!scale){
//            scaleValues = new float[]{2,0,0,0,2,0,0,0,1};
//        }else {
//            scaleValues = new float[]{1,0,0,0,1,0,0,0,1};
//        }
//        scale = !scale;
//        target = new Matrix();
//        target.setValues(scaleValues);
        mAnimator = new ScaleAnimator(mOutMatrix, target);
        mAnimator.start();

        MathUtils.rectFGiven(bound);
        MathUtils.matrixGiven(innerMatrix);
        MathUtils.matrixGiven(testM);
        MathUtils.matrixGiven(target);


    }

    private boolean scale = false;

    private float[] scaleValues = new float[]{2,0,0,0,2,0,0,0,1};

    private ScaleAnimator mAnimator;

    @Override
    protected void onDraw(Canvas canvas) {
        if (isReady()){
            Log.i(TAG, " is ready!");
            Matrix matrix = MathUtils.matrixTake();
            matrix = getCurrentImageMatrix(matrix);
            setImageMatrix(matrix);
            MathUtils.matrixGiven(matrix);
        }
        super.onDraw(canvas);
    }

    private class ScaleAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener{

        /**
         * 开始矩阵
         */
        private float[] mStart = new float[9];

        /**
         * 结束矩阵
         */
        private float[] mEnd = new float[9];

        /**
         * 中间结果矩阵
         */
        private float[] mResult = new float[9];

        public ScaleAnimator(Matrix src, Matrix dst){
            this(src, dst, SCALE_ANIMATOR_DURATION);
        }

        /**
         * 构建一个缩放动画
         * <p>
         * 从一个矩阵变换到另外一个矩阵
         *
         * @param start    开始矩阵
         * @param end      结束矩阵
         * @param duration 动画时间
         */
        public ScaleAnimator(Matrix start, Matrix end, long duration) {
            super();
            setFloatValues(0, 1f);
            setDuration(duration);
            addUpdateListener(this);
            start.getValues(mStart);
            end.getValues(mEnd);
            Log.i(TAG, "start: " + Arrays.toString(mStart) + " end: " + Arrays.toString(mEnd));
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            //获取动画进度
            float value = (Float) animation.getAnimatedValue();
            //根据动画进度计算矩阵中间插值
            for (int i = 0; i < 9; i++) {
                mResult[i] = mStart[i] + (mEnd[i] - mStart[i]) * value;
            }
            //设置矩阵并重绘
            mOutMatrix.setValues(mResult);
            Log.i(TAG, "Matrix: " + Arrays.toString(mResult));
//            dispatchOuterMatrixChanged();
            invalidate();
        }
    }

    /**
     * 判断当前能否执行手势相关操作，只有当测量、布局等工作完成，获得了控件和Drawable
     * 最终的参数时，才能执行手势相关操作
     * 包括：是否有图片，图片是否已经加载，控件大小是否确定？
     * @return 是否已经可以执行手势相关操作
     * @see #doubleTap(float, float)
     */
    private boolean isReady(){
        return  (getDrawable() != null && getDrawable().getIntrinsicHeight() > 0 && getDrawable().getIntrinsicWidth() > 0
                && getHeight() > 0 && getWidth() > 0);
    }


    /**
     * 计算第一层变换的矩阵
     * @param matrix
     * @see #doubleTap(float, float)
     */
    private Matrix getInnerMatrix(Matrix matrix){
        if (matrix == null){
            matrix = new Matrix();
        }else {
            matrix.reset();
        }
        if (isReady()) {
            //求解当前drawable放入控件的转换矩阵，转换方式是中心对齐
            //源矩形是drawable，目标矩形是控件
            RectF srcRect = MathUtils.rectFTake(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
            RectF dstRect = MathUtils.rectFTake(0, 0, getWidth(), getHeight());
            matrix.setRectToRect(srcRect, dstRect, Matrix.ScaleToFit.CENTER);
            //在矩形池中回收矩形对象
            MathUtils.rectFGiven(dstRect);
            MathUtils.rectFGiven(srcRect);
        }
        return matrix;
    }

    /**
     * 获得外层变换矩阵的值
     * @param matrix
     */
    private Matrix getOuterMatrix(Matrix matrix){
        if (matrix == null){
            matrix = new Matrix();
        }
        matrix.set(mOutMatrix);
        return matrix;
    }

    /**
     * 获取图片总变换矩阵.
     * <p>
     * 总变换矩阵为内部变换矩阵x外部变换矩阵,决定了原图到所见最终状态的变换
     * 当尚未布局或者原图不存在时,其值无意义.所以在调用前需要确保前置条件有效,否则将影响计算结果.
     *
     * @param matrix 用于填充结果的对象
     * @return 如果传了matrix参数则将matrix填充后返回, 否则new一个填充返回
     * @see #getOuterMatrix(Matrix)
     * @see #getInnerMatrix(Matrix)
     */
    public Matrix getCurrentImageMatrix(Matrix matrix) {
        //获取内部变换矩阵
        matrix = getInnerMatrix(matrix);
        //乘上外部变换矩阵
        matrix.postConcat(mOutMatrix);
        return matrix;
    }

    /**
     * 获得最大的放大比例
     * @return 最大放大比例
     */
    protected float getMaxScale(){
        return MAX_SCALE;
    }

    private float calculateNextScale(float innerScale, float outerScale){
        float cur = innerScale * outerScale;
        if (cur == MAX_SCALE)   {
            return innerScale;
        }else {
            return MAX_SCALE;
        }
    }

    /**
     * 停止所有的手势动画
     */
    private void cancelAllAnimations(){
        if (mAnimator != null){
            mAnimator.cancel();
            mAnimator = null;
        }
    }


    ////////////////////////////////对外广播事件////////////////////////////////

    /**
     * 外部矩阵变化事件通知监听器
     */
    public interface OuterMatrixChangedListener {

        /**
         * 外部矩阵变化回调
         * <p>
         * 外部矩阵的任何变化后都收到此回调.
         * 外部矩阵变化后,总变化矩阵,图片的展示位置都将发生变化.
         *
         * @param detailImageView
         * @see #getOuterMatrix(Matrix)
         * @see #getCurrentImageMatrix(Matrix)
         * @see #getImageBound(RectF)
         */
        void onOuterMatrixChanged(DetailImageView detailImageView);
    }

    /**
     * 所有OuterMatrixChangedListener监听列表
     *
     * @see #addOuterMatrixChangedListener(OuterMatrixChangedListener)
     * @see #removeOuterMatrixChangedListener(OuterMatrixChangedListener)
     */
    private List<OuterMatrixChangedListener> mOuterMatrixChangedListeners;

    /**
     * 当mOuterMatrixChangedListeners被锁定不允许修改时,临时将修改写到这个副本中
     *
     * @see #mOuterMatrixChangedListeners
     */
    private List<OuterMatrixChangedListener> mOuterMatrixChangedListenersCopy;

    /**
     * mOuterMatrixChangedListeners的修改锁定
     * <p>
     * 当进入dispatchOuterMatrixChanged方法时,被加1,退出前被减1
     *
     * @see #dispatchOuterMatrixChanged()
     * @see #addOuterMatrixChangedListener(OuterMatrixChangedListener)
     * @see #removeOuterMatrixChangedListener(OuterMatrixChangedListener)
     */
    private int mDispatchOuterMatrixChangedLock;

    /**
     * 添加外部矩阵变化监听
     *
     * @param listener
     */
    public void addOuterMatrixChangedListener(OuterMatrixChangedListener listener) {
        if (listener == null) {
            return;
        }
        //如果监听列表没有被修改锁定直接将监听添加到监听列表
        if (mDispatchOuterMatrixChangedLock == 0) {
            if (mOuterMatrixChangedListeners == null) {
                mOuterMatrixChangedListeners = new ArrayList<OuterMatrixChangedListener>();
            }
            mOuterMatrixChangedListeners.add(listener);
        } else {
            //如果监听列表修改被锁定,那么尝试在监听列表副本上添加
            //监听列表副本将会在锁定被解除时替换到监听列表里
            if (mOuterMatrixChangedListenersCopy == null) {
                if (mOuterMatrixChangedListeners != null) {
                    mOuterMatrixChangedListenersCopy = new ArrayList<OuterMatrixChangedListener>(mOuterMatrixChangedListeners);
                } else {
                    mOuterMatrixChangedListenersCopy = new ArrayList<OuterMatrixChangedListener>();
                }
            }
            mOuterMatrixChangedListenersCopy.add(listener);
        }
    }

    /**
     * 删除外部矩阵变化监听
     *
     * @param listener
     */
    public void removeOuterMatrixChangedListener(OuterMatrixChangedListener listener) {
        if (listener == null) {
            return;
        }
        //如果监听列表没有被修改锁定直接在监听列表数据结构上修改
        if (mDispatchOuterMatrixChangedLock == 0) {
            if (mOuterMatrixChangedListeners != null) {
                mOuterMatrixChangedListeners.remove(listener);
            }
        } else {
            //如果监听列表被修改锁定,那么就在其副本上修改
            //其副本将会在锁定解除时替换回监听列表
            if (mOuterMatrixChangedListenersCopy == null) {
                if (mOuterMatrixChangedListeners != null) {
                    mOuterMatrixChangedListenersCopy = new ArrayList<OuterMatrixChangedListener>(mOuterMatrixChangedListeners);
                }
            }
            if (mOuterMatrixChangedListenersCopy != null) {
                mOuterMatrixChangedListenersCopy.remove(listener);
            }
        }
    }


    /**
     * 触发外部矩阵修改事件
     * <p>
     * 需要在每次给外部矩阵设置值时都调用此方法.
     *
     * @see #mOutMatrix
     */
    private void dispatchOuterMatrixChanged() {
        if (mOuterMatrixChangedListeners == null) {
            return;
        }
        //增加锁
        //这里之所以用计数器做锁定是因为可能在锁定期间又间接调用了此方法产生递归
        //使用boolean无法判断递归结束
        mDispatchOuterMatrixChangedLock++;
        //在列表循环过程中不允许修改列表,否则将引发崩溃
        for (OuterMatrixChangedListener listener : mOuterMatrixChangedListeners) {
            listener.onOuterMatrixChanged(this);
        }
        //减锁
        mDispatchOuterMatrixChangedLock--;
        //如果是递归的情况,mDispatchOuterMatrixChangedLock可能大于1,只有减到0才能算列表的锁定解除
        if (mDispatchOuterMatrixChangedLock == 0) {
            //如果期间有修改列表,那么副本将不为null
            if (mOuterMatrixChangedListenersCopy != null) {
                //将副本替换掉正式的列表
                mOuterMatrixChangedListeners = mOuterMatrixChangedListenersCopy;
                //清空副本
                mOuterMatrixChangedListenersCopy = null;
            }
        }
    }


    ////////////////////////////////防止内存抖动复用对象////////////////////////////////

    /**
     * 对象池
     * <p>
     * 防止频繁new对象产生内存抖动.
     * 由于对象池最大长度限制,如果吞吐量超过对象池容量,仍然会发生抖动.
     * 此时需要增大对象池容量,但是会占用更多内存.
     *
     * @param <T> 对象池容纳的对象类型
     */
    private static abstract class ObjectsPool<T> {

        /**
         * 对象池的最大容量
         */
        private int mSize;

        /**
         * 对象池队列
         */
        private Queue<T> mQueue;

        /**
         * 创建一个对象池
         *
         * @param size 对象池最大容量
         */
        public ObjectsPool(int size) {
            mSize = size;
            mQueue = new LinkedList<T>();
        }

        /**
         * 获取一个空闲的对象
         * <p>
         * 如果对象池为空,则对象池自己会new一个返回.
         * 如果对象池内有对象,则取一个已存在的返回.
         * take出来的对象用完要记得调用given归还.
         * 如果不归还,让然会发生内存抖动,但不会引起泄漏.
         *
         * @return 可用的对象
         * @see #given(Object)
         */
        public T take() {
            //如果池内为空就创建一个
            if (mQueue.size() == 0) {
                return newInstance();
            } else {
                //对象池里有就从顶端拿出来一个返回
                return resetInstance(mQueue.poll());
            }
        }

        /**
         * 归还对象池内申请的对象
         * <p>
         * 如果归还的对象数量超过对象池容量,那么归还的对象就会被丢弃.
         *
         * @param obj 归还的对象
         * @see #take()
         */
        public void given(T obj) {
            //如果对象池还有空位子就归还对象
            if (obj != null && mQueue.size() < mSize) {
                mQueue.offer(obj);
            }
        }

        /**
         * 实例化对象
         *
         * @return 创建的对象
         */
        abstract protected T newInstance();

        /**
         * 重置对象
         * <p>
         * 把对象数据清空到就像刚创建的一样.
         *
         * @param obj 需要被重置的对象
         * @return 被重置之后的对象
         */
        abstract protected T resetInstance(T obj);
    }


    /**
     * 矩阵对象池
     */
    private static class MatrixPool extends ObjectsPool<Matrix> {

        public MatrixPool(int size) {
            super(size);
        }

        @Override
        protected Matrix newInstance() {
            return new Matrix();
        }

        @Override
        protected Matrix resetInstance(Matrix obj) {
            obj.reset();
            return obj;
        }
    }

    /**
     * 矩形对象池
     */
    private static class RectFPool extends ObjectsPool<RectF> {

        public RectFPool(int size) {
            super(size);
        }

        @Override
        protected RectF newInstance() {
            return new RectF();
        }

        @Override
        protected RectF resetInstance(RectF obj) {
            obj.setEmpty();
            return obj;
        }
    }



    ////////////////////////////////数学计算工具类////////////////////////////////

    /**
     * 数学计算工具类
     */
    public static class MathUtils {

        /**
         * 矩阵对象池
         */
        private static MatrixPool mMatrixPool = new MatrixPool(16);

        /**
         * 获取矩阵对象
         */
        public static Matrix matrixTake() {
            return mMatrixPool.take();
        }

        /**
         * 获取某个矩阵的copy
         */
        public static Matrix matrixTake(Matrix matrix) {
            Matrix result = mMatrixPool.take();
            if (matrix != null) {
                result.set(matrix);
            }
            return result;
        }

        /**
         * 归还矩阵对象
         */
        public static void matrixGiven(Matrix matrix) {
            mMatrixPool.given(matrix);
        }

        /**
         * 矩形对象池
         */
        private static RectFPool mRectFPool = new RectFPool(16);

        /**
         * 获取矩形对象
         */
        public static RectF rectFTake() {
            return mRectFPool.take();
        }

        /**
         * 按照指定值获取矩形对象
         */
        public static RectF rectFTake(float left, float top, float right, float bottom) {
            RectF result = mRectFPool.take();
            result.set(left, top, right, bottom);
            return result;
        }

        /**
         * 获取某个矩形的副本
         */
        public static RectF rectFTake(RectF rectF) {
            RectF result = mRectFPool.take();
            if (rectF != null) {
                result.set(rectF);
            }
            return result;
        }

        /**
         * 归还矩形对象
         */
        public static void rectFGiven(RectF rectF) {
            mRectFPool.given(rectF);
        }

        /**
         * 获取两点之间距离
         *
         * @param x1 点1
         * @param y1 点1
         * @param x2 点2
         * @param y2 点2
         * @return 距离
         */
        public static float getDistance(float x1, float y1, float x2, float y2) {
            float x = x1 - x2;
            float y = y1 - y2;
            return (float) Math.sqrt(x * x + y * y);
        }

        /**
         * 获取两点的中点
         *
         * @param x1 点1
         * @param y1 点1
         * @param x2 点2
         * @param y2 点2
         * @return float[]{x, y}
         */
        public static float[] getCenterPoint(float x1, float y1, float x2, float y2) {
            return new float[]{(x1 + x2) / 2f, (y1 + y2) / 2f};
        }

        /**
         * 获取矩阵的缩放值
         *
         * @param matrix 要计算的矩阵
         * @return float[]{scaleX, scaleY}
         */
        public static float[] getMatrixScale(Matrix matrix) {
            if (matrix != null) {
                float[] value = new float[9];
                matrix.getValues(value);
                return new float[]{value[0], value[4]};
            } else {
                return new float[2];
            }
        }

        /**
         * 计算点除以矩阵的值
         * <p>
         * matrix.mapPoints(unknownPoint) -> point
         * 已知point和matrix,求unknownPoint的值.
         *
         * @param point
         * @param matrix
         * @return unknownPoint
         */
        public static float[] inverseMatrixPoint(float[] point, Matrix matrix) {
            if (point != null && matrix != null) {
                float[] dst = new float[2];
                //计算matrix的逆矩阵
                Matrix inverse = matrixTake();
                matrix.invert(inverse);
                //用逆矩阵变换point到dst,dst就是结果
                inverse.mapPoints(dst, point);
                //清除临时变量
                matrixGiven(inverse);
                return dst;
            } else {
                return new float[2];
            }
        }

        /**
         * 计算两个矩形之间的变换矩阵
         * <p>
         * unknownMatrix.mapRect(to, from)
         * 已知from矩形和to矩形,求unknownMatrix
         *
         * @param from
         * @param to
         * @param result unknownMatrix
         */
        public static void calculateRectTranslateMatrix(RectF from, RectF to, Matrix result) {
            if (from == null || to == null || result == null) {
                return;
            }
            if (from.width() == 0 || from.height() == 0) {
                return;
            }
            result.reset();
            result.postTranslate(-from.left, -from.top);
            result.postScale(to.width() / from.width(), to.height() / from.height());
            result.postTranslate(to.left, to.top);
        }

        /**
         * 计算图片在某个ImageView中的显示矩形
         *
         * @param container ImageView的Rect
         * @param srcWidth  图片的宽度
         * @param srcHeight 图片的高度
         * @param scaleType 图片在ImageView中的ScaleType
         * @param result    图片应该在ImageView中展示的矩形
         */
        public static void calculateScaledRectInContainer(RectF container, float srcWidth, float srcHeight, ScaleType scaleType, RectF result) {
            if (container == null || result == null) {
                return;
            }
            if (srcWidth == 0 || srcHeight == 0) {
                return;
            }
            //默认scaleType为fit center
            if (scaleType == null) {
                scaleType = ScaleType.FIT_CENTER;
            }
            result.setEmpty();
            if (ScaleType.FIT_XY.equals(scaleType)) {
                result.set(container);
            } else if (ScaleType.CENTER.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                matrix.setTranslate((container.width() - srcWidth) * 0.5f, (container.height() - srcHeight) * 0.5f);
                matrix.mapRect(result, rect);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else if (ScaleType.CENTER_CROP.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                float scale;
                float dx = 0;
                float dy = 0;
                if (srcWidth * container.height() > container.width() * srcHeight) {
                    scale = container.height() / srcHeight;
                    dx = (container.width() - srcWidth * scale) * 0.5f;
                } else {
                    scale = container.width() / srcWidth;
                    dy = (container.height() - srcHeight * scale) * 0.5f;
                }
                matrix.setScale(scale, scale);
                matrix.postTranslate(dx, dy);
                matrix.mapRect(result, rect);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else if (ScaleType.CENTER_INSIDE.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                float scale;
                float dx;
                float dy;
                if (srcWidth <= container.width() && srcHeight <= container.height()) {
                    scale = 1f;
                } else {
                    scale = Math.min(container.width() / srcWidth, container.height() / srcHeight);
                }
                dx = (container.width() - srcWidth * scale) * 0.5f;
                dy = (container.height() - srcHeight * scale) * 0.5f;
                matrix.setScale(scale, scale);
                matrix.postTranslate(dx, dy);
                matrix.mapRect(result, rect);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else if (ScaleType.FIT_CENTER.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempSrc = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempDst = rectFTake(0, 0, container.width(), container.height());
                matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER);
                matrix.mapRect(result, rect);
                rectFGiven(tempDst);
                rectFGiven(tempSrc);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else if (ScaleType.FIT_START.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempSrc = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempDst = rectFTake(0, 0, container.width(), container.height());
                matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.START);
                matrix.mapRect(result, rect);
                rectFGiven(tempDst);
                rectFGiven(tempSrc);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else if (ScaleType.FIT_END.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempSrc = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempDst = rectFTake(0, 0, container.width(), container.height());
                matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.END);
                matrix.mapRect(result, rect);
                rectFGiven(tempDst);
                rectFGiven(tempSrc);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else {
                result.set(container);
            }
        }
    }

}
