package org.qcode.fontchange;

/**
 * 加载字体大小调节过程的事件回调
 * qqliu
 * 2016/9/24.
 */
public interface IFontChangeListener {
    /***
     * 加载字体开始
     */
    void onLoadStart(float scale);
    void onLoadStart(String path);

    /***
     * 加载字体完成
     */
    void onLoadSuccess(float scale);
    void onLoadSuccess(String path);

    /***
     * 加载字体失败
     */
    void onLoadFail(float scale);
    void onLoadFail(String path);
}
