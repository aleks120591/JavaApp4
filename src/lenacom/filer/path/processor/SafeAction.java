package lenacom.filer.path.processor;

import lenacom.filer.util.BasicAction;

import java.awt.event.ActionEvent;

abstract class SafeAction extends BasicAction {
    private SafeProcessor processor;

    public SafeAction(SafeProcessor processor, String key, Object... params) {
        super(key, params);
        this.processor = processor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            safeActionPerformed();
        } catch (Exception x) {
            processor.processError(x);
        }
    }

    protected abstract void safeActionPerformed() throws Exception;
}
