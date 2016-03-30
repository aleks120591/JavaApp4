package lenacom.filer.progress;

import lenacom.filer.config.ResourceKey;

import javax.swing.*;

public abstract class ExtendedPathProgress extends BasicPathProgress {

    public ExtendedPathProgress(JFrame owner, ResourceKey keyTitle, ResourceKey keyDescription, long totalValue) {
        super(owner, keyTitle, keyDescription, totalValue, true);
    }
}
