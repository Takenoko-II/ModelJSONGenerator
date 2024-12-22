package com.gmail.subnokoii78.util.file.json;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public final class JSONFile {
    private final Path path;

    public JSONFile(@NotNull String path) {
        /*if (!path.endsWith(".json")) {
            throw new IllegalArgumentException("ファイルの拡張子は.jsonである必要があります");
        }*/

        this.path = Path.of(path);
    }

    private @NotNull String read() {
        try {
            return String.join("", Files.readAllLines(path));
        }
        catch (IOException e) {
            throw new IllegalStateException("ファイルの読み取りに失敗しました", e);
        }
    }

    private void write(@NotNull String json) {
        try {
            Files.write(path, Arrays.stream(json.split("\\n")).toList(), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            throw new IllegalStateException("ファイルの書き込みに失敗しました", e);
        }
    }

    /**
     * オブジェクトを解析します。
     * @return 解析されたjsonオブジェクト
     */
    public @NotNull JSONObject readAsObject() {
        return JSONParser.parseObject(read());
    }

    /**
     * 配列を解析します。
     * @return 解析されたjson配列
     */
    public @NotNull JSONArray readAsArray() {
        return JSONParser.parseArray(read());
    }

    public void write(@NotNull JSONStructure structure) {
        write(JSONSerializer.serialize(structure));
    }
}
