package net.yoojia.imagemap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import com.caverock.androidsvg.SVG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yoojia.imagemap.core.CollectPointShape;
import net.yoojia.imagemap.core.LineShape;
import net.yoojia.imagemap.core.MoniPointShape;
import net.yoojia.imagemap.core.PushMessageShape;
import net.yoojia.imagemap.core.Shape;
import net.yoojia.imagemap.core.ShapeExtension;
import net.yoojia.imagemap.core.SpecialShape;
import net.yoojia.imagemap.core.PrruInfoShape;
import net.yoojia.imagemap.util.MatrixConverHelper;
import net.yoojia.imagemap.util.SvgHelper;
import net.yoojia.imagemap.util.SvgHelper.SvgPath;

public class HighlightImageView1 extends TouchImageView1 implements ShapeExtension {
    private static final int SVGTOPATH_OK = 1;
    private Context context;
    public boolean isAllowRequestTranslate;
    public boolean isFiter;
    public boolean isOnArea;
    private LineShape lineShape;
    private Shape longClickShape;
    public int mFiterColor;
    private SVG mSvgData;
    private List<SvgPath> mSvgPaths;
    private Handler mhandler;
    private OnShapeActionListener onShapeClickListener;
    private PrruInfoShape pInfoShapeNew;
    private Map<Object, Shape> shapesCache;

    private PrruInfoShape templePrru = null;

    private float startX = 0f;
    private float startY = 0f;
    private boolean rsrpFlag = false;

    public void setAllRsrpListener(final AllRsrpMoveListener allRsrpListener) {

        setMoveAllRsRpListener(new MoveAllRsRpListener() {
            @Override
            public void startTranslate(float x, float y) {
                startX = x;
                startY = y;
                LogUtils.e("测试拖动", "start x" + x + " , y =" + y);
                for (Shape shape : shapesCache.values()) {  //判断是否有shape被点中
                    if (shape.inArea(x, y)) {
                        LogUtils.e("测试拖动", "点击shape");
                        rsrpFlag = true;
                        break;
                    }
                }
            }

            @Override
            public void moveTranslate(float x, float y) {
                LogUtils.e("测试拖动", "move");
                if (rsrpFlag) {
                    LogUtils.e("测试拖动", "move x" + x + " , y =" + y);
                    allRsrpListener.move(x -startX, y - startY);
                }
            }

            @Override
            public void endTranslate(float x, float y) {
                LogUtils.e("测试拖动", "end");
                if (rsrpFlag) {
                    LogUtils.e("测试拖动", "end x" + x + " , y =" + y);
                    allRsrpListener.end(x - startX, y - startY);
                    rsrpFlag = false;
                }
            }
        });
    }


    public void setPRRUMoveHListener(final PrruModifyHListener hListener) {
        setPRRUMoveListener(new PrruModifyListener() {
            @Override
            public void startTranslate(float x, float y) {
                boolean flag = false;

                for (Shape shape : shapesCache.values()) {
                    if (shape.inArea(x, y) && shape instanceof PrruInfoShape) {
                        flag = true;
                        templePrru = (PrruInfoShape) shape;
                        hListener.startTranslate((PrruInfoShape) shape, x, y);

                    }
                }
                if (!flag) {
                    hListener.clickBlank();
                }
            }

            @Override
            public void moveTranslate(float x, float y) {
                if (templePrru != null) {
                    hListener.moveTranslate(templePrru, x, y);
                }
            }

            @Override
            public void endTranslate(float x, float y) {
                if (templePrru != null) {
                    hListener.endTranslate(templePrru, x, y);
                }
                templePrru = null;
            }

            @Override
            public void touchOutside(float x, float y) {  //判断是否是除开prru的外部包括图片以外
                boolean flag = false;
                for (Shape shape : shapesCache.values()) {
                    if (shape.inArea(x, y) && shape instanceof PrruInfoShape) {
                        flag = true;
                    }
                }
                if (!flag) {
                    hListener.clickOutSide();
                }
            }
        });
    }

    public int getmFiterColor() {
        return this.mFiterColor;
    }

    public void setmFiterColor(int mFiterColor) {
        this.mFiterColor = mFiterColor;
    }

    public boolean isOnArea() {
        return this.isOnArea;
    }

    public void setOnArea(boolean isOnArea) {
        this.isOnArea = isOnArea;
    }

    public HighlightImageView1(Context context) {
        this(context, null);
        this.context = context;
    }

    public HighlightImageView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.shapesCache = new HashMap(0);
        this.mSvgPaths = new ArrayList();
        this.mhandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    HighlightImageView1.this.bindPathToShape(HighlightImageView1.this.getShapes(), HighlightImageView1.this.mSvgPaths);
                }
            }
        };
        this.isFiter = false;
        this.isAllowRequestTranslate = false;
    }

    private void bindPathToShape(final List<Shape> shapes, final List<SvgPath> svgPaths) {
        if (svgPaths.size() != 0 && shapes.size() != 0) {
            new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < shapes.size(); i++) {
                        int z = shapes.size();
                        Shape shape = (Shape) shapes.get(i);
                        if (shape instanceof SpecialShape) {
                            SpecialShape ss = (SpecialShape) shape;
                            if (ss.getSvgPath() == null) {
                                for (int j = 0; j < svgPaths.size(); j++) {
                                    SvgPath p = (SvgPath) svgPaths.get(j);
                                    if (p.isArea(ss.getCenterX(), ss.getCenterY())) {
                                        ss.setSvgPath(p);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }).start();
        }
    }

    private void bindPathToShape(SpecialShape shape, List<SvgPath> svgPaths) {
        for (int j = 0; j < svgPaths.size(); j++) {
            SvgPath p = (SvgPath) svgPaths.get(j);
            if (p.isArea(shape.getCenterX(), shape.getCenterY())) {
                shape.setSvgPath(p);
                return;
            }
        }
    }

    public boolean isAllowRequestTranslate() {
        return this.isAllowRequestTranslate;
    }

    public void setAllowRequestTranslate(boolean isAllowRequestTranslate) {
        this.isAllowRequestTranslate = isAllowRequestTranslate;
    }

    public void setSvg(final SVG svg) {
        if (svg != null) {
            clearShapes();
            this.mSvgData = svg;
            new Thread(new Runnable() {
                public void run() {
                    SvgHelper svgHelper = new SvgHelper(new Paint());
                    svgHelper.load(HighlightImageView1.this.mSvgData);
                    HighlightImageView1.this.mSvgPaths = svgHelper.getPathsForViewport((int) svg.getDocumentWidth(), (int) svg.getDocumentHeight());
                    HighlightImageView1.this.mhandler.sendEmptyMessage(1);
                }
            }).start();
        }
    }

    public void setOnShapeClickListener(OnShapeActionListener onShapeClickListener) {
        this.onShapeClickListener = onShapeClickListener;
    }

    public void addShape(Shape shape, boolean isMove) {
        this.shapesCache.put(shape.tag, shape);
        if (shape instanceof SpecialShape) {
            bindPathToShape((SpecialShape) shape, this.mSvgPaths);
        }
        if (isMove) {
            requestTranslate(new PointF(shape.getCenterX(), shape.getCenterY()));
        }
        postInvalidate();
    }

    public void removeShape(Object tag) {
        if (this.shapesCache.containsKey(tag)) {
            this.shapesCache.remove(tag);
            postInvalidate();
        }
    }

    public Shape getShape(Object tag) {
        if (this.shapesCache.containsKey(tag)) {
            return (Shape) this.shapesCache.get(tag);
        }
        return null;
    }

    public void clearShapes() {
        this.shapesCache.clear();
        this.mSvgPaths.clear();
        postInvalidate();
    }

    public List<Shape> getShapes() {
        return new ArrayList(this.shapesCache.values());
    }

    protected void onDraw(Canvas canvas) {
        Matrix matrix = new Matrix(getImageMatrix());
        super.onDraw(canvas);
        float scale = getScale();
        Shape temple = null;
        for (Shape shape : this.shapesCache.values()) {
            shape.postMatrixCahnge(matrix);
            if (!shape.tag.equals("loc")) {
                shape.setScale(scale);
                shape.onDraw(canvas);
            } else {
                temple = shape;
            }
        }
        if (temple != null) {
            temple.setScale(scale);
            temple.onDraw(canvas);
            temple = null;
        }
        postInvalidate();
        onDrawWithCanvas(canvas);
    }

    private void requestTranslate(PointF point) {
        if (!this.mMapHandle.isAniming()) {
            point = MatrixConverHelper.mapMatrixPoint(getImageMatrix(), point.x, point.y);
            float dx = (((float) this.mView_width) / 2.0f) - point.x;
            float dy = (((float) this.mView_height) / 2.0f) - point.y;
            if (Math.abs(dx) >= 4.0f || Math.abs(dy) >= 4.0f) {
                translateOffset(dx, dy);
            }
        }
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
    }

    protected void onDrawWithCanvas(Canvas canvas) {
    }

    protected Shape onViewLongClickShape(float xOnView, float yOnView) {
        this.longClickShape = null;
        for (Shape shape : this.shapesCache.values()) {
            if (shape.inArea(xOnView, yOnView) && (shape instanceof PrruInfoShape)) {
                this.longClickShape = shape;
                break;
            }
        }
        return this.longClickShape;
    }

    protected boolean onViewLongClickShapeMove(float xOnView, float yOnView) {
        if (!(this.longClickShape == null || this.pInfoShapeNew == null)) {
            this.pInfoShapeNew.setValues(xOnView, yOnView);
            this.lineShape.setValues(this.pInfoShapeNew.getCenterX(), this.pInfoShapeNew.getCenterY(), this.longClickShape.getCenterX(), this.longClickShape.getCenterY(), 1.0f);
        }
        return true;
    }

    public void autoShowView(Shape shape) {
        if (this.onShapeClickListener != null) {
            this.onShapeClickListener.onPrruInfoShapeClick((PrruInfoShape) shape, shape.getCenterX(), shape.getCenterY());
        }
    }

    public boolean onViewClick(float xOnView, float yOnView) {
        for (Shape shape : this.shapesCache.values()) {
            if (shape.inArea(xOnView, yOnView)) {
                if (shape instanceof SpecialShape) {
                    if (this.onShapeClickListener != null) {
                        this.onShapeClickListener.onSpecialShapeClick((SpecialShape) shape, shape.getCenterX(), shape.getCenterY());
                    }
                } else if (shape instanceof PushMessageShape) {
                    if (this.onShapeClickListener != null) {
                        this.onShapeClickListener.onPushMessageShapeClick((PushMessageShape) shape, shape.getCenterX(), shape.getCenterY());
                    }
                } else if (shape instanceof CollectPointShape) {
                    if (this.onShapeClickListener != null) {
                        this.onShapeClickListener.onCollectShapeClick((CollectPointShape) shape, shape.getCenterX(), shape.getCenterY());
                    }
                } else if (shape instanceof MoniPointShape) {
                    if (this.onShapeClickListener != null) {
                        this.onShapeClickListener.onMoniShapeClick((MoniPointShape) shape, shape.getCenterX(), shape.getCenterY());
                    }
                } else if ((shape instanceof PrruInfoShape) && this.onShapeClickListener != null) {
                    this.onShapeClickListener.onPrruInfoShapeClick((PrruInfoShape) shape, shape.getCenterX(), shape.getCenterY());
                }
                PointF point = new PointF(shape.getCenterX(), shape.getCenterY());
                point = MatrixConverHelper.mapMatrixPoint(getImageMatrix(), point.x, point.y);
                if (this.isAllowTranslate) {
                    translateOffset((((float) this.mView_width) / 2.0f) - point.x, (((float) this.mView_height) / 2.0f) - point.y);
                }
                return true;
            }
        }
        if (this.onShapeClickListener != null) {
            this.onShapeClickListener.outShapeClick(xOnView, yOnView);
        }
        return false;
    }

    public interface PrruModifyHListener {
        void startTranslate(PrruInfoShape shape, float x, float y);

        void moveTranslate(PrruInfoShape shape, float x, float y);

        void endTranslate(PrruInfoShape shape, float x, float y);

        void clickBlank();

        void clickOutSide();

    }

    /***
     * 监听rsrp移动事件
     */
    public interface AllRsrpMoveListener {
        void move(float x, float y);

        void end(float x, float y);
    }


}
