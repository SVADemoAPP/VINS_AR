package net.yoojia.imagemap.core;

import android.graphics.PointF;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * author : chenyoca@gmail.com date : 13-5-19 The bubble wrapper.
 */
public class Bubble extends FrameLayout {

    // static final boolean IS_API_11_LATER = Build.VERSION.SDK_INT >=
    // Build.VERSION_CODES.HONEYCOMB;
    public final View view;
    private boolean push_msg = false;
    private float posY;
    public final PointF position = new PointF();
    private RenderDelegate renderDelegate;

    public Bubble(View view) {
        super(view.getContext());
        this.view = view;
        final int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
        LayoutParams params = new LayoutParams(
                wrapContent, wrapContent);
        view.setLayoutParams(params);
        view.setClickable(true);
        addView(view);
    }

    /**
     * Bubble界面渲染代理。用以处理Bubble界面的数据填充。
     */
    public interface RenderDelegate {
        void onDisplay(Shape shape, View bubbleView);
    }

    /**
     * 为Bubble界面设置一个渲染代理接口
     *
     * @param renderDelegate 渲染代理接口
     */
    public void setRenderDelegate(RenderDelegate renderDelegate) {
        this.renderDelegate = renderDelegate;
    }

    /**
     * Show the bubble view controller on the shape.
     *
     * @param shape the shape to show on
     */
    public void showAtShape(Shape shape) {
        if (view == null) {
            return;
        }
        shape.createBubbleRelation(this);
        if (!shape.bubbleTag()) {
            push_msg = true;
        } else {
            push_msg = false;
        }
        if (shape instanceof PrruInfoShape) { //判断是bubble是依赖于prru的时候进行位置调整
            PointF centerPoint = shape.getCenterPoint();
            setBubbleViewAtPosition(centerPoint.x+30,centerPoint.y-200);
        }else {
            setBubbleViewAtPosition(shape.getCenterPoint());
        }

        if (renderDelegate != null) {
            renderDelegate.onDisplay(shape, view);
        }
    }

    private PointF point = new PointF(0.0f, 0.0f);

    public PointF getBubblePosition() {
        return point;
    }

    private void setBubbleViewAtPosition(PointF center) {
        point = center;
        float posX = center.x - view.getWidth() / 2 - 5;
        if (push_msg) {
            posY = center.y - view.getHeight() - 115;

        } else {
            posY = center.y - view.getHeight();
        }
        setBubbleViewAtPosition(posX + 140, posY + 110);
    }

    private void setBubbleViewAtPosition(float x, float y) {
        // BUG : HTC SDK 2.3.3 界面会被不停的重绘,这个重绘请求是View.onDraw()方法发起的。
        if (position.equals(x, y)) {
            return;
        }
        position.set(x, y);
        // if(IS_API_11_LATER){
        // view.setX(x);
        // view.setY(y);
        // }else{
        LayoutParams params = (LayoutParams) view
                .getLayoutParams();
        int left = new Float(x).intValue();
        int top = new Float(y).intValue();
        // HTC SDK 2.3.3 Required
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.TOP;
        params.leftMargin = left;
        params.topMargin = top;
        view.setLayoutParams(params);
        // }
    }

}
