package com.gmail.subnokoii78.util.file.json;

/**
 * nullを表現します。
 */
public final class JSONNull extends JSONValue<Object> {
    private JSONNull() {
        super(null);
    }

    public static final JSONNull NULL = new JSONNull();
}
