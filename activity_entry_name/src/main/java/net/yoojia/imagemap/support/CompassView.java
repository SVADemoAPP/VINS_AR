package net.yoojia.imagemap.support;

/* Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * �Զ���һ��View�̳�ImageView������һ��ͨ�õ���תͼƬ��Դ�ķ���
 * 
 * @author way
 * 
 */
public class CompassView extends ImageView
{
    private float mDirection;// ������ת������
    private Drawable compass;// ͼƬ��Դ

    // ����������
    public CompassView(Context context)
    {
        super(context);
        mDirection = 0.0f;// Ĭ�ϲ���ת
        compass = null;
    }

    public CompassView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mDirection = 0.0f;
        compass = null;
    }

    public CompassView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mDirection = 0.0f;
        compass = null;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (compass == null)
        {
            compass = getDrawable();// ��ȡ��ǰview��ͼƬ��Դ
            compass.setBounds(0, 0, getWidth(), getHeight());// ͼƬ��Դ��view��λ�ã��˴��൱�ڳ���view
        }

        canvas.save();
        canvas.rotate(mDirection, getWidth() / 2, getHeight() / 2);// ��ͼƬ���ĵ���ת��
        compass.draw(canvas);// ����ת���ͼƬ����view�ϣ���������ת�������
        canvas.restore();// ����һ��
    }

    /**
     * �Զ�����·���ķ���
     * 
     * @param direction
     *            ����ķ���
     */
    public void updateDirection(float direction)
    {
        mDirection = direction;
        invalidate();// ����ˢ��һ�£����·���
    }

}