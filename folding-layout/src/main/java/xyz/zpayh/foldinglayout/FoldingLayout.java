package xyz.zpayh.foldinglayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;

import xyz.zpayh.foldinglayout.animations.AnimationEndListener;
import xyz.zpayh.foldinglayout.animations.FoldAnimation;
import xyz.zpayh.foldinglayout.animations.HeightAnimation;
import xyz.zpayh.foldinglayout.views.FoldingView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


/**
 * 文 件 名: FoldLayout
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/3/23 15:39
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class FoldingLayout extends ViewGroup{

    private static final int ANIMATION = -1;
    public static final int FOLDED = 0;
    public static final int UN_FOLDED = 1;

    private static final String TAG = "FoldLayout";

    // 默认值
    private final int DEFAULT_ANIMATION_DURATION = 1000;
    private final int DEFAULT_BACK_SIDE_COLOR = Color.GRAY;
    private final int DEFAULT_ADDITIONAL_FLIPS = 0;
    private final int DEFAULT_CAMERA_HEIGHT = 30;
    private final boolean DEFAULT_FOLD = true;

    // 状态变量
    private boolean mFolded = DEFAULT_FOLD;
    private boolean mAnimationInProgress;

    private int mAnimationDuration = DEFAULT_ANIMATION_DURATION;
    @ColorInt
    private int mBackSideColor = DEFAULT_BACK_SIDE_COLOR;
    private int mAdditionalFlipsCount = DEFAULT_ADDITIONAL_FLIPS;
    private int mCameraHeight = DEFAULT_CAMERA_HEIGHT;

    private final ArrayList<View> mOtherChildren = new ArrayList<>(1);

    public FoldingLayout(Context context) {
        super(context);
        init(context,null,0,0);
    }

    public FoldingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,0,0);
    }

    public FoldingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr,0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //清除其他布局
        //clearOtherChild();

        int count = getChildCount();

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (mAnimationInProgress){
                //处于动画状态中
                if (lp.responsibility == ANIMATION){
                    //只测量动画布局
                    child.setVisibility(VISIBLE);
                    measureChildWithMargins(child,widthMeasureSpec,0,heightMeasureSpec,0);
                    maxWidth = Math.max(maxWidth,
                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                    maxHeight = Math.max(maxHeight,
                            child.getMeasuredHeight() + lp.bottomMargin + lp.topMargin);
                    childState = combineMeasuredStates(childState,child.getMeasuredState());
                }else{
                    //隐藏其他两个布局
                    child.setVisibility(GONE);
                }
            }else if (mFolded){
                //折叠布局
                if (lp.responsibility == FOLDED){
                    child.setVisibility(VISIBLE);
                    measureChildWithMargins(child,widthMeasureSpec,0,heightMeasureSpec,0);
                    maxWidth = Math.max(maxWidth,
                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                    maxHeight = Math.max(maxHeight,
                            child.getMeasuredHeight() + lp.bottomMargin + lp.topMargin);
                    childState = combineMeasuredStates(childState,child.getMeasuredState());
                }else{
                    child.setVisibility(GONE);
                }
            }else{
                //展开布局
                if (lp.responsibility == UN_FOLDED){
                    child.setVisibility(VISIBLE);
                    measureChildWithMargins(child,widthMeasureSpec,0,heightMeasureSpec,0);
                    maxWidth = Math.max(maxWidth,
                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                    maxHeight = Math.max(maxHeight,
                            child.getMeasuredHeight() + lp.bottomMargin + lp.topMargin);
                    childState = combineMeasuredStates(childState,child.getMeasuredState());
                }else{
                    child.setVisibility(GONE);
                }
            }
        }

        // 加上padding,禁用padding
        //maxWidth += getPaddingLeft() + getPaddingRight();
        //maxHeight += getPaddingBottom() + getPaddingTop();

        // 检查我们的最小高度和宽度
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());


        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight,heightMeasureSpec,childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    public void addView(View child) {
        if (!checkView(child.getLayoutParams())){
            //throw new IllegalStateException("FoldingLayout can only add unfolded or folded view");
            Log.d(TAG, "addView: FoldingLayout can only add unfolded or folded view");
            return;
        }
        super.addView(child,-1,child.getLayoutParams());
    }

    @Override
    public void addView(View child, int index) {
        if (!checkView(child.getLayoutParams())){
            //throw new IllegalStateException("FoldingLayout can only add unfolded or folded view");
            Log.d(TAG, "addView: FoldingLayout can only add unfolded or folded view");
            return;
        }
        super.addView(child, index,getLayoutParams());
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!checkView(child.getLayoutParams())){
            //throw new IllegalStateException("FoldingLayout can only add unfolded or folded view");
            Log.d(TAG, "addView: FoldingLayout can only add unfolded or folded view");
            return;
        }
        super.addView(child, index, params);
    }

    private boolean checkView(ViewGroup.LayoutParams params) {
        if (params == null){
            return false;
        }

        if (checkLayoutParams(params)){
            LayoutParams lp = (LayoutParams) params;
            if (lp.responsibility == FOLDED){
                final View oldFoldedView = getFoldedView();
                if (oldFoldedView != null){
                    removeViewInLayout(oldFoldedView);
                }
                return true;
            }
            if (lp.responsibility == UN_FOLDED){
                final View oldUnfoldedView = getUnfoldedView();
                if (oldUnfoldedView != null){
                    removeViewInLayout(oldUnfoldedView);
                }
                return true;
            }
            if (lp.responsibility == ANIMATION){
                return true;
            }
        }
        return false;
    }

    /*private void clearOtherChild() {
        int count = getChildCount();
        mOtherChildren.clear();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp == null || lp.responsibility == UN_RESPONSIBILITY){
                mOtherChildren.add(child);
            }
        }

        for (View child : mOtherChildren) {
            removeViewInLayout(child);
        }
        mOtherChildren.clear();
    }*/

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren(l,t,r,b);
    }

    private void layoutChildren(int left, int top, int right, int bottom) {
        final int count = getChildCount();

        final int parentLeft = getPaddingLeft();
        final int parentRight = right - left - getPaddingRight();
        final int parentTop = getPaddingTop();
        final int parentBottom = top - bottom - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            //if (lp.responsibility != UN_RESPONSIBILITY || child.getVisibility() != GONE){
            if (child.getVisibility() != GONE){

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft = parentLeft + lp.leftMargin;
                int childTop = parentTop + lp.topMargin;

                child.layout(childLeft,childTop,childLeft+width,childTop+height);
            }
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(MATCH_PARENT, MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(),attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams){
            return new LayoutParams((LayoutParams)p);
        }else if (p instanceof MarginLayoutParams){
            return new LayoutParams((MarginLayoutParams)p);
        }
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams{

        public static final int UN_RESPONSIBILITY = -2;

        public int responsibility = UN_RESPONSIBILITY;

        public LayoutParams(@NonNull Context c,@NonNull AttributeSet attrs) {
            super(c, attrs);

            final TypedArray a = c.obtainStyledAttributes(attrs,R.styleable.FoldingLayout);
            responsibility = a.getInt(R.styleable.FoldingLayout_responsibility,UN_RESPONSIBILITY);
            a.recycle();
        }

        public LayoutParams(@Px int width, @Px int height) {
            super(width, height);
        }

        public LayoutParams(@Px int width, @Px int height, int responsibility) {
            super(width, height);
            this.responsibility = responsibility;
        }

        public LayoutParams(@NonNull MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull LayoutParams source){
            super(source);

            this.responsibility = source.responsibility;
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FoldingLayout,defStyleAttr,defStyleRes);
        try {
            mFolded = array.getBoolean(R.styleable.FoldingLayout_folded,DEFAULT_FOLD);
            mAnimationDuration = array.getInt(R.styleable.FoldingLayout_animationDuration,DEFAULT_ANIMATION_DURATION);
            mBackSideColor = array.getColor(R.styleable.FoldingLayout_backSideColor,DEFAULT_BACK_SIDE_COLOR);
            mAdditionalFlipsCount = array.getInt(R.styleable.FoldingLayout_additionalFlipsCount,DEFAULT_ADDITIONAL_FLIPS);
            mCameraHeight = array.getInt(R.styleable.FoldingLayout_cameraHeight,DEFAULT_CAMERA_HEIGHT);

            Log.d(TAG, "mFolded:" + mFolded);
            Log.d(TAG, "mAnimationDuration:" + mAnimationDuration);
            Log.d(TAG, "mBackSideColor:" + mBackSideColor);
            Log.d(TAG, "mAdditionalFlipsCount:" + mAdditionalFlipsCount);
            Log.d(TAG, "mCameraHeight:" + mCameraHeight);
        }finally {
            array.recycle();
        }
        setClipChildren(false);//允许子节点超出父节点
        setClipToPadding(false);//允许子节点绘制在父节点的padding上
        super.setPadding(0,0,0,0);

        //clearOtherChild();
    }

    @Override
    public void setPaddingRelative(@Px int start, @Px int top, @Px int end, @Px int bottom) {
        // do nothing, 禁用padding
    }

    @Override
    public void setPadding(@Px int left, @Px int top, @Px int right, @Px int bottom) {
        // do nothing, 禁用padding
    }

    public boolean isFolded() {
        return mFolded;
    }

    public View getFoldedView(){
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.responsibility == FOLDED){
                return view;
            }
        }
        return null;
    }

    public View getUnfoldedView(){
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.responsibility == UN_FOLDED){
                return view;
            }
        }
        return null;
    }

    /**
     * 展开布局
     * @param skipAnimation true 忽略动画， false 动态展开
     */
    public void unfold(boolean skipAnimation){
        //正处于动画状态下不处理
        if (!mFolded || mAnimationInProgress) return;

        if (skipAnimation){
            setStateToUnfolded();
            return;
        }

        // 获得主内容
        final View unfoldedView = getUnfoldedView();
        if (unfoldedView == null) return;
        final View foldedView = getFoldedView();
        if (foldedView == null) return;

        // 为动画创建布局容器元素
        final LinearLayout foldingLayout = createAndPrepareFoldingContainer();
        addView(foldingLayout);

        // 隐藏展开布局和折叠布局
        //foldedView.setVisibility(GONE);
        //unfoldedView.setVisibility(GONE);

        // 提取展开布局和折叠布局的内容
        Bitmap bitmapFromTitleView = getBitmapFromView(foldedView, getMeasuredWidth());
        Bitmap bitmapFromContentView = getBitmapFromView(unfoldedView, getMeasuredWidth());

        // 计算每个动画部分的高度
        ArrayList<Integer> heights = calculateHeightsForAnimationParts(foldedView.getHeight(),
                unfoldedView.getHeight(),mAdditionalFlipsCount);

        // 创建动画列表
        ArrayList<FoldingView> foldingViews = prepareViewsForAnimation(heights, bitmapFromTitleView,
                bitmapFromContentView);

        // 开始折叠动画的监听
        int childCount = foldingViews.size();
        int part90degreeAnimationDuration = mAnimationDuration / (childCount*2);
        startUnfoldAnimation(foldingViews,foldingLayout,part90degreeAnimationDuration,
                new AnimationEndListener(){
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //unfoldedView.setVisibility(VISIBLE);
                        //foldingLayout.setVisibility(GONE);
                        removeView(foldingLayout);
                        mFolded = false;
                        mAnimationInProgress = false;

                        Log.d(TAG, "End:折叠:("+foldedView.getWidth()+","+foldedView.getHeight()+")");
                        Log.d(TAG, "End:展开:("+unfoldedView.getWidth()+","+unfoldedView.getHeight()+")");
                        Log.d(TAG, "End:现在:("+getWidth()+","+getHeight()+")");
                    }
                });

        startExpandHeightAnimation(heights, part90degreeAnimationDuration*2);
        mAnimationInProgress = true;
    }

    /**
     * 折叠布局
     * @param skipAnimation true 忽略动画， false 动态折叠
     */
    public void fold(boolean skipAnimation){
        //如果已经是折叠状态或者还在动画中则不处理
        if (mFolded || mAnimationInProgress) return;
        if (skipAnimation){
            setStateToFolded();
            return;
        }

        // 获得主内容
        final View unfoldedView = getUnfoldedView();
        if (unfoldedView == null) return;
        final View foldedView = getFoldedView();
        if (foldedView == null) return;

        // 为折叠动画创建空布局
        final LinearLayout foldingLayout = createAndPrepareFoldingContainer();
        addView(foldingLayout);

        // 提取折叠和展开的内容
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        Bitmap bitmapFromTitleView = getBitmapFromView(foldedView,width);
        Bitmap bitmapFromContentView = getBitmapFromView(unfoldedView,width);

        //foldedView.setVisibility(GONE);
        //unfoldedView.setVisibility(GONE);

        Log.d(TAG, "titleView.getHeight():" + foldedView.getHeight());
        Log.d(TAG, "contentView.getHeight():" + unfoldedView.getHeight());

        ArrayList<Integer> heights = calculateHeightsForAnimationParts(foldedView.getHeight(),
                unfoldedView.getHeight(),mAdditionalFlipsCount);

        ArrayList<FoldingView> foldingViews = prepareViewsForAnimation(heights,bitmapFromTitleView,
                bitmapFromContentView);

        int childCount = foldingViews.size();
        int part90degreeAnimationDuration = mAnimationDuration / (childCount*2);

        startFoldAnimation(foldingViews,foldingLayout,part90degreeAnimationDuration,
                new AnimationEndListener(){
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //unfoldedView.setVisibility(GONE);
                        //foldedView.setVisibility(VISIBLE);
                        //foldingLayout.setVisibility(GONE);
                        removeView(foldingLayout);
                        mAnimationInProgress = false;
                        mFolded = true;
                        Log.d(TAG, "End:标题:("+foldedView.getWidth()+","+foldedView.getHeight()+")");
                        Log.d(TAG, "End:内容:("+unfoldedView.getWidth()+","+unfoldedView.getHeight()+")");
                        Log.d(TAG, "End:自己:("+getWidth()+","+getHeight()+")");
                    }
                });

        startCollapseHeightAnimation(heights, part90degreeAnimationDuration * 2);
        mAnimationInProgress = true;
    }

    /**
     * 改变当前折叠状态
     * @param skipAnimation true 忽略动画， false 动态折叠
     */
    public void toggle(boolean skipAnimation){
        if (mFolded){
            unfold(skipAnimation);
        }else{
            fold(skipAnimation);
        }
    }

    private ArrayList<FoldingView> prepareViewsForAnimation(ArrayList<Integer> viewHeights, Bitmap titleViewBitmap, Bitmap contentViewBitmap) {
        if (viewHeights == null || viewHeights.isEmpty()) {
            throw new IllegalStateException("ViewHeights array must be not null and more than zero");
        }

        ArrayList<FoldingView> partsList = new ArrayList<>();

        int partWidth = titleViewBitmap.getWidth();
        int yOffset = 0;

        for (int i = 0; i < viewHeights.size(); i++) {
            int partHeight = viewHeights.get(i);
            Bitmap partBitmap = Bitmap.createBitmap(partWidth,partHeight,Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(partBitmap);
            Rect srcRect = new Rect(0, yOffset, partWidth, yOffset + partHeight);
            Rect destRect = new Rect(0, 0, partWidth, partHeight);
            canvas.drawBitmap(contentViewBitmap, srcRect, destRect, null);
            ImageView backView = createImageViewFromBitmap(partBitmap);
            ImageView frontView = null;
            if ( i < viewHeights.size() - 1){
                //如果不是最后一个
                frontView = (i == 0) ? createImageViewFromBitmap(titleViewBitmap)
                        : createBackSideView(viewHeights.get(i+1));
            }
            partsList.add(new FoldingView(getContext(),backView,frontView));
            yOffset += partHeight;
        }
        return partsList;
    }

    private ArrayList<Integer> calculateHeightsForAnimationParts(int titleViewHeight, int contentViewHeight, int additionalFlipsCount) {
        ArrayList<Integer> partHeights = new ArrayList<>();
        int additionPartsTotalHeight = contentViewHeight - titleViewHeight * 2;
        if (additionPartsTotalHeight < 0){
            throw new IllegalStateException("Content View Height is too small");
        }

        partHeights.add(titleViewHeight);
        partHeights.add(titleViewHeight);

        if (additionPartsTotalHeight == 0){
            return partHeights;
        }

        if (additionalFlipsCount == 0 ){
            int partsCount = additionPartsTotalHeight / titleViewHeight;
            int restPartHeight = additionPartsTotalHeight % titleViewHeight;
            for (int i = 0; i < partsCount; i++) {
                partHeights.add(titleViewHeight);
            }
            if (restPartHeight > 0){
                partHeights.add(restPartHeight);
            }
        }else{
            int additionalPartHeight = additionPartsTotalHeight / additionalFlipsCount;
            int remainingHeight = additionPartsTotalHeight % additionalFlipsCount;

            if (additionalPartHeight + remainingHeight > titleViewHeight){
                throw new IllegalStateException("Additional flips count is too small");
            }
            for (int i = 0; i < additionalFlipsCount; i++) {
                partHeights.add(additionalPartHeight + (i==0?remainingHeight:0));
            }
        }

        return partHeights;
    }

    private ImageView createBackSideView(int height) {
        ImageView imageView = new ImageView(getContext());
        imageView.setBackgroundColor(mBackSideColor);
        imageView.setLayoutParams(new LayoutParams(MATCH_PARENT,height));
        return imageView;
    }

    private ImageView createImageViewFromBitmap(Bitmap bitmap) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(bitmap);
        imageView.setLayoutParams(new LayoutParams(bitmap.getWidth(),bitmap.getHeight()));
        return imageView;
    }

    private Bitmap getBitmapFromView(View view, int parentWidth) {
        measureView(view,parentWidth);
        layoutView(view);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(-view.getScrollX(),-view.getScrollY());
        view.draw(canvas);
        return bitmap;
    }

    private LinearLayout createAndPrepareFoldingContainer() {
        LinearLayout foldingContainer = new LinearLayout(getContext());
        foldingContainer.setClipChildren(false);
        foldingContainer.setClipToPadding(false);
        foldingContainer.setOrientation(LinearLayout.VERTICAL);
        LayoutParams params = new LayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        params.responsibility = ANIMATION;
        foldingContainer.setLayoutParams(params);
        return foldingContainer;
    }

    private void startExpandHeightAnimation(ArrayList<Integer> viewHeights, int partAnimationDuration) {
        if (viewHeights == null || viewHeights.isEmpty()){
            throw new IllegalStateException("ViewHeights array must be not null and more than zero");
        }

        ArrayList<Animation> heightAnimations = new ArrayList<>(viewHeights.size());
        int fromHeight = viewHeights.get(0);
        int delay = 0;
        int animationDuration = partAnimationDuration - delay;
        for (int i = 1; i < viewHeights.size(); i++) {
            int toHeight = fromHeight + viewHeights.get(i);
            HeightAnimation animation = new HeightAnimation(this,fromHeight,toHeight,animationDuration)
                    .withInterpolator(new DecelerateInterpolator());
            animation.setStartOffset(delay);
            heightAnimations.add(animation);
            fromHeight = toHeight;
        }
        createAnimationChain(heightAnimations,this);
        startAnimation(heightAnimations.get(0));
    }

    private void startCollapseHeightAnimation(ArrayList<Integer> viewHeights, int partAnimationDuration) {
        if (viewHeights == null || viewHeights.isEmpty()){
            throw new IllegalStateException("ViewHeights array must be not null and more than zero");
        }

        ArrayList<Animation> heightAnimations = new ArrayList<>(viewHeights.size());
        int fromHeight = viewHeights.get(0);
        for (int i = 1; i < viewHeights.size(); i++) {
            int toHeight = fromHeight + viewHeights.get(i);
            HeightAnimation animation = new HeightAnimation(this,toHeight, fromHeight,partAnimationDuration)
                    .withInterpolator(new DecelerateInterpolator());
            heightAnimations.add(animation);
            fromHeight = toHeight;
        }

        Collections.reverse(heightAnimations);
        createAnimationChain(heightAnimations,this);
        startAnimation(heightAnimations.get(0));
    }

    private void createAnimationChain(final ArrayList<Animation> animations, final View view) {
        for (int i = 0; i < animations.size(); i++) {
            Animation animation = animations.get(i);
            if (i < animations.size()-1){
                final int index = i;
                animation.setAnimationListener(new AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.startAnimation(animations.get(index+1));
                    }
                });
            }
        }
    }

    private void startFoldAnimation(ArrayList<FoldingView> foldingViews, LinearLayout foldingLayout,
                                    int part90degreeAnimationDuration, AnimationEndListener animationEndListener) {
        for (FoldingView foldingView : foldingViews) {
            foldingLayout.addView(foldingView);
        }

        Collections.reverse(foldingViews);

        int nextDelay = 0;
        for (int i = 0; i < foldingViews.size(); i++) {
            FoldingView view = foldingViews.get(i);
            view.setVisibility(VISIBLE);
            if (i != 0){
                FoldAnimation foldAnimation = new FoldAnimation(FoldAnimation.UNFOLD_UP,mCameraHeight,
                        part90degreeAnimationDuration)
                        .withStartOffset(nextDelay)
                        .withInterpolator(new DecelerateInterpolator());
                if (i== foldingViews.size() - 1){
                    foldAnimation.setAnimationListener(animationEndListener);
                }
                view.animateFrontView(foldAnimation);
                nextDelay += part90degreeAnimationDuration;
            }

            if (i != foldingViews.size() -1){
                view.startAnimation(new FoldAnimation(FoldAnimation.FOLD_UP,mCameraHeight,
                        part90degreeAnimationDuration)
                        .withStartOffset(nextDelay)
                        .withInterpolator(new DecelerateInterpolator()));
                nextDelay += part90degreeAnimationDuration;
            }
        }
    }

    private void startUnfoldAnimation(ArrayList<FoldingView> foldingViews, LinearLayout foldingLayout,
                                      int part90degreeAnimationDuration, AnimationEndListener animationEndListener) {
        int nextDelay = 0;
        for (int i = 0; i < foldingViews.size(); i++) {
            FoldingView view = foldingViews.get(i);
            view.setVisibility(VISIBLE);
            foldingLayout.addView(view);
            if (i != 0){
                FoldAnimation foldAnimation = new FoldAnimation(FoldAnimation.UNFOLD_DOWN,mCameraHeight,
                        part90degreeAnimationDuration)
                        .withStartOffset(nextDelay)
                        .withInterpolator(new DecelerateInterpolator());

                if (i == foldingViews.size() - 1){
                    foldAnimation.setAnimationListener(animationEndListener);
                }

                nextDelay += part90degreeAnimationDuration;
                view.startAnimation(foldAnimation);
            }

            if (i != foldingViews.size() -1){
                FoldAnimation animation = new FoldAnimation(FoldAnimation.FOLD_DOWN, mCameraHeight,
                        part90degreeAnimationDuration)
                        .withStartOffset(nextDelay)
                        .withInterpolator(new DecelerateInterpolator());
                view.animateFrontView(animation);
                nextDelay += part90degreeAnimationDuration;
            }
        }
    }

    private void setStateToUnfolded() {
        if (mAnimationInProgress || !mFolded) return;

        final View unfoldedView = getUnfoldedView();
        if (unfoldedView == null) return;
        final View foldedView = getFoldedView();
        if (foldedView == null) return;
        //unfoldedView.setVisibility(VISIBLE);
        //foldedView.setVisibility(GONE);
        measureView(unfoldedView,this.getMeasuredWidth());
        layoutView(unfoldedView);
        mFolded = false;
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = unfoldedView.getHeight();
        Log.d(TAG, "展开高度：height:" + params.height+",测量高度"+unfoldedView.getMeasuredHeight());
        setLayoutParams(params);
        requestLayout();
    }

    private void setStateToFolded() {
        if (mAnimationInProgress || mFolded) return;

        final View unfoldedView = getUnfoldedView();
        if (unfoldedView == null) return;
        final View foldedView = getFoldedView();
        if (foldedView == null) return;
        //unfoldedView.setVisibility(GONE);
        //foldedView.setVisibility(VISIBLE);
        mFolded = true;
        measureView(foldedView,getMeasuredWidth());
        layoutView(foldedView);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = foldedView.getHeight();
        Log.d(TAG, "折叠高度：height:" + params.height+",测量高度"+foldedView.getMeasuredHeight());
        setLayoutParams(params);
        requestLayout();
    }

    private void measureView(View view, int parentWidth) {
        int width = MeasureSpec.makeMeasureSpec(parentWidth, MeasureSpec.EXACTLY);
        int height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(width,height);
    }

    private void layoutView(View view) {
        view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight());
    }

    public int getAnimationDuration() {
        return mAnimationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    public int getBackSideColor() {
        return mBackSideColor;
    }

    public void setBackSideColor(@ColorInt int backSideColor) {
        mBackSideColor = backSideColor;
    }

    public int getAdditionalFlipsCount() {
        return mAdditionalFlipsCount;
    }

    public void setAdditionalFlipsCount(int additionalFlipsCount) {
        mAdditionalFlipsCount = additionalFlipsCount;
    }

    public int getCameraHeight() {
        return mCameraHeight;
    }

    public void setCameraHeight(int cameraHeight) {
        mCameraHeight = cameraHeight;
    }
}
