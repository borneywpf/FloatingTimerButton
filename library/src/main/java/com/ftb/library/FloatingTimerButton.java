package com.ftb.library;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import java.util.concurrent.TimeUnit;

public class FloatingTimerButton extends FloatingActionButton implements
        View.OnTouchListener {
    private final static int REFRESH_RATE_TIME = 100;
    private final static float CLICK_DRAG_TOLERANCE = 10;

    private Handler handler;
    private float downRawX, downRawY;
    private float dX, dY;
    private int drawableWidh, drawableHeight;
    private int progressColor, progressSecondColor;
    private int progressWidth, progressPadding;
    private int borderColor;
    private int borderWidth;
    private Paint bordPaint;
    private Paint progressPaint, progressSecondPaint;
    private float maxProgress = 100.0f;
    private float progress = 0;
    private float onceProgress = 0;
    private int screenWidth;
    /**
     * 进度条角度
     */
    private float sweepAngle = 0.0f;
    /**
     * 定时器周期
     */
    private int periodMillis = 30 * 1000;
    private ValueAnimator facingAnimator;
    private TimeInterpolator facingTimeInterpolator;
    private int facingDuration = 500;
    private int facingPadding;

    public FloatingTimerButton(Context context) {
        this(context, null);
    }

    public FloatingTimerButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingTimerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs);
        init(context);
        initPaint();
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatingTimerButton);
        if (a.hasValue(R.styleable.FloatingTimerButton_drawableWidh)) {
            drawableWidh = a.getDimensionPixelOffset(R.styleable.FloatingTimerButton_drawableWidh,
                    0);
        }
        if (a.hasValue(R.styleable.FloatingTimerButton_drawableHeight)) {
            drawableHeight = a.getDimensionPixelOffset(
                    R.styleable.FloatingTimerButton_drawableHeight,
                    0);
        }
        if (a.hasValue(R.styleable.FloatingTimerButton_progressColor)) {
            progressColor = a.getColor(R.styleable.FloatingTimerButton_progressColor, Color.BLACK);
        }
        if (a.hasValue(R.styleable.FloatingTimerButton_progressSecondColor)) {
            progressSecondColor = a.getColor(R.styleable.FloatingTimerButton_progressSecondColor,
                    Color.WHITE);
        }
        if (a.hasValue(R.styleable.FloatingTimerButton_progressWidth)) {
            progressWidth = a.getDimensionPixelOffset(R.styleable.FloatingTimerButton_progressWidth,
                    0);
        }
        if (a.hasValue(R.styleable.FloatingTimerButton_progressPadding)) {
            progressPadding = a.getDimensionPixelOffset(
                    R.styleable.FloatingTimerButton_progressPadding, 0);
        }
        if (a.hasValue(R.styleable.FloatingTimerButton_borderColor)) {
            borderColor = a.getColor(R.styleable.FloatingTimerButton_borderColor,
                    Color.parseColor("#f6f5ec"));
        }
        if (a.hasValue(R.styleable.FloatingTimerButton_borderWidth)) {
            borderWidth = a.getDimensionPixelOffset(R.styleable.FloatingTimerButton_borderWidth, 1);
        }
        if (a.hasValue(R.styleable.FloatingTimerButton_maxProgress)) {
            maxProgress = a.getFloat(R.styleable.FloatingTimerButton_maxProgress, 100);
            if (maxProgress <= 0) {
                maxProgress = 100.0f;
            }
        }
        if (a.hasValue(R.styleable.FloatingTimerButton_periodSecond)) {
            periodMillis = a.getInt(R.styleable.FloatingTimerButton_periodSecond, 30) * 1000;
            if (periodMillis <= 0) {
                periodMillis = 30 * 1000;
            }
        }
        onceProgress = maxProgress * REFRESH_RATE_TIME / periodMillis;
        if (a.hasValue(R.styleable.FloatingTimerButton_facingDuration)) {
            facingDuration = a.getInt(R.styleable.FloatingTimerButton_facingDuration, 500);
            if (facingDuration < 0) {
                facingDuration = 500;
            }
        }
        if (a.hasValue(R.styleable.FloatingTimerButton_facingPadding)) {
            facingPadding = a.getDimensionPixelOffset(R.styleable.FloatingTimerButton_facingPadding,
                    0);
        }
    }

    private void initPaint() {
        //初始化边框画笔
        bordPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        bordPaint.setStrokeWidth(borderWidth);
        bordPaint.setColor(borderColor);
        bordPaint.setStyle(Paint.Style.STROKE);

        //初始化进度条画笔
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeJoin(Paint.Join.ROUND);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setColor(progressColor);

        //初始化进度条背景画笔
        progressSecondPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        progressSecondPaint.setStyle(Paint.Style.STROKE);
        progressSecondPaint.setStrokeWidth(progressWidth);
        progressSecondPaint.setColor(progressSecondColor);
    }

    private void init(Context context) {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    setProgress(progress + onceProgress);
                    handler.sendEmptyMessageDelayed(1, REFRESH_RATE_TIME);
                }
            }
        };
        screenWidth = getScreenWidth(context);
        facingTimeInterpolator = new LinearInterpolator();
        setOnTouchListener(this);
    }

    public void startTimer() {
        handler.removeMessages(1);
        handler.sendEmptyMessage(1);
    }

    public void pauseTimer() {
        handler.removeMessages(1);
    }

    public void endTimer() {
        handler.removeMessages(1);
        setProgress(maxProgress);
    }

    private void cancelTimer() {
        handler.removeMessages(1);
    }

    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //noinspection ConstantConditions
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            //noinspection ConstantConditions
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    /**
     * 设置进度
     */
    public void setProgress(float progress) {
        if (progress < 0) {
            this.progress = 0;
        } else if (progress > maxProgress) {
            this.progress = maxProgress;
            cancelTimer();
        } else {
            this.progress = progress;
        }
        progressToAngle();
        invalidate();
    }

    public void setTimeSecond(int time) {
        setTime(time, TimeUnit.SECONDS);
    }

    public void setTime(int time, TimeUnit unit) {
        long millis = unit.toMillis(time);
        if (millis < 0) {
            millis = 0;
        } else if (millis > periodMillis) {
            millis = periodMillis;
        }

        float progress = maxProgress * millis / periodMillis;
        setProgress(progress);
    }

    /**
     * 获得当前进度
     */
    public float getProgress() {
        return progress;
    }

    private void progressToAngle() {
        sweepAngle = 360.f / maxProgress * progress;
    }

    @Override
    public boolean onTouch(final View view, MotionEvent motionEvent) {
        if (facingAnimator != null && facingAnimator.isRunning()) {
            return true;
        }
        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();
            dX = view.getX() - downRawX;
            dY = view.getY() - downRawY;
            return true; // Consumed
        } else if (action == MotionEvent.ACTION_MOVE) {
            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();

            View viewParent = (View) view.getParent();
            int parentWidth = viewParent.getWidth();
            int parentHeight = viewParent.getHeight();

            float newX = motionEvent.getRawX() + dX;
            newX = Math.max(0, newX); // Don't allow the FAB past the left hand side of the parent
            newX = Math.min(parentWidth - viewWidth,
                    newX); // Don't allow the FAB past the right hand side of the parent

            float newY = motionEvent.getRawY() + dY;
            newY = Math.max(0, newY); // Don't allow the FAB past the top of the parent
            newY = Math.min(parentHeight - viewHeight,
                    newY); // Don't allow the FAB past the bottom of the parent

            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start();

            return true; // Consumed
        } else if (action == MotionEvent.ACTION_UP) {
            float upRawX = motionEvent.getRawX();
            float upRawY = motionEvent.getRawY();

            float upDX = upRawX - downRawX;
            float upDY = upRawY - downRawY;

            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY)
                    < CLICK_DRAG_TOLERANCE) { // A click
                return performClick();
            } else { // A drag
                startFacing(view);
                return true; // Consumed
            }
        } else {
            return super.onTouchEvent(motionEvent);
        }
    }

    private void startFacing(final View view) {
        int width = getWidth();
        final float x = getX();
        final float y = getY();
        int halfWidth = width / 2;
        float certerX = x + halfWidth;
        int halfScreenWidth = screenWidth / 2;

        if (certerX > halfScreenWidth) {
            facingAnimator = ValueAnimator.ofFloat(x, screenWidth - width - facingPadding);
        } else {
            facingAnimator = ValueAnimator.ofFloat(x, facingPadding);
        }

        facingAnimator.setDuration(facingDuration);
        facingAnimator.setInterpolator(facingTimeInterpolator);
        facingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.animate()
                        .x(value)
                        .y(y)
                        .setDuration(0)
                        .start();
            }
        });
        facingAnimator.start();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != VISIBLE) {
            release();
        }
    }

    /**
     * 贴边动画插值器
     */
    public void setFacingAnimatorInterpolator(TimeInterpolator interpolator) {
        if (interpolator != null) {
            facingTimeInterpolator = interpolator;
        }
    }

    /**
     * 贴边动画执行时间
     */
    public void setFacingAnimatorDuration(int duration) {
        facingDuration = duration > 0 ? duration : 500;
    }

    public void release() {
        facingAnimator.end();
        handler.removeMessages(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        Drawable background = getBackground();
        background.setBounds(0, 0, width, height);
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        if ((scrollX | scrollY) == 0) {
            background.draw(canvas);
        } else {
            canvas.translate(scrollX, scrollY);
            background.draw(canvas);
            canvas.translate(-scrollX, -scrollY);
        }
        canvas.drawCircle(width / 2, height / 2, (width - borderWidth) / 2.0f, bordPaint);

        RectF progressOval = new RectF(progressPadding, progressPadding, width - progressPadding,
                height - progressPadding);
        canvas.drawArc(progressOval, -90, 360, false, progressSecondPaint);
        canvas.drawArc(progressOval, -90, sweepAngle, false, progressPaint);

        Drawable drawable = getDrawable();
        drawable.setBounds(0, 0, drawableWidh, drawableHeight);
        canvas.save();
        canvas.translate((width - drawableWidh) / 2, (height - drawableHeight) / 2);
        drawable.draw(canvas);
        canvas.restore();
    }
}
