package com.gmail.subnokoii78.util.file.json;

import java.util.*;

public abstract class JSONValueType<T> {
    protected JSONValueType() {}

    protected abstract T get(Object value);

    @Override
    public abstract String toString();

    public static JSONValueType<?> of(Object value) {
        return switch (value) {
            case JSONValue<?> jsonValue -> of(jsonValue.value);
            case Boolean ignored -> BOOLEAN;
            case Number ignored -> NUMBER;
            case String ignored -> STRING;
            case Map<?, ?> ignored -> OBJECT;
            case Collection<?> ignored -> ARRAY;
            case null -> NULL;
            default -> {
                if (value.getClass().isArray()) yield ARRAY;
                else throw new IllegalArgumentException("渡された値はjsonで使用できない型です: " + value.getClass().getName());
            }
        };
    }

    public static void throwIfInvalid(Object value) throws IllegalArgumentException {
        switch (value) {
            case JSONValue<?> ignored: break;
            case Boolean ignored: break;
            case Number ignored: break;
            case String ignored: break;
            case Map<?, ?> ignored: break;
            case Collection<?> ignored: break;
            case null: break;
            default: if (!value.getClass().isArray()) throw new IllegalArgumentException("その型の値はJSON構造オブジェクトに使用できません: " + value.getClass().getName());
        };
    }

    public static final JSONValueType<Boolean> BOOLEAN = new JSONValueType<>() {
        @Override
        protected Boolean get(Object value) {
            if (value instanceof Boolean v) return v;
            else throw new IllegalArgumentException("value is not a boolean value");
        }

        @Override
        public String toString() {
            return "Boolean";
        }
    };

    public static final JSONValueType<Number> NUMBER = new JSONValueType<>() {
        @Override
        protected Number get(Object value) {
            if (value instanceof Number v) return v;
            else throw new IllegalArgumentException("value is not a number value");
        }

        @Override
        public String toString() {
            return "Number";
        }
    };

    public static final JSONValueType<String> STRING = new JSONValueType<>() {
        @Override
        protected String get(Object value) {
            if (value instanceof String v) return v;
            else throw new IllegalArgumentException("value is not a string value");
        }

        @Override
        public String toString() {
            return "String";
        }
    };

    public static final JSONValueType<JSONObject> OBJECT = new JSONValueType<>() {
        @Override
        protected JSONObject get(Object value) {
            if (value instanceof JSONObject jsonObject) return jsonObject;

            if (value instanceof Map<?, ?> map) {
                final Map<String, Object> object = new HashMap<>();

                for (final Object key : map.keySet()) {
                    if (key instanceof String string) {
                        object.put(string, map.get(string));
                    }
                    else {
                        throw new IllegalArgumentException("A key of Map is not a string");
                    }
                }

                return new JSONObject(object);
            }
            else throw new IllegalArgumentException("value is not a json object value: " + value.getClass().getName());
        }

        @Override
        public String toString() {
            return "Object";
        }
    };

    public static final JSONValueType<JSONArray> ARRAY = new JSONValueType<>() {
        @Override
        protected JSONArray get(Object value) {
            if (value instanceof JSONArray jsonArray) return jsonArray;

            return switch (value) {
                case boolean[] array -> {
                    final List<Object> list = new ArrayList<>();
                    for (boolean element : array) list.add(element);
                    yield new JSONArray(list);
                }
                case byte[] array -> {
                    final List<Object> list = new ArrayList<>();
                    for (byte element : array) list.add(element);
                    yield new JSONArray(list);
                }
                case short[] array -> {
                    final List<Object> list = new ArrayList<>();
                    for (short element : array) list.add(element);
                    yield new JSONArray(list);
                }
                case int[] array -> {
                    final List<Object> list = new ArrayList<>();
                    for (int element : array) list.add(element);
                    yield new JSONArray(list);
                }
                case long[] array -> {
                    final List<Object> list = new ArrayList<>();
                    for (long element : array) list.add(element);
                    yield new JSONArray(list);
                }
                case float[] array -> {
                    final List<Object> list = new ArrayList<>();
                    for (float element : array) list.add(element);
                    yield new JSONArray(list);
                }
                case double[] array -> {
                    final List<Object> list = new ArrayList<>();
                    for (double element : array) list.add(element);
                    yield new JSONArray(list);
                }
                case char[] array -> {
                    final List<Object> list = new ArrayList<>();
                    for (char element : array) list.add(element);
                    yield new JSONArray(list);
                }
                case Object[] array -> new JSONArray(Arrays.asList(array));
                case Collection<?> collection -> new JSONArray(collection);
                default -> throw new IllegalArgumentException("value is not a json array value");
            };
        }

        @Override
        public String toString() {
            return "Array";
        }
    };

    public static final JSONValueType<JSONNull> NULL = new JSONValueType<>() {
        @Override
        protected JSONNull get(Object value) {
            if (value instanceof JSONNull jsonNull) return jsonNull;

            if (value == null) return JSONNull.NULL;
            else throw new IllegalArgumentException("value is not a null value");
        }

        @Override
        public String toString() {
            return "Null";
        }
    };
}
