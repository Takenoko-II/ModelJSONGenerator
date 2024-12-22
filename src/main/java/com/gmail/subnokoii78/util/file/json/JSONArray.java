package com.gmail.subnokoii78.util.file.json;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class JSONArray extends JSONValue<List<Object>> implements Iterable<Object>, JSONStructure {
    public JSONArray() {
        super(new ArrayList<>());
    }

    public JSONArray(@NotNull Collection<?> collection) {
        super(new ArrayList<>(collection));
    }

    public boolean has(int index) {
        return index < value.size();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public JSONValueType<?> getTypeAt(int index) {
        if (!has(index)) {
            throw new IllegalArgumentException("インデックス '" + index + "' は存在しません");
        }

        return JSONValueType.of(value.get(index));
    }

    public <T> T get(int index, @NotNull JSONValueType<T> type) {
        if (!has(index)) {
            throw new IllegalArgumentException("インデックス '" + index + "' は存在しません");
        }

        if (!getTypeAt(index).equals(type)) {
            throw new IllegalArgumentException("インデックス '" + index + "' は期待される型の値と紐づけられていません");
        }

        return type.get(value.get(index));
    }

    public void add(int index, Object value) {
        if (index < 0 || index > this.value.size()) {
            throw new IllegalArgumentException("そのインデックスは使用できません");
        }

        JSONValueType.throwIfInvalid(value);

        if (value instanceof JSONValue<?> jsonValue) {
            this.value.add(index, jsonValue);
        }
        else {
            this.value.add(index, value);
        }
    }

    public void add(Object value) {
        JSONValueType.throwIfInvalid(value);

        if (value instanceof JSONValue<?> jsonValue) {
            this.value.add(jsonValue);
        }
        else {
            this.value.add(value);
        }
    }

    public void set(int index, Object value) {
        if (index < 0 || index >= this.value.size()) {
            throw new IllegalArgumentException("そのインデックスは使用できません");
        }

        JSONValueType.throwIfInvalid(value);

        if (value instanceof JSONValue<?> jsonValue) {
            this.value.set(index, jsonValue.value);
        }
        else {
            this.value.set(index, value);
        }
    }

    public void delete(int index) {
        if (has(index)) value.remove(index);
    }

    public void clear() {
        value.clear();
    }

    public int length() {
        return value.size();
    }

    @Override
    public @NotNull Iterator<Object> iterator() {
        final List<Object> list = new ArrayList<>();

        for (int i = 0; i < this.value.size(); i++) {
            list.add(get(i, getTypeAt(i)));
        }

        return list.iterator();
    }

    public List<Object> asList() {
        final List<Object> list = new ArrayList<>();

        for (int i = 0; i < length(); i++) {
            final JSONValueType<?> type = getTypeAt(i);

            if (type.equals(JSONValueType.OBJECT)) {
                final JSONObject object = get(i, JSONValueType.OBJECT);
                list.add(object.asMap());
            }
            else if (type.equals(JSONValueType.ARRAY)) {
                final JSONArray array = get(i, JSONValueType.ARRAY);
                list.add(array.asList());
            }
            else {
                list.add(value.get(i));
            }
        }

        return list;
    }

    public @NotNull JSONArray copy() {
        return new JSONArray(asList());
    }

    public boolean isArrayOf(@NotNull JSONValueType<?> type) {
        for (int i = 0; i < length(); i++) {
            if (!getTypeAt(i).equals(type)) {
                return false;
            }
        }

        return true;
    }

    public <T> TypedJSONArray<T> typed(@NotNull JSONValueType<T> type) {
        final TypedJSONArray<T> array = new TypedJSONArray<>(type);

        for (int i = 0; i < length(); i++) {
            if (!getTypeAt(i).equals(type)) {
                throw new IllegalStateException("その型の値でない要素が見つかりました: " + getTypeAt(i).toString());
            }

            final T element = get(i, type);
            array.add(element);
        }

        return array;
    }
}
