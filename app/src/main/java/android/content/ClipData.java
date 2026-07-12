package android.content;

/**
 * 最小模拟 —— 仅支持 newPlainText / getText
 */
public class ClipData {

    private final String label;
    private final String text;

    private ClipData(String label, String text) {
        this.label = label;
        this.text = text;
    }

    /**
     * 创建纯文本剪贴数据
     * @param label 数据来源标签（无实际影响，仅标记）
     * @param text  要复制的文本内容
     */
    public static ClipData newPlainText(String label, String text) {
        return new ClipData(label, text);
    }

    public String getLabel() {
        return label;
    }

    /**
     * 获取剪贴板中的文本（取第一个 item 的文本）
     */
    public CharSequence getItemAt(int index) {
        return index == 0 ? text : null;
    }

    public int getItemCount() {
        return text != null ? 1 : 0;
    }
}