package lenacom.filer.config;

public class ResourceKey {
    private String key;
    private Object[] params;

    public ResourceKey(String key, Object... params) {
        this.key = key;
        this.params = params;
    }

    public String getKey() {
        return key;
    }

    public Object[] getParams() {
        return params;
    }
}
