package xyz.zpayh.foldinglayout.animations;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.support.annotation.IntDef;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 文 件 名: FoldAnimation
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/3/21 12:03
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注: 主要的折叠动画
 */

public class FoldAnimation extends Animation {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FOLD_UP,UNFOLD_DOWN,FOLD_DOWN,UNFOLD_UP})
    @interface FoldAnimationMode{}

    public static final int FOLD_UP = 0;
    public static final int UNFOLD_DOWN = 1;
    public static final int FOLD_DOWN = 2;
    public static final int UNFOLD_UP = 3;

    @FoldAnimationMode
    private final int mFoldMode;
    private final int mCameraHeight;
    private float mFromDegrees;
    private float mToDegrees;
    private float mCenterX;
    private float mCenterY;
    private Camera mCamera;

    public FoldAnimation(@FoldAnimationMode int foldMode, int cameraHeight, long duration) {
        mFoldMode = foldMode;
        setFillAfter(true);
        setDuration(duration);
        mCameraHeight = cameraHeight;
    }

    public FoldAnimation withAnimationListener(AnimationListener animationListener){
        setAnimationListener(animationListener);
        return this;
    }

    public FoldAnimation withStartOffset(int offset){
        setStartOffset(offset);
        return this;
    }

    public FoldAnimation withInterpolator(Interpolator interpolator){
        if (interpolator != null){
            setInterpolator(interpolator);
        }
        return this;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
        mCamera.setLocation(0,0,-mCameraHeight);

        mCenterX = width / 2;
        switch (mFoldMode){
            case FOLD_UP:
                mCenterY = 0;
                mFromDegrees = 0;
                mToDegrees = 90;
                break;
            case FOLD_DOWN:
                mCenterY = height;
                mFromDegrees = 0;
                mToDegrees = -90;
                break;
            case UNFOLD_UP:
                mCenterY = height;
                mFromDegrees = -90;
                mToDegrees = 0;
                break;
            case UNFOLD_DOWN:
                mCenterY = 0;
                mFromDegrees = 90;
                mToDegrees = 0;
                break;
            default:break;
        }
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final Camera camera = mCamera;
        final Matrix matrix = t.getMatrix();
        final float fromDegrees = mFromDegrees;
        final float degrees = fromDegrees + ((mToDegrees - fromDegrees)*interpolatedTime);

        camera.save();
        camera.rotateX(degrees);
        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-mCenterX,-mCenterY);
        matrix.postTranslate(mCenterX,mCenterY);
    }

    @Override
    public String toString() {
        return "FoldAnimation{" +
                "mFoldMode=" + mFoldMode +
                ", mFromDegrees=" + mFromDegrees +
                ", mToDegrees=" + mToDegrees +
                '}';
    }
}
