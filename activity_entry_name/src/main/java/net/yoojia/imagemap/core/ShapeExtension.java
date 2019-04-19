package net.yoojia.imagemap.core;

public interface ShapeExtension {

    public interface OnShapeActionListener {
        void onCollectShapeClick(CollectPointShape collectPointShape, float f, float f2);

        void onMoniShapeClick(MoniPointShape moniPointShape, float f, float f2);

        void onPrruInfoShapeClick(PrruInfoShape prruinfoshape, float f, float f2);

        void onPushMessageShapeClick(PushMessageShape pushMessageShape, float f, float f2);

        void onSpecialShapeClick(SpecialShape specialShape, float f, float f2);

        void outShapeClick(float f, float f2);
    }

    void addShape(Shape shape, boolean z);

    void clearShapes();

    void removeShape(Object obj);
}
