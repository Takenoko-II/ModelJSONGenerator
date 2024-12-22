package com.gmail.takenokoii78;

import com.gmail.subnokoii78.util.file.json.JSONFile;
import com.gmail.subnokoii78.util.file.json.JSONObject;
import com.gmail.subnokoii78.util.file.json.JSONSerializer;
import com.gmail.subnokoii78.util.file.json.JSONValueType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadPackMeta();

        while (true) {
            OutputBuilder.text("<ItemModel>.jsonを生成するための画像ファイルの親ディレクトリまでのパスを入力してください:")
                .newLine()
                .append(OutputBuilder.text("    "))
                .append(
                    OutputBuilder.text("assets/minecraft/textures/...")
                        .color(OutputBuilder.Color.YELLOW)
                )
                .decoration(OutputBuilder.Decoration.FINE)
                .color(OutputBuilder.Color.WHITE)
                .out();

            final String text = scanner.next();

            if (text.isEmpty()) {
                break;
            }

            final Path inTexturesDir;

            try {
                inTexturesDir = Path.of(("assets/minecraft/textures/" + text).trim());
            }
            catch (InvalidPathException e) {
                OutputBuilder.text("入力されたパスの形式が間違っています")
                    .color(OutputBuilder.Color.RED)
                    .out();
                continue;
            }

            if (!inTexturesDir.toFile().exists()) {
                OutputBuilder.text("入力されたパスは存在しません")
                    .color(OutputBuilder.Color.RED)
                    .out();
                continue;
            }

            OutputBuilder.text("入力されたパス: ")
                .append(
                    OutputBuilder.text(inTexturesDir.toAbsolutePath().toString())
                        .color(OutputBuilder.Color.YELLOW)
                )
                .decoration(OutputBuilder.Decoration.FINE)
                .out();

            if (!confirm()) {
                continue;
            }

            final Path outRootDir = Path.of(inTexturesDir.toString().replaceFirst("textures", "models"));

            if (!Files.exists(outRootDir)) {
                try {
                    Files.createDirectory(outRootDir);
                    OutputBuilder.text("出力先ルートディレクトリの作成を行いました").decoration(OutputBuilder.Decoration.FINE).out();
                }
                catch (IOException e) {
                    endToEnter("出力先ルートディレクトリを作成できませんでした");
                }
            }

            createJSONFiles(inTexturesDir);

            OutputBuilder.text("出力: ")
                .color(OutputBuilder.Color.WHITE)
                .append(
                    OutputBuilder.text("END")
                        .color(OutputBuilder.Color.GREEN)
                        .decoration(OutputBuilder.Decoration.BOLD)
                        .decoration(OutputBuilder.Decoration.ITALIC)
                )
                .out();

            final String text2 = scanner.nextLine();

            if (text2.isEmpty()) {
                scanner.close();
                break;
            }
        }
    }

    private static void createJSONFiles(@NotNull Path inDir) {
        try (final var stream = Files.list(inDir)) {
            for (final Path innerInPath : stream.toList()) {
                final Path innerOutPath = Path.of(
                    innerInPath.toString()
                        .replaceFirst("textures", "models")
                        .replaceAll("\\\\([a-zA-Z0-9_]+)\\.png$", "\\\\$1.json")
                );

                if (innerInPath.toFile().isDirectory()) {
                    if (!innerOutPath.toFile().exists()) {
                        OutputBuilder.text("ディレクトリを作成しました: ")
                            .decoration(OutputBuilder.Decoration.FINE)
                            .out();
                        OutputBuilder.text(innerOutPath.toString())
                            .color(OutputBuilder.Color.PINK)
                            .out();
                        Files.createDirectory(innerOutPath);
                    }
                    createJSONFiles(innerInPath);
                }
                else if (innerInPath.toString().endsWith(".png")) {
                    if (!innerOutPath.toFile().exists()) {
                        OutputBuilder.text("ファイルを作成しました: ")
                            .decoration(OutputBuilder.Decoration.FINE)
                            .out();
                        OutputBuilder.text(innerOutPath.toString())
                            .color(OutputBuilder.Color.PINK)
                            .out();
                        Files.createFile(innerOutPath);
                    }
                    final JSONFile jsonFile = new JSONFile(innerOutPath.toString());

                    final JSONObject texturesObj = new JSONObject();
                    texturesObj.set(
                        "layer0",
                        innerOutPath.toString()
                            .replaceAll("^assets\\\\minecraft\\\\models\\\\", "")
                            .replaceAll("\\.json$", "")
                            .replaceAll("\\\\", "/")
                    );

                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.set("parent", "item/generated");
                    jsonObject.set("textures", texturesObj);

                    jsonFile.write(jsonObject);

                    OutputBuilder.text(innerOutPath.toString())
                        .color(OutputBuilder.Color.PINK)
                        .newLine()
                        .append(
                            OutputBuilder.text("> ファイルに以下を書き込みます:")
                                .decoration(OutputBuilder.Decoration.FINE)
                                .color(OutputBuilder.Color.WHITE)
                        )
                        .newLine()
                        .append(
                            OutputBuilder.text(JSONSerializer.serialize(jsonObject))
                                .color(OutputBuilder.Color.GREEN)
                                .decoration(OutputBuilder.Decoration.ITALIC)
                        )
                        .out();
                }
                else {
                    OutputBuilder.text(innerInPath.toString())
                        .color(OutputBuilder.Color.PINK)
                        .newLine()
                        .append(
                            OutputBuilder.text("> .pngではないためスキップします")
                                .decoration(OutputBuilder.Decoration.FINE)
                                .color(OutputBuilder.Color.WHITE)
                        )
                        .out();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadPackMeta() {
        final Path root = Path.of(FileSystems.getDefault().getPath("").toAbsolutePath().toString());

        OutputBuilder.text("リソースパックのルートディレクトリを以下として読み込みます: ")
            .newLine()
            .append(OutputBuilder.text("    "))
            .append(
                OutputBuilder.text(root.toAbsolutePath().toString())
                    .color(OutputBuilder.Color.YELLOW)
            )
            .decoration(OutputBuilder.Decoration.FINE)
            .out();

        final Path packMeta = Path.of("pack.mcmeta");

        if (!Files.exists(packMeta)) {
            endToEnter("pack.mcmetaが見つかりませんでした");
        }

        OutputBuilder.text("pack.mcmetaを読み込んでいます...")
            .decoration(OutputBuilder.Decoration.FINE)
            .out();
        final JSONObject packMetaObj = new JSONFile(packMeta.toString()).readAsObject();

        if (!packMetaObj.has("pack.pack_format")) {
            endToEnter("pack.mcmetaにpack.pack_formatが欠落しています");
        }
        else if (!packMetaObj.has("pack.description")) {
            endToEnter("pack.mcmetaにpack.descriptionが欠落しています");
        }

        final int packFormat = packMetaObj.get("pack.pack_format", JSONValueType.NUMBER).intValue();
        final String description = packMetaObj.get("pack.description", JSONValueType.STRING);

        OutputBuilder.text("pack_format: ")
            .append(OutputBuilder.text(String.valueOf(packFormat)))
            .newLine()
            .append(OutputBuilder.text("description: "))
            .newLine()
            .append(OutputBuilder.text(description))
            .newLine()
            .append(OutputBuilder.text("pack.mcmetaのロードに成功しました"))
            .color(OutputBuilder.Color.BLUE)
            .decoration(OutputBuilder.Decoration.FINE);
    }

    private static void endToEnter(@NotNull String errorMessage) {
        OutputBuilder.text(errorMessage)
            .color(OutputBuilder.Color.RED)
            .append(
                OutputBuilder.text("Enterを押して終了")
                    .color(OutputBuilder.Color.WHITE)
                    .decoration(OutputBuilder.Decoration.FINE)
            )
            .out();

        scanner.next();

        throw new RuntimeException("Program End");
    }

    private static boolean confirm() {
        OutputBuilder.text("正しければy, 間違っていればnを入力してください")
            .decoration(OutputBuilder.Decoration.FINE)
            .out();

        final String c = scanner.next();

        if (c.equals("n")) {
            return false;
        }
        else if (c.equals("y")) {
            return true;
        }
        else {
            OutputBuilder.text("無効な文字です")
                .color(OutputBuilder.Color.RED)
                .out();
            return confirm();
        }
    }
}
