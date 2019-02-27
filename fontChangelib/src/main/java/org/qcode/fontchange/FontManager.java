package org.qcode.fontchange;

import com.superapps.view.TypefacedTextView;

import org.qcode.fontchange.impl.ActivityFontEventHandlerImpl;
import org.qcode.fontchange.impl.FontManagerImpl;
import org.qcode.fontchange.impl.FontViewHelperImpl;

public class FontManager {
    public static final String MESSAGE_FONT_SCALE = "message_font_scale";

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
