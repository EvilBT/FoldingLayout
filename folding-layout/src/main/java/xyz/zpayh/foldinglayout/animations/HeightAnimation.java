package xyz.zpayh.foldinglayout.animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

/**
 * 文 件 名: HeightAnimation
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/3/21 15:38
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注: 高度动画实现
 */

public class HeightAnimation extends Animation {
    private final View mView;
    private final int mHeightFrom;
    private final int mHeightTo;

    public HeightAnimation(View view, int heightFrom, int heightTo, long duration) {
        mView = view;
        mHeightFrom = heightFrom;
        mHeightTo = heightTo;
        setDuration(duration);
    }

    public HeightAnimation withInterpolator(Interpolator interpolator){
        if (interpolator != null) {
            setInterpolator(interpolator);
        }
        return this;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float newHeight = mHeightFrom + (mHeightTo-mHeightFrom)*interpolatedTime;

        if (interpolatedTime == 1){
            mView.getLayoutParams().height = mHeightTo;
        }else{
            mView.getLayoutParams().height = (int) newHeight;
        }
        mView.requestLayout();
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

    @Override
    public boolean isFillEnabled() {
        return false;
    }

    @Override
    public String toString() {
        return "HeightAnimation{" +
                ", mHeightFrom=" + mHeightFrom +
                ", mHeightTo=" + mHeightTo +
                ", offset=" + getStartOffset() +
                ", duration=" + getDuration() +
                '}';
    }
}
