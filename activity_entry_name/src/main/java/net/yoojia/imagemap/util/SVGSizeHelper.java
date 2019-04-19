package net.yoojia.imagemap.util;
/**
 * Created by yipenga on 2015/12/22.
 */
public class SVGSizeHelper {
    /**
     * 获取Bitmap适配屏幕的宽高的比例
     * (默认取->view width/bitmap width 与 view height/bitmap height的较小值)
     *
     * @param view_width
     * @param view_height
     * @param dw
     * @param dh
     * @return
     */
    public static float getBitmapMatchScale(int view_width, int view_height, float dw, float dh) {
        float scale = 1f;
        if (view_width > dw && view_height < dh) {
            scale = view_height * 1f / dh;
        }
        if (view_width < dw && view_height > dh) {
            scale = view_width * 1f / dw;
        }
        if ((view_width < dw && view_height < dh) || (view_width > dw && view_height > dh)) {
            scale = Math.min(view_height * 1f / dh, view_width * 1f / dw);
        }
        return scale;
    }

    /**
     * 获取Bitmap适配屏幕的宽高的比例 并乘以自定义比例
     * (默认取->view width/bitmap width 与 view height/bitmap height的较小值)
     *
     * @param view_width
     * @param view_height
     * @param dw
     * @param dh
     * @return
     */
    public static float getBitmapMatchScale(int view_width, int view_height, int dw, int dh, float scale) {
        return scale * getBitmapMatchScale(view_width, view_height, dw, dh);
    }

}
