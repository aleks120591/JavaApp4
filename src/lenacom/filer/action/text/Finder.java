package lenacom.filer.action.text;

class Finder {
    private String haystack;
    private boolean reverse;
    private boolean cyclical;
    private int start;
    private String[] needles;
    private int needleLength;

    //return -1 if nothing is found
    static int find(String haystack, String needle, FindParameters params) {
        if (needle.length() > haystack.length()) return -1;
        return new Finder(haystack, needle, params).findNeedle();
    }

    private Finder(String haystack, String needle, FindParameters params) {
        this.haystack = haystack;
        this.needleLength = needle.length();
        this.start = params.getStart();
        this.reverse = params.isReverse();
        this.cyclical = params.isCyclical();
        if (params.isCaseSensitive()) {
            needles = new String[]{ needle };
        } else {
            needles = new String[]{
                needle.toLowerCase(), // lowercase first, it is more probable
                needle.toUpperCase()
            };
        }
    }

    private int findNeedle() {
        int max = haystack.length() - needleLength;
        if (reverse) {
            start = start - needleLength - 1;
            if (start < 0) start = max;
        } else {
            start++;
            if (start > max) start = 0;
        }
        int pos = start;
        int step = reverse? -1 : 1;

        outer:
        do {
            pos += step;
            if (pos > max || pos < 0) {
                if (cyclical) {
                    pos = pos < 0 ? max : 0;
                } else {
                    return -1; //not found
                }
            }
            if (pos == start) return -1; //not found

            for (int i = 0; i < needleLength; i++) {
                char ch = haystack.charAt(pos + i);
                if (!matches(ch, i)) continue outer; //not found
            }

            return pos; //found
        } while (true);
    }

    private boolean matches(char ch, int needleIndex) {
        for (String checkNeedle: needles) {
            if (ch == checkNeedle.charAt(needleIndex)) return true;
        }
        return false;
    }
}
