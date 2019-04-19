package net.yoojia.imagemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import net.yoojia.imagemap.core.Bubble;
import net.yoojia.imagemap.core.Bubble.RenderDelegate;
import net.yoojia.imagemap.core.Shape;
import net.yoojia.imagemap.core.ShapeExtension;
import net.yoojia.imagemap.support.TranslateAnimation;
import net.yoojia.imagemap.support.TranslateAnimation.OnAnimationListener;

public class ImageMap extends FrameLayout implements ShapeExtension, OnAnimationListener {
    private Bubble bubble;
    private PointF defaultPoint;
    private HighlightImageView highlightImageView;
    private Context mContext;
    private View view;
    private View viewForAnimation;

    public ImageMap(Context context) {
        this(context, null);
        this.mContext = context;
    }

    public ImageMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.defaultPoint = new PointF(0.0f, 0.0f);
        initialImageView(context);
    }

    public ImageMap(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.defaultPoint = new PointF(0.0f, 0.0f);
        initialImageView(context);
    }

    private void initialImageView(Context context) {
        this.mContext = context;
        this.highlightImageView = new HighlightImageView(context);
        addView(this.highlightImageView, new LayoutParams(-1, -1));
        this.viewForAnimation = new View(context);
        addView(this.viewForAnimation, 0, 0);
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
        this.highlightImageView.moveBy(deltaX, deltaY);
    }

    public void addShape(Shape shape, boolean isMove) {
        shape.onScale(this.highlightImageView.getScale());
        PointF from = this.highlightImageView.getAbsoluteCenter();
        PointF to = shape.getCenterPoint();
        TranslateAnimation movingAnimation = new TranslateAnimation(from.x, to.x, from.y, to.y);
        movingAnimation.setOnAnimationListener(this);
        movingAnimation.setInterpolator(new DecelerateInterpolator());
        movingAnimation.setDuration(500);
        movingAnimation.setFillAfter(true);
        if (isMove) {
            this.viewForAnimation.startAnimation(movingAnimation);
        }
        PointF offset = this.highlightImageView.getAbsoluteOffset();
        shape.onTranslate(offset.x, offset.y);
        this.highlightImageView.addShape(shape, isMove);
    }

    public PointF getLocation() {
        PointF offset = this.highlightImageView.getAbsoluteOffset();
        return new PointF(offset.x, offset.y);
    }

    public boolean getShape(Object tag) {
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

    public PointF getCenter() {
        float scale = this.highlightImageView.getScale();
        PointF center = this.highlightImageView.getAbsoluteCenter();
        return new PointF(center.x / scale, center.y / scale);
    }

    public float getZoom() {
        return this.highlightImageView.getScale();
    }

    public float getMinScale() {
        return this.highlightImageView.getMinScale();
    }

    public PointF getPoint() {
        float scale = this.highlightImageView.getScale();
        PointF center = this.highlightImageView.getPoints();
        return new PointF(center.x / scale, center.y / scale);
    }

    public void setBoo(boolean b) {
        this.highlightImageView.setBoolean(b);
    }

    public void setZoom(float scale, float x, float y) {
        this.highlightImageView.setZoom(scale, x, y);
    }

    public float getMesure() {
        return this.highlightImageView.getMesure();
    }
}
