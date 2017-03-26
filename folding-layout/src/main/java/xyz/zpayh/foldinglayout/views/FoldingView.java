package xyz.zpayh.foldinglayout.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

/**
 * 文 件 名: FoldingView
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/3/21 16:23
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class FoldingView extends RelativeLayout{

    private View mBackView;
    private View mFrontView;

    public FoldingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        setClipToPadding(false);
        setClipChildren(false);
        setLayoutParams(layoutParams);
    }

    public FoldingView(Context context, View backView, View frontView) {
        super(context);
        mBackView = backView;
        mFrontView = frontView;

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        setClipToPadding(false);
        setClipChildren(false);

        if (mBackView != null){
            addView(mBackView);
            LayoutParams params = (LayoutParams) mBackView.getLayoutParams();
            params.addRule(ALIGN_PARENT_BOTTOM);
            mBackView.setLayoutParams(params);
            layoutParams.height = params.height;
        }

        if (mFrontView != null){
            addView(mFrontView);
            LayoutParams params = (LayoutParams) mFrontView.getLayoutParams();
            params.addRule(ALIGN_PARENT_BOTTOM);
            mFrontView.setLayoutParams(params);
        }

        setLayoutParams(layoutParams);
    }

    public FoldingView withFrontView(View frontView){
        mFrontView = frontView;

        if (mFrontView != null) {
            addView(mFrontView);
            LayoutParams params = (LayoutParams) mFrontView.getLayoutParams();
            params.addRule(ALIGN_PARENT_BOTTOM);
            mFrontView.setLayoutParams(params);
        }

        return this;
    }

    public FoldingView withBackView(View backView){
        mBackView = backView;

        if (mBackView != null){
            addView(mBackView);
            LayoutParams params = (LayoutParams) mBackView.getLayoutParams();
            params.addRule(ALIGN_PARENT_BOTTOM);
            mBackView.setLayoutParams(params);
        }

        return this;
    }

    public View getBackView() {
        return mBackView;
    }

    public View getFrontView() {
        return mFrontView;
    }

    public void animateFrontView(Animation animation){
        if (mFrontView != null){
            mFrontView.startAnimation(animation);
        }
    }
}
