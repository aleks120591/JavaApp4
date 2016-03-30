package lenacom.filer.path.processor;

import lenacom.filer.util.BasicAction;

import java.awt.event.ActionEvent;

class DoNothingAction extends BasicAction {

    public DoNothingAction(String key) {
        super(key);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //do nothing
    }
}
