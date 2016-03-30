package lenacom.filer.action.text;

class FindParameters {
    private int start = 0;
    private boolean cyclical = true;
    private boolean reverse = false;
    private boolean caseSensitive = false;

    int getStart() {
        return start;
    }

    void setStart(int start) {
        this.start = start;
    }

    boolean isCyclical() {
        return cyclical;
    }

    void setCyclical(boolean cyclical) {
        this.cyclical = cyclical;
    }

    boolean isReverse() {
        return reverse;
    }

    void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    boolean isCaseSensitive() {
        return caseSensitive;
    }

    void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
}
