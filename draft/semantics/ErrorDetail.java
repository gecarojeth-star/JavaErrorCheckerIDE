package core.semantics;

import java.awt.Color;
import utils.Theme;

public class ErrorDetail {
    public enum Severity { ERROR, WARNING, INFO }

    public final Severity severity;
    public final String category;
    public final String message;
    public final String hint;
    public final int line;
    public final int startOffset;
    public final int endOffset;

    public ErrorDetail(Severity severity, String category, String message, String hint,
                       int line, int startOffset, int endOffset) {
        this.severity = severity;
        this.category = category;
        this.message = message;
        this.hint = hint;
        this.line = line;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public Color getColor() {
        switch (severity) {
            case ERROR: return Theme.DANGER;
            case WARNING: return new Color(0xf1fa8c); // WARNING_CLR
            default: return Theme.TEXT_MUTED;
        }
    }
}