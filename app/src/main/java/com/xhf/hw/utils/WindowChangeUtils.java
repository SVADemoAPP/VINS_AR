package com.xhf.hw.utils;

import android.view.WindowManager;

import com.xhf.hw.ui.base.BaseActivity;


public class WindowChangeUtils {
    /**
     * 更改窗口透明度
     *
     * @param context 上下文对象
     * @param alfa    透明度 采用float类型    弹出时候一般用0.7f  隐藏的时候恢复正常1f
     */
    public static void changeWindowAlfa(final BaseActivity context, final float alfa) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WindowManager.LayoutParams params = context.getWindow().getAttributes();
                if (alfa == 1f) {
                    while (params.alpha < alfa) {
                        try {
                            Thread.sleep(4); //精度越高，变化越明显
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        params.alpha += 0.01f;
                        if (params.alpha > 1f) {
                            params.alpha = 1f;
                        }
                        final WindowManager.LayoutParams params2 = params;
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                context.getWindow().setAttributes(params2);
                            }
                        });
                    }
                } else if (alfa < 1f) {
                    while (params.alpha > alfa) {
                        try {
                            Thread.sleep(4); //精度越高，变化越明显
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        params.alpha -= 0.01f;
                        final WindowManager.LayoutParams params2 = params;
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                context.getWindow().setAttributes(params2);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    /**
     * 设置透明度（渐变暗色）
     *
     * @param alfa
     */
    public static void setBlack(final BaseActivity context, final float alfa) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WindowManager.LayoutParams params = context.getWindow().getAttributes();
                while (params.alpha > alfa) {
                    try {
                        Thread.sleep(4); //精度越高，变化越明显
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    params.alpha -= 0.01f;
                    final WindowManager.LayoutParams params2 = params;
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            context.getWindow().setAttributes(params2);
                        }
                    });
                }
            }
        }).start();
    }

    /***
     * 设置透明度 渐变亮色
     * @param context
     * @param alfa
     */
    public static void setLight(final BaseActivity context, final float alfa) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WindowManager.LayoutParams params = context.getWindow().getAttributes();
                while (params.alpha < alfa) {
                    try {
                        Thread.sleep(4); //精度越高，变化越明显
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    params.alpha += 0.01f;
                    if (params.alpha > 1f) {
                        params.alpha = 1f;
                    }
                    final WindowManager.LayoutParams params2 = params;
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            context.getWindow().setAttributes(params2);
                        }
                    });
                }
            }
        }).start();
    }
}