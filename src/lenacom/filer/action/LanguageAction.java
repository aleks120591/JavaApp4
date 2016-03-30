package lenacom.filer.action;

import lenacom.filer.component.OkCancelDialog;
import lenacom.filer.component.UIConstants;
import lenacom.filer.component.VerticalPanel;
import lenacom.filer.config.Language;
import lenacom.filer.config.ResourceKey;
import lenacom.filer.config.Resources;
import lenacom.filer.message.Confirmation;
import lenacom.filer.message.Messages;
import lenacom.filer.panel.XColumn;
import lenacom.filer.panel.XPanels;
import lenacom.filer.root.FileOperationWorkers;
import lenacom.filer.root.RootFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

class LanguageAction extends XAction {

    LanguageAction(String key, boolean withEllipsis) {
        super(key, withEllipsis);
    }

    @Override
    public void act() {
        new LanguageDialog();
    }

    private class LanguageDialog extends OkCancelDialog implements ActionListener {
        private Language currentLanguage;
        private Language selectedLanguage = Language.ENGLISH;
        private java.util.List<LanguageButton> buttons;

        @Override
        public void actionPerformed(ActionEvent e) {
            LanguageButton btn = ((LanguageButton) e.getSource());
            if (btn.isSelected()) {
                selectedLanguage = btn.getLanguage();
                setOkEnabled(!btn.getLanguage().equals(currentLanguage));
            }
        }

        private class LanguageButton extends JRadioButton {
            private Language language;

            private LanguageButton(Language language) {
                this.language = language;
                this.setText(language.getName());
                this.addActionListener(LanguageDialog.this);
            }

            private Language getLanguage() {
                return language;
            }
        }

        public LanguageDialog() {
            super(RootFrame.getRoot(), "dlg.lang.title");
            JPanel pnl = new JPanel(new GridLayout(0, 1, 0, UIConstants.VERTICAL_GAP));
            currentLanguage = Language.getLanguage();
            buttons = new ArrayList<>();
            ButtonGroup bg = new ButtonGroup();
            for (Language language: Language.values()) {
                LanguageButton btn = new LanguageButton(language);
                btn.setSelected(currentLanguage.equals(language));
                buttons.add(btn);
                bg.add(btn);
                pnl.add(btn);
            }
            this.setCenterComponent(pnl);
            setOkEnabled(false);
            setVisibleRelativeToParent();
        }


        @Override
        protected boolean onOk() {
            int count = FileOperationWorkers.count();
            if (count > 0) {
                Messages.showMessage("msg.file.operations.running", count);
                return false;
            }
            if (Confirmation.confirm(new ResourceKey("dlg.lang.confirm.restart"))) {
                Language.setLanguage(selectedLanguage);
                Resources.reload();
                Actions.reload();
                XColumn.reload();
                XPanels.reload();
                RootFrame.restart();
                return true;
            } else {
                return false;
            }
        }
    }
}
