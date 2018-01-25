package com.netease.nim.quizgame.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.netease.nim.quizgame.R;

/**
 * Created by hzxuwen on 2017/4/19.
 */

public class CircleProgressView extends View {

    private static final String TAG = "CircleProgressBar";

    private float mMaxProgress = 100;

    private float mProgress = 30;

    private final int mCircleLineStrokeWidth = 8;

    private final int mTxtStrokeWidth = 48;

    // 画圆所在的距形区域
    private final RectF mRectF;

    private final Paint mPaint;

    private final Context mContext;

    private String format;

    private String content = "";

    private int textColor = Color.WHITE;

    private int backgroundColor = Color.TRANSPARENT;

    private int progressColor = Color.WHITE;

    private float textSize = 17;

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mRectF = new RectF();
        mPaint = new Paint();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView);
        textColor = ta.getColor(R.styleable.CircleProgressView_text_color, Color.WHITE);
        backgroundColor = ta.getColor(R.styleable.CircleProgressView_background_color, Color.TRANSPARENT);
        progressColor = ta.getColor(R.styleable.CircleProgressView_progress_color, Color.WHITE);
        textSize = ta.getDimension(R.styleable.CircleProgressView_text_size, 17);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();

        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }

        // 位置
        mRectF.left = mCircleLineStrokeWidth / 2; // 左上角x
        mRectF.top = mCircleLineStrokeWidth / 2; // 左上角y
        mRectF.right = width - mCircleLineStrokeWidth / 2; // 左下角x
        mRectF.bottom = height - mCircleLineStrokeWidth / 2; // 右下角y

        // 绘制圆背景色
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(backgroundColor);
        mPaint.setAntiAlias(false);
        canvas.drawCircle(mRectF.centerX(), mRectF.centerY(), height / 2 - mCircleLineStrokeWidth, mPaint);

        // 绘制圆圈，进度条背景
        mPaint.setStrokeWidth(mCircleLineStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.TRANSPARENT);
        canvas.drawArc(mRectF, -90, 360, false, mPaint);
        mPaint.setColor(progressColor);
        canvas.drawArc(mRectF, -90, ((float) mProgress / mMaxProgress) * 360, false, mPaint);

        // 绘制进度文案显示
        mPaint.setColor(textColor);
        String text = content + format;
        mPaint.setTextSize(textSize);

        float textWidth  = mPaint.measureText(text);
        float x = (width - textWidth) / 2;
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        //ascent是负的，descent是正的，dy是正的
        float dy = -(metrics.ascent + metrics.descent) / 2;
        float y = height / 2 + dy;
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, x, y, mPaint);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public float getMaxProgress() {
        return mMaxProgress;
    }

    public void setMaxProgress(float maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        this.invalidate();
    }

    public void setProgressNotInUiThread(float progress) {
        this.mProgress = progress;
        this.postInvalidate();
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }
}
