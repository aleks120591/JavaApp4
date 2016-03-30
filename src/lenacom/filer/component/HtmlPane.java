package lenacom.filer.component;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public abstract class HtmlPane extends JEditorPane implements HyperlinkListener {
    public HtmlPane() {
        super("text/html", "");
        setEditable(false);
        addHyperlinkListener(this);
    }

    @Override
    public void hyperlinkUpdate(final HyperlinkEvent e) {
        HyperlinkEvent.EventType type = e.getEventType();
        if (HyperlinkEvent.EventType.ACTIVATED == type) {
            onHyperlinkClicked(e);
        } else if (HyperlinkEvent.EventType.ENTERED == type) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    String tooltip = getToolTip(e);
                    if (tooltip != null && tooltip.length() > 0) {
                        setToolTipText(tooltip);
                    }
                }
            });
        } else if (HyperlinkEvent.EventType.EXITED == type) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    setToolTipText(null);
                }
            });
        }
    }

    protected abstract void onHyperlinkClicked(HyperlinkEvent e);
    protected abstract String getToolTip(HyperlinkEvent e);
}
