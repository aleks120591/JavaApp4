package lenacom.filer.action.find;

import java.util.List;

class Result {
    private FindParameters params;
    private List<FoundPath> paths;

    Result(FindParameters params, List<FoundPath> paths) {
        this.params = params;
        this.paths = paths;
    }

    FindParameters getFindParameters() {
        return params;
    }

    List<FoundPath> getPaths() {
        return paths;
    }
}
