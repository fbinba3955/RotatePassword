package com.kiana.sjt.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.math.BigDecimal;

/**
 * 双转盘密码
 * @author shijianting
 * 2018/9/30 上午9:45
 */
public class RotatePassword extends View{

    //左右方向
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;

    Canvas mCanvas;

    boolean isCreateBitmap;

    //是否监听触摸事件
    boolean watchTouchEvent = false;
    //标识触摸事件是否可用
    boolean enableTouchEvent = true;

    //内圈标志
    public static final int OUTER = 0;
    public static final int INNER = 1;

    //顺时针
    public static final int CLOCK_RIGHT = 0;
    public static final int CLOCK_LEFT = 1;
    //旋转方向 0=顺时针 1=逆时针
    private int rotate_fx = CLOCK_RIGHT;
    private int rotate_fx_in = CLOCK_RIGHT;

    //圆盘走一格
    //外圈18个点，一格是20度
    public final float ONEPART = 20f;
    //内圈12个点，一格是30度
    public final float ONEPARTIN = 30f;

    //滑动触发阈值
    public final int MOVE_STEP = 20;

    //当前转动的角度
    private float rotateSD = 0;
    private float rotateSDIn = 0;

    // 设置图片画笔
    Paint bitmapPaint;
    //设置指针画笔
    Paint pointerPaint;

    Bitmap outCircleBitmap,inCircleBitmap;
    Bitmap bitmap;
    //外圈bitmap长宽
    int outCWidth,outCHeight;
    //内圈bitmap长宽
    int inCWidth,inCHeight;
    //中心点
    int centerX,centerY;

    //正确的密码值,未设置时为-1
    int correctOuterNumber = -1;
    int correctInnerNumber = -1;

    //外圈图片的原始尺寸
    int orginOutCWidth;
    int orginInCWidth;

    //图片缩放比例
    float scale = 1;

    //回调接口
    OnCircleChangedListener onCircleChangedListener;

    public RotatePassword(Context context) {
        super(context);
        init();
    }

    public RotatePassword(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RotatePassword(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // 画笔初始化
    private void init() {

        //加载需要操作的图片
        outCircleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_circle_out);
        inCircleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_circle_in);
        outCWidth = outCircleBitmap.getWidth();
        inCWidth = inCircleBitmap.getWidth();
        outCHeight = outCircleBitmap.getHeight();
        inCHeight = inCircleBitmap.getHeight();

        //onMeasure会多次执行，在这里保存外圈的原始尺寸
        orginOutCWidth = outCWidth;
        orginInCWidth = inCWidth;

        bitmapPaint = new Paint();
        //抗锯齿
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setDither(true);

        pointerPaint = new Paint();
        pointerPaint.setStrokeWidth(3);
        pointerPaint.setColor(Color.BLACK);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 获取宽-测量规则的模式和大小
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        // 获取高-测量规则的模式和大小
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //长和宽都是match_parent或者指定尺寸是，取其中小的作为圆盘的宽度
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            outCWidth = Math.min(widthSize, heightSize);
            outCHeight = outCWidth;
            //内圈大小按比例缩放
            BigDecimal scale = BigDecimal.valueOf(outCWidth).divide(BigDecimal.valueOf(orginOutCWidth), 4, BigDecimal.ROUND_HALF_EVEN);
            this.scale = scale.floatValue();
            inCWidth = orginInCWidth * outCWidth / orginOutCWidth;
            inCHeight = inCWidth;
        }

        // 设置wrap_content的默认宽 / 高值
        // 默认宽/高的设定并无固定依据,根据需要灵活设置
        // 类似TextView,ImageView等针对wrap_content均在onMeasure()对设置默认宽 / 高值有特殊处理,具体读者可以自行查看
        int mWidth = outCWidth;
        int mHeight = outCHeight;

        // 当布局参数设置为wrap_content时，设置默认值
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, mHeight);
            // 宽 / 高任意一个布局参数为= wrap_content时，都设置默认值
        } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, heightSize);
        } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, mHeight);
        }
        else {
            setMeasuredDimension(mWidth, mHeight);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();

        // 获取绘制内容的高度和宽度（考虑了四个方向的padding值）
        int width = getWidth() - paddingLeft - paddingRight ;
        int height = getHeight() - paddingTop - paddingBottom ;

        //这是中心点位置
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        //居中绘制外层转盘
        canvas.drawBitmap(getOutCircleBitmap(outCircleBitmap, outCWidth, rotateSD), getWidth()/2 - outCWidth/2, getHeight()/2 - outCHeight/2, bitmapPaint);
        canvas.drawBitmap(getInCircleBitmap(inCircleBitmap, inCWidth, rotateSDIn), getWidth()/2 - inCWidth/2, getHeight()/2 - inCHeight/2, bitmapPaint);

        drawPointer(canvas);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != outCircleBitmap) {
            outCircleBitmap.recycle();
        }
        if (null != inCircleBitmap) {
            inCircleBitmap.recycle();
        }
    }

    /**
     * 缩放图片
     * @param bitmap
     * @return
     */
    public Bitmap zoomBitmap(Bitmap bitmap) {
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidht = scale;
        float scaleHeight = scale;
        /*
         * 通过Matrix类的postScale方法进行缩放
         */
        matrix.postScale(scaleWidht, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, (int)width, (int)height, matrix, true);
        return newbmp;
    }


    /**
     * 旋转外部转盘
     * @param image
     * @param width
     * @param rotate
     * @return
     */
    private Bitmap getOutCircleBitmap(Bitmap image, int width, float rotate) {
        if (!isCreateBitmap) {
            //创建一个指定宽高的空白bitmap
            bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            //用那个空白bitmap创建一个画布
            mCanvas = new Canvas(bitmap);
        }
        if (rotate_fx==0) {
            //顺时针
            mCanvas.rotate(rotate, width / 2, width / 2);
        } else {
            //旋转画布：意思是下一次绘制的内容会被旋转这么多个角度
            //逆时针
            mCanvas.rotate(rotate, width / 2, width / 2);
        }
        image = zoomBitmap(image);
        //绘制图片，（图片会被旋转）
        mCanvas.drawBitmap(image, 0, 0, bitmapPaint);
        //这个bitmap在画布中被旋转，画圆，返回后就是一个圆形的bitmap
        return bitmap;
    }

    /**
     * 旋转内部转盘
     * @param image
     * @param width
     * @param rotate
     * @return
     */
    private Bitmap getInCircleBitmap(Bitmap image, int width, float rotate) {
        if (!isCreateBitmap) {
            //创建一个指定宽高的空白bitmap
            bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            //用那个空白bitmap创建一个画布
            mCanvas = new Canvas(bitmap);
        }
        if (rotate_fx_in==0) {
            //顺时针
            mCanvas.rotate(rotate, width / 2, width / 2);
        } else {
            //旋转画布：意思是下一次绘制的内容会被旋转这么多个角度
            //逆时针
            mCanvas.rotate(rotate, width / 2, width / 2);
        }
        image = zoomBitmap(image);
        //绘制图片，（图片会被旋转）
        mCanvas.drawBitmap(image, 0, 0, bitmapPaint);
        //这个bitmap在画布中被旋转，画圆，返回后就是一个圆形的bitmap
        return bitmap;
    }

    /**
     * 画指针
     * @param canvas
     */
    private void drawPointer(Canvas canvas) {
        canvas.drawLine(centerX, centerY, centerX, centerY - inCHeight / 3, pointerPaint);
    }

    /**
     * 设置正确的内外圈数字
     * @param outerNumber
     * @param innerNumber
     */
    public void setCorrectNumber(int outerNumber, int innerNumber) {
        this.correctOuterNumber = outerNumber;
        this.correctInnerNumber = innerNumber;
    }

    /**
     * 设置正确的内外圈数字，监听触摸事件，在转动到正确数字时，停止触摸事件。
     * @param outerNumber
     * @param innerNumber
     * @param watchTouchRotate
     */
    public void setCorrectNumber(int outerNumber, int innerNumber, boolean watchTouchRotate) {
        setCorrectNumber(outerNumber, innerNumber);
        watchTouchEvent = watchTouchRotate;
    }

    public void setOnCircleChangedListener(OnCircleChangedListener listener) {
        this.onCircleChangedListener = listener;
    }

    /**
     * 设置旋转角度
     * @param fx 方向
     * @param inOrOut 内外圆
     */
    public void setDegree(int fx, int inOrOut) {
        if (OUTER == inOrOut) {
            //顺时针
            if (fx == CLOCK_RIGHT) {
                rotate_fx = fx;
                rotateSD += ONEPART;
            }
            else{
                rotate_fx = fx;
                rotateSD -= ONEPART;
            }
        }
        if (INNER == inOrOut) {
            //顺时针
            if (fx == CLOCK_RIGHT) {
                rotate_fx_in = fx;
                rotateSDIn += ONEPARTIN;
            }
            else{
                rotate_fx_in = fx;
                rotateSDIn -= ONEPARTIN;
            }
        }
        invalidate();
    }

    /**
     * 获取外圈当前数字
     * @return
     */
    public int getOutNumber() {
        int sd = (int) rotateSD;
        int result = (360 - sd) % 360 / 20;
        return result;
    }

    /**
     * 获取内圈当前数字
     * @return
     */
    public int getInNumber() {
        int sd = (int) rotateSDIn;
        int result = (360 - sd) % 360 / 30;
        return result;
    }

    int beginX;
    int beginY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!enableTouchEvent && watchTouchEvent) {
            return true;
        }

        int x= (int) event.getX();
        int y= (int) event.getY();

        switch (event.getAction()){
            //触摸屏幕
            case MotionEvent.ACTION_DOWN:
                beginX=x;
                beginY=y;
                break;

            //在屏幕上拖动
            case MotionEvent.ACTION_MOVE:
                boolean moveResult = rotateWithMove(beginX, beginY, x, y, inInnerCircle(x, y));
                if (moveResult) {
                    //回调事件
                    if (onCircleChangedListener != null) {
                        int out = getOutNumber();
                        int in = getInNumber();
                        onCircleChangedListener.onChanged(out, in);
                        if (out == correctOuterNumber && in == correctInnerNumber) {
                            enableTouchEvent = false;
                            onCircleChangedListener.isCorrect(out, in);
                        }
                    }
                    beginX = x;
                    beginY = y;
                }
                break;

            //触摸离开屏幕
            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }

    /**
     * 判断坐标是不是在小圆内
     * @param x
     * @param y
     * @return
     */
    private int inInnerCircle(int x, int y) {
        //点距离圆心的距离
        int rangeCenter = (int) Math.sqrt(Math.pow(x - getWidth()/2, 2) + Math.pow(y - getHeight()/2, 2));
        //内圆半径
        int innerR = inCWidth / 2;
        return rangeCenter > innerR ? OUTER : INNER;
    }

    /**
     * 根据滑动 转动圆盘
     * @param bx
     * @param by
     * @param ex
     * @param ey
     * @param inOrOut
     * @return 是否转动了圆盘
     */
    private boolean rotateWithMove(int bx, int by, int ex, int ey, int inOrOut) {
        /*
        4区域分别处理
                    |
              1     |      2
                    |
        ---------------------------
                    |
              4     |      3
                    |
         */
        int mx = ex - bx;
        int my = ey - by;
        /**
         * zone1
         * 范围x:0~width/2 y:0~height/2
         */
        if (bx >= 0 && by >=0 && bx < outCWidth/2 && by < outCHeight/2) {
            //顺时针转动一格
            if (mx > MOVE_STEP || my < -MOVE_STEP) {
                setDegree(CLOCK_RIGHT, inOrOut);
                return true;
            }
            //逆时针转动一格
            if (mx < -MOVE_STEP || my > MOVE_STEP) {
                setDegree(CLOCK_LEFT, inOrOut);
                return true;
            }
        }
        /**
         * zone2
         * 范围x:width/2~width y:0~height/2
         */
        else if(bx >= outCWidth/2 && by >=0 && bx < outCWidth && by < outCHeight/2) {
            //顺时针转动一格
            if (mx > MOVE_STEP || my > MOVE_STEP) {
                setDegree(CLOCK_RIGHT, inOrOut);
                return true;
            }
            //逆时针转动一格
            if (mx < -MOVE_STEP || my < -MOVE_STEP) {
                setDegree(CLOCK_LEFT, inOrOut);
                return true;
            }
        }
        /**
         * zone3
         * 范围x:width/2~width y:height/2~height
         */
        else if(bx >= outCWidth/2 && by >=outCHeight/2 && bx < outCWidth && by < outCHeight) {
            //顺时针转动一格
            if (mx < -MOVE_STEP || my > MOVE_STEP) {
                setDegree(CLOCK_RIGHT, inOrOut);
                return true;
            }
            //逆时针转动一格
            if (mx > MOVE_STEP || my < -MOVE_STEP) {
                setDegree(CLOCK_LEFT, inOrOut);
                return true;
            }
        }
        /**
         * zone4
         * 范围x:0~width/2 y:height/2~height
         */
        else if(bx >= 0 && by >=outCHeight/2 && bx < outCWidth/2 && by < outCHeight) {
            //顺时针转动一格
            if (mx < -MOVE_STEP || my < -MOVE_STEP) {
                setDegree(CLOCK_RIGHT, inOrOut);
                return true;
            }
            //逆时针转动一格
            if (mx > MOVE_STEP || my > MOVE_STEP) {
                setDegree(CLOCK_LEFT, inOrOut);
                return true;
            }
        }
        return false;
    }

    /**
     * 转盘转动回调接口
     */
    public interface OnCircleChangedListener {

        /**
         * 内外圆值变化后的回调
         * @param outerNumber
         * @param innerNumber
         */
        public void onChanged(int outerNumber, int innerNumber);

        /**
         * 得到正确值的回调
         * @param outerNumber
         * @param innerNumber
         */
        public void isCorrect(int outerNumber, int innerNumber);
    }
}
