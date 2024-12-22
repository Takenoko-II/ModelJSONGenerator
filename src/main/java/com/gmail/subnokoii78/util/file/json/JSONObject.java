package com.gmail.subnokoii78.util.file.json;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class JSONObject extends JSONValue<Map<String, Object>> implements JSONStructure {
    private final JSONPathAccessor accessor = new JSONPathAccessor(this);

    public JSONObject() {
        super(new HashMap<>());
    }

    public JSONObject(@NotNull Map<String, Object> map) {
        super(map);
    }

    public boolean hasKey(@NotNull String key) {
        return value.containsKey(key);
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public @NotNull JSONValueType<?> getTypeOfKey(@NotNull String key) {
        if (!hasKey(key)) {
            throw new IllegalArgumentException("キー '" + key + "' は存在しません");
        }

        return JSONValueType.of(value.get(key));
    }

    public @NotNull <T> T getKey(@NotNull String key, JSONValueType<T> type) {
        if (!hasKey(key)) {
            throw new IllegalArgumentException("キー '" + key + "' は存在しません");
        }

        if (!getTypeOfKey(key).equals(type)) {
            throw new IllegalArgumentException("キー '" + key + "' は期待される型の値と紐づけられていません");
        }

        return type.get(value.get(key));
    }

    public void setKey(@NotNull String key, Object value) {
        JSONValueType.throwIfInvalid(value);

        if (value instanceof JSONValue<?> jsonValue) {
            this.value.put(key, jsonValue.value);
        }
        else {
            this.value.put(key, value);
        }
    }

    public void deleteKey(@NotNull String key) {
        if (hasKey(key)) value.remove(key);
    }

    public void clear() {
        value.clear();
    }

    public Set<String> keys() {
        return value.keySet();
    }

    public void merge(@NotNull JSONObject jsonObject) {
        for (String key : jsonObject.keys()) {
            setKey(key, jsonObject.value.get(key));
        }
    }

    public Map<String, Object> asMap() {
        final Map<String, Object> map = new HashMap<>();

        for (String key : keys()) {
            final JSONValueType<?> type = getTypeOfKey(key);

            if (type.equals(JSONValueType.OBJECT)) {
                final JSONObject object = getKey(key, JSONValueType.OBJECT);
                map.put(key, object.asMap());
            }
            else if (type.equals(JSONValueType.ARRAY)) {
                final JSONArray array = getKey(key, JSONValueType.ARRAY);
                map.put(key, array.asList());
            }
            else {
                map.put(key, value.get(key));
            }
        }

        return map;
    }

    public @NotNull JSONObject copy() {
        return new JSONObject(asMap());
    }

    public boolean has(@NotNull String path) {
        return accessor.has(path);
    }

    public @NotNull JSONValueType<?> getTypeOf(@NotNull String path) {
        return accessor.getTypeOf(path);
    }

    public <T> @NotNull T get(@NotNull String path, @NotNull JSONValueType<T> type) {
        return accessor.get(path, type);
    }

    public void set(@NotNull String path, Object value) {
        accessor.set(path, value);
    }

    public void delete(@NotNull String path) {
        accessor.delete(path);
    }
}
