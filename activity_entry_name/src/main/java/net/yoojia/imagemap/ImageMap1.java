package net.yoojia.imagemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.caverock.androidsvg.SVG;

import net.yoojia.imagemap.TouchImageView1.MyOnTouchListener;
import net.yoojia.imagemap.TouchImageView1.OnLongClickListener1;
import net.yoojia.imagemap.TouchImageView1.OnRotateListener;
import net.yoojia.imagemap.TouchImageView1.OnSingleClickListener;
import net.yoojia.imagemap.core.Bubble;
import net.yoojia.imagemap.core.Bubble.RenderDelegate;
import net.yoojia.imagemap.core.CollectPointShape;
import net.yoojia.imagemap.core.MoniPointShape;
import net.yoojia.imagemap.core.PushMessageShape;
import net.yoojia.imagemap.core.Shape;
import net.yoojia.imagemap.core.ShapeExtension;
import net.yoojia.imagemap.core.ShapeExtension.OnShapeActionListener;
import net.yoojia.imagemap.core.SpecialShape;
import net.yoojia.imagemap.core.PrruInfoShape;
import net.yoojia.imagemap.support.TranslateAnimation.OnAnimationListener;
import net.yoojia.imagemap.util.ImageViewHelper;
import net.yoojia.imagemap.util.ImageViewHelper.OnMaxZoomCallback;
import net.yoojia.imagemap.util.ImageViewHelper.OnMinZoomCallback;

public class ImageMap1 extends FrameLayout implements ShapeExtension, OnShapeActionListener, OnAnimationListener {
    private Bubble bubble;
    private PointF defaultPoint;
    private HighlightImageView1 highlightImageView;
    private Context mContext;
    private OnShapeActionListener mOnShapeClickListener;
    private View view;
    private View viewForAnimation;
    private boolean showBubble = true;

    public void setTranlateFlag(boolean flag) {
        highlightImageView.setTranlateFlag(flag);
    }

    public boolean getShowBubble() {
        return showBubble;
    }

    public void setShowBubble(boolean showBubble) {
        this.showBubble = showBubble;
    }

    public void setPrruListener(HighlightImageView1.PrruModifyHListener prruModifyListener) {
        highlightImageView.setPRRUMoveHListener(prruModifyListener);
    }

    public void setRsrpMoveListener(HighlightImageView1.AllRsrpMoveListener rsrpMoveListener) {
        highlightImageView.setAllRsrpListener(rsrpMoveListener);
    }

    public ImageMap1(Context context) {
        this(context, null);
        this.mContext = context;
    }

    public ImageMap1(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.defaultPoint = new PointF(0.0f, 0.0f);
        initialImageView(context);
    }

    public ImageMap1(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.defaultPoint = new PointF(0.0f, 0.0f);
        initialImageView(context);
    }

    public HighlightImageView1 getHighlightImageView() {
        return this.highlightImageView;
    }

    private void initialImageView(Context context) {
        this.mContext = context;
        this.highlightImageView = new HighlightImageView1(context);
        this.highlightImageView.setOnShapeClickListener(this);
        addView(this.highlightImageView, new LayoutParams(-1, -1));
        this.viewForAnimation = new View(context);
        addView(this.viewForAnimation, 0, 0);
    }

    public void autoShowBubbleView(Shape shape) {
        this.highlightImageView.autoShowView(shape);
    }

    public void setBubbleView(View bubbleView, RenderDelegate renderDelegate) {
        if (bubbleView == null) {
            throw new IllegalArgumentException("View for bubble cannot be null !");
        }
        this.view = bubbleView;
        this.bubble = new Bubble(this.view);
        this.bubble.setRenderDelegate(renderDelegate);
        addView(this.bubble);
        bubbleView.setVisibility(View.GONE);
    }

    public void addShapeAndRefToBubble(Shape shape, boolean isMove) {
        addShape(shape, isMove);
        if (this.bubble != null) {
            shape.createBubbleRelation(this.bubble);
        }
    }

    public void onTranslate(float deltaX, float deltaY) {
        this.highlightImageView.postMatrixTranslate(deltaX, deltaY);
        this.highlightImageView.postImageMatrix();
    }

    public void addShape(Shape shape, boolean isMove) {
        this.highlightImageView.addShape(shape, isMove);
    }

    public Shape getShape(Object tag) {
        return this.highlightImageView.getShape(tag);
    }

    public void removeShape(Object tag) {
        this.highlightImageView.removeShape(tag);
    }

    public void clearShapes() {
        for (Shape item : this.highlightImageView.getShapes()) {
            item.cleanBubbleRelation();
        }
        this.highlightImageView.clearShapes();
        if (this.bubble != null) {
            this.bubble.view.setVisibility(View.GONE);
        }
    }

    public void setMapBitmap(Bitmap bitmap) {
        this.highlightImageView.setImageBitmap(bitmap);
    }

    public void setMapPicture(Picture picture) {
        setMapDrawable(new PictureDrawable(picture));
    }

    public void setMapDrawable(Drawable d) {
        this.highlightImageView.setImageDrawable(d);
    }

    public float getLastOffsetX() {
        return this.highlightImageView.getLastOffsetX();
    }

    public float getLastOffsetY() {
        return this.highlightImageView.getLastOffsetY();
    }

    public void reset() {
        removeAllViews();
        initialImageView(this.mContext);
    }

    public PointF getBubblePosition() {
        if (this.bubble != null) {
            return this.bubble.getBubblePosition();
        }
        return this.defaultPoint;
    }

    public View getBubble() {
        return view;
    }

    public float getZoom() {
        return this.highlightImageView.getScale();
    }

    public float getMinScale() {
        return this.highlightImageView.getHelper().getMinZoomScale();
    }

    public boolean isOnArea() {
        return this.highlightImageView.isOnArea();
    }

    public void outShapeClick(float xOnImage, float yOnImage) {
        if (this.mOnShapeClickListener != null) {
            this.mOnShapeClickListener.outShapeClick(xOnImage, yOnImage);
            for (Shape item : this.highlightImageView.getShapes()) {
                item.cleanBubbleRelation();
            }
            if (this.bubble != null) {
                this.view.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void setSvg(SVG svg) {
        this.highlightImageView.setSvg(svg);
    }

    public void pullScale() {
        this.highlightImageView.pullScale();
    }

    public void zoomScale() {
        this.highlightImageView.zoomScale();
    }

    public ImageViewHelper getHelper() {
        return this.highlightImageView.getHelper();
    }

    public void setMaxCallback(OnMaxZoomCallback callback) {
        getHelper().setOnMaxZoomScaleCallback(callback);
    }

    public void setMinCallback(OnMinZoomCallback callback) {
        getHelper().setOnMinZoomScaleCallback(callback);
    }

    public void setOnRotateListener(OnRotateListener listener) {
        this.highlightImageView.setOnRotateListener(listener);
    }

    public void setOnMyTouchClickListener(MyOnTouchListener listener) {
        this.highlightImageView.setOnMyTouchListener(listener);
    }

    public void setOnSingleClickListener(OnSingleClickListener listener) {
        this.highlightImageView.setOnSingleClickListener(listener);
    }

    public void setOnLongClickListener1(OnLongClickListener1 listener) {
        this.highlightImageView.setOnLongClickListener(listener);
    }

    public void setAllowRotate(boolean allow) {
        this.highlightImageView.setAllowRotate(allow);
    }

    public void releaseImageShow() {
        this.highlightImageView.releaseImageShow();
    }

    public PointF getCenterByImagePoint() {
        return this.highlightImageView.getCenterByImage();
    }

    public void setAllowRequestTranslate(boolean isAllow) {
        this.highlightImageView.setAllowRequestTranslate(isAllow);
    }

    public boolean isAllowRotate() {
        return this.highlightImageView.isAllowRotate();
    }

    public boolean isAllowTranslate() {
        return this.highlightImageView.isAllowTranslate();
    }

    public void setAllowTranslate(boolean isAllowTranslate) {
        this.highlightImageView.setAllowTranslate(isAllowTranslate);
    }

    public void setFiterColor(int color) {
        this.highlightImageView.setmFiterColor(color);
    }

    public void setFiter(boolean isFiter) {
        this.highlightImageView.isFiter = isFiter;
    }

    public void setOnShapeClickListener(OnShapeActionListener listener) {
        this.mOnShapeClickListener = listener;
    }

    public void onSpecialShapeClick(SpecialShape shape, float xOnImage, float yOnImage) {
        if (this.mOnShapeClickListener != null) {
            this.mOnShapeClickListener.onSpecialShapeClick(shape, xOnImage, yOnImage);
            for (Shape item : this.highlightImageView.getShapes()) {
                item.cleanBubbleRelation();
            }
            if (this.bubble != null) {
                this.bubble.showAtShape(shape);
                this.view.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onPushMessageShapeClick(PushMessageShape shape, float xOnImage, float yOnImage) {
        if (this.mOnShapeClickListener != null) {
            this.mOnShapeClickListener.onPushMessageShapeClick(shape, xOnImage, yOnImage);
            for (Shape item : this.highlightImageView.getShapes()) {
                item.cleanBubbleRelation();
            }
            if (this.bubble != null) {
                this.bubble.showAtShape(shape);
                this.view.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onCollectShapeClick(CollectPointShape shape, float xOnImage, float yOnImage) {
        if (this.mOnShapeClickListener != null) {
            this.mOnShapeClickListener.onCollectShapeClick(shape, xOnImage, yOnImage);
            for (Shape item : this.highlightImageView.getShapes()) {
                item.cleanBubbleRelation();
            }
            if (this.bubble != null) {
                this.bubble.showAtShape(shape);
                this.view.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onMoniShapeClick(MoniPointShape shape, float xOnImage, float yOnImage) {
        if (this.mOnShapeClickListener != null) {
            this.mOnShapeClickListener.onMoniShapeClick(shape, xOnImage, yOnImage);
            for (Shape item : this.highlightImageView.getShapes()) {
                item.cleanBubbleRelation();
            }
            if (this.bubble != null) {
                this.bubble.showAtShape(shape);
                this.view.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onPrruInfoShapeClick(PrruInfoShape shape, float xOnImage, float yOnImage) {
        if (this.mOnShapeClickListener != null) {
            mOnShapeClickListener.onPrruInfoShapeClick(shape, xOnImage, yOnImage);
            for (Shape item : this.highlightImageView.getShapes()) {
                item.cleanBubbleRelation();
            }
            if (this.bubble != null) {
                this.bubble.showAtShape(shape);
                if (showBubble) {
                    if (!shape.getMove()) {
                        this.view.setVisibility(View.GONE);    //xhf ar版本  gone
                    }
                }
            }
        }
    }


}
