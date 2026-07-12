package android.content;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * 最小模拟 —— 仅支持 setPrimaryClip
 */
public class ClipboardManager {

    private static final ClipboardManager INSTANCE = new ClipboardManager();

    private ClipData primaryClip;

    private ClipboardManager() {}

    public static ClipboardManager getInstance() {
        return INSTANCE;
    }

    /**
     * 设置剪贴板内容
     * 同时写入系统剪贴板（PC 端真正生效的一步）
     */
    public void setPrimaryClip(ClipData clip) {
        this.primaryClip = clip;

        // ===== 写入 PC 系统剪贴板 =====
        String text = (String) clip.getItemAt(0);
        if (text != null) {
            StringSelection selection = new StringSelection(text);
            Clipboard sysClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            sysClipboard.setContents(selection, null);
        }
    }

    /**
     * 获取剪贴板内容
     */
    public ClipData getPrimaryClip() {
        return primaryClip;
    }

    /**
     * 判断剪贴板是否有文本
     */
    public boolean hasPrimaryClip() {
        return primaryClip != null && primaryClip.getItemCount() > 0;
    }
}