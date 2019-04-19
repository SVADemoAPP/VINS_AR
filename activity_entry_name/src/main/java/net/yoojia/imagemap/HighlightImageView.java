package net.yoojia.imagemap;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.yoojia.imagemap.core.Shape;
import net.yoojia.imagemap.core.ShapeExtension;
import net.yoojia.imagemap.core.ShapeExtension.OnShapeActionListener;

public class HighlightImageView extends TouchImageView implements ShapeExtension {
    private OnShapeActionListener onShapeClickListener;
    private Map<Object, Shape> shapesCache;

    public HighlightImageView(Context context) {
        this(context, null);
    }

    public HighlightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.shapesCache = new HashMap(0);
    }

    public void setOnShapeClickListener(OnShapeActionListener onShapeClickListener) {
        this.onShapeClickListener = onShapeClickListener;
    }

    public void addShape(Shape shape, boolean isMove) {
        this.shapesCache.put(shape.tag, shape);
        postInvalidate();
    }

    public void removeShape(Object tag) {
        if (this.shapesCache.containsKey(tag)) {
            this.shapesCache.remove(tag);
            postInvalidate();
        }
    }

    public boolean getShape(Object tag) {
        if (this.shapesCache.containsKey(tag)) {
            return true;
        }
        return false;
    }

    public void clearShapes() {
        this.shapesCache.clear();
    }

    public List<Shape> getShapes() {
        return new ArrayList(this.shapesCache.values());
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Shape shape : this.shapesCache.values()) {
            shape.onDraw(canvas);
        }
        onDrawWithCanvas(canvas);
    }

    protected void onDrawWithCanvas(Canvas canvas) {
    }

    protected void onViewClick(float xOnView, float yOnView) {
        if (this.onShapeClickListener != null) {
            Iterator it = this.shapesCache.values().iterator();
            while (it.hasNext() && !((Shape) it.next()).inArea(xOnView, yOnView)) {
                this.onShapeClickListener.outShapeClick(xOnView, yOnView);
            }
        }
    }

    protected void postScale(float scaleFactor, float scaleCenterX, float scaleCenterY) {
        super.postScale(scaleFactor, scaleCenterX, scaleCenterY);
        if (scaleFactor != 0.0f) {
            for (Shape shape : this.shapesCache.values()) {
                if (scaleFactor != 0.0f) {
                    shape.onScale(scaleFactor, scaleCenterX, scaleCenterY);
                }
            }
        }
    }

    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        if (deltaX != 0.0f || deltaY != 0.0f) {
            for (Shape shape : this.shapesCache.values()) {
                shape.onTranslate(deltaX, deltaY);
            }
        }
    }
}
