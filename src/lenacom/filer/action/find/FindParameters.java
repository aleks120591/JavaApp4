package lenacom.filer.action.find;

import lenacom.filer.config.Resources;

class FindParameters {
    private String name;
    private String contains;
    private boolean caseSensitive = false;
    private boolean allExtracts = false;

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name.isEmpty()? null : name;
    }

    String getContains() {
        return contains;
    }

    void setContains(String contains) {
        this.contains = contains.isEmpty()? null : contains;
    }

    boolean isCaseSensitive() {
        return caseSensitive;
    }

    void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    boolean isAllExtracts() {
        return allExtracts;
    }

    void setAllExtracts(boolean allExtracts) {
        this.allExtracts = allExtracts;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (name != null) {
            sb.append(Resources.getMessage("dlg.find.lbl.name")).append(": ").append(name);
        }
        if (contains != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(Resources.getMessage("dlg.find.lbl.contains")).append(": ").append(contains);
        }
        if (caseSensitive) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(Resources.getMessage("dlg.find.lbl.match.case"));
        }
        if (allExtracts) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(Resources.getMessage("dlg.find.lbl.all.extracts"));
        }
        return sb.toString();
    }
}
