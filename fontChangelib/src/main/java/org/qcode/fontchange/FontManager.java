package org.qcode.fontchange;

import android.widget.TextView;

import com.superapps.view.TypefacedTextView;

import org.qcode.fontchange.impl.ActivityFontEventHandlerImpl;
import org.qcode.fontchange.impl.FontManagerImpl;
import org.qcode.fontchange.impl.FontViewHelperImpl;

/**
 * 字体大小调节框架对外接口
 * qqliu
 * 2016/10/8.
 */

public class FontManager {
    /***
     * 获取字体管理类实例
     * @return
     */
    public static FontManager getInstance() {
        return FontManagerImpl.getInstance();
    }

    /***
     * 获取View的字体属性管理类
     * @param view
     * @return
     */
    public static FontViewHelperImpl with(TypefacedTextView view) {
        return new FontViewHelperImpl(view);
    }

    /***
     * 创建一个新的Activity的字体事件处理器
     * @return
     */
    public static ActivityFontEventHandlerImpl newActivityFontEventHandler() {
        return new ActivityFontEventHandlerImpl();
    }
}
