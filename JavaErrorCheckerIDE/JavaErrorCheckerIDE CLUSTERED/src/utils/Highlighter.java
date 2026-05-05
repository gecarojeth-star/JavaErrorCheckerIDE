package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextPane; // Added missing import
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import tokenrepository.KeywordToken;

public class Highlighter {

    public static void apply(JTextPane pane) {
        // SwingUtilities ensures UI updates happen on the correct thread to avoid flickering/crashes
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = pane.getStyledDocument();
                String text = doc.getText(0, doc.getLength());
                
                // 1. Reset default style to base text color
                Style def = pane.addStyle("def", null); 
                StyleConstants.setForeground(def, Theme.TEXT);
                doc.setCharacterAttributes(0, text.length(), def, true);
                
                // 2. Define Theme Styles for different token types
                Style kw = pane.addStyle("kw", null); 
                StyleConstants.setForeground(kw, Theme.PRIMARY); 
                StyleConstants.setBold(kw, true);
                
                Style str = pane.addStyle("str", null); 
                StyleConstants.setForeground(str, Theme.CYAN);
                
                Style num = pane.addStyle("num", null); 
                StyleConstants.setForeground(num, Theme.SUCCESS);
                
                Style com = pane.addStyle("com", null); 
                StyleConstants.setForeground(com, Theme.TEXT_MUTED); 
                StyleConstants.setItalic(com, true);

                // 3. Comprehensive Regex Pattern
                // Order: Multi-line comments | Single-line comments | Strings | Chars | Numbers | Words
                Pattern p = Pattern.compile("/\\*(?:[^*]|\\*[^/])*\\*/|//[^\n]*|\"[^\"]*\"|'[^']'|\\b\\d+\\.?\\d*\\b|\\b[a-zA-Z_$]\\w*\\b");
                Matcher m = p.matcher(text);
                
                while (m.find()) {
                    String match = m.group();
                    
                    // Determine which style to apply based on the matched text
                    if (match.startsWith("/") || match.startsWith("*")) {
                        doc.setCharacterAttributes(m.start(), match.length(), com, false);
                    } else if (match.startsWith("\"") || match.startsWith("'")) {
                        doc.setCharacterAttributes(m.start(), match.length(), str, false);
                    } else if (Character.isDigit(match.charAt(0))) {
                        doc.setCharacterAttributes(m.start(), match.length(), num, false);
                    } else if (KeywordToken.KEYWORDS.contains(match)) {
                        doc.setCharacterAttributes(m.start(), match.length(), kw, false);
                    }
                }
            } catch (Exception e) {
                // Silently handle exceptions that occur if text changes while highlighting
            }
        });
    }
}