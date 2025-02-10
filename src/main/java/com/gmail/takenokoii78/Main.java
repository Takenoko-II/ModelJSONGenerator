package com.gmail.takenokoii78;

import com.gmail.subnokoii78.util.file.json.*;
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
        System.out.println("Hello World");

        OutputBuilder.text("ModelJSONGeneratorが正常に起動しました")
            .color(OutputBuilder.Color.GREEN)
            .out();

        if (!loadPackMeta()) {
            return;
        }

        while (true) {
            OutputBuilder.text("操作する名前空間を入力してください")
                .decoration(OutputBuilder.Decoration.FINE)
                .out();

            final String namespace = scanner.nextLine();

            final Path namespaceDir = Path.of("assets/" + namespace);

            if (!Files.exists(namespaceDir)) {
                OutputBuilder.text("名前空間が見つかりません")
                    .color(OutputBuilder.Color.RED)
                    .out();
                continue;
            }
            else if (!namespaceDir.toFile().isDirectory()) {
                OutputBuilder.text("名前空間が見つかりません")
                    .color(OutputBuilder.Color.RED)
                    .out();
                continue;
            }

            OutputBuilder.text("対象ディレクトリ: assets/" + namespace + "/...")
                .decoration(OutputBuilder.Decoration.FINE)
                .out();

            if (!confirm()) {
                continue;
            }

            OutputBuilder.text("<ItemModel>.jsonを生成するための画像ファイルの親ディレクトリまでのパスを入力してください:")
                .newLine()
                .append(OutputBuilder.text("    "))
                .append(
                    OutputBuilder.text("assets/" + namespace + "/textures/...")
                        .color(OutputBuilder.Color.YELLOW)
                )
                .decoration(OutputBuilder.Decoration.FINE)
                .color(OutputBuilder.Color.WHITE)
                .out();

            final String text = scanner.nextLine();

            if (text.isEmpty()) {
                break;
            }

            final Path inTexturesDir;

            try {
                inTexturesDir = Path.of(("assets/" + namespace + "/textures/" + text).trim());
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
                    break;
                }
            }

            final TypedJSONArray<JSONObject> cases = new TypedJSONArray<>(JSONValueType.OBJECT);

            final int count = createFiles(inTexturesDir, cases);

            if (count < 0) {
                break;
            }

            if (!createItemModelJSON(inTexturesDir, cases)) {
                break;
            }

            OutputBuilder.text("jsonの生成が全て完了しました: " + (count + 1) + "個のjsonファイルを作成または編集しました")
                .color(OutputBuilder.Color.GREEN)
                .decoration(OutputBuilder.Decoration.BOLD)
                .decoration(OutputBuilder.Decoration.ITALIC)
                .out();

            OutputBuilder.text("Enterを押して終了")
                .decoration(OutputBuilder.Decoration.FINE)
                .out();

            scanner.nextLine();
            // scanner.close();
            break;
        }
    }

    private static @NotNull Path replaceAt(@NotNull Path path, int index, @NotNull String replacement) {
        return Path.of(path.subpath(0, index) + "\\" + replacement + "\\" + path.subpath(index + 1, path.getNameCount()));
    }

    private static boolean createItemModelJSON(@NotNull Path inDir, @NotNull TypedJSONArray<JSONObject> cases) {
        final Path temp = replaceAt(inDir, 2, "items");
        final Path outPath = Path.of(temp.subpath(0, 3) + "\\" + temp.subpath(4, temp.getNameCount()) + ".json");
        final Path items = outPath.subpath(0, outPath.getNameCount() - 1);

        items.toFile().mkdirs();

        if (!outPath.subpath(0, outPath.getNameCount() - 1).toFile().exists()) {
            endToEnter("ディレクトリの作成に失敗しました: " + items);
            return false;
        }

        OutputBuilder.text("ディレクトリを作成しました: ")
            .decoration(OutputBuilder.Decoration.FINE)
            .out();
        OutputBuilder.text(items.toString())
            .color(OutputBuilder.Color.PINK)
            .out();

        if (!Files.exists(outPath)) {
            try {
                Files.createFile(outPath);
                OutputBuilder.text("ファイルを作成しました: ")
                    .decoration(OutputBuilder.Decoration.FINE)
                    .out();
                OutputBuilder.text(outPath.toString())
                    .color(OutputBuilder.Color.PINK)
                    .out();
            }
            catch (IOException e) {
                endToEnter(e.toString());
                return false;
            }
        }

        final JSONFile file = new JSONFile(outPath.toString());
        final JSONObject jsonObject = new JSONObject();
        final TypedJSONArray<String> notes = new JSONArray().typed(JSONValueType.STRING);

        notes.add("This file was generated by ModelJSONGenerator.");
        notes.add("If you want to call this item model, please use following custom model data:");
        notes.add("minecraft:custom_model_data={strings: [PATH]}");
        notes.add("For example, 'PATH' will be 'normal_combo1/frame1'.");
        notes.add("Please use only '0' as index.");
        notes.add("Bug report: https://github.com/Takenoko-II/ModelJSONGenerator");

        jsonObject.set("notes", notes);
        jsonObject.set("model.type", "minecraft:select");
        jsonObject.set("model.property", "minecraft:custom_model_data");
        jsonObject.set("model.index", 0);
        jsonObject.set("model.cases", cases);

        OutputBuilder.text(outPath.toString())
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

        file.write(jsonObject);

        return true;
    }

    private static void addCase(@NotNull TypedJSONArray<JSONObject> cases, @NotNull String path) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.set("when", path.replaceAll("^item/[a-zA-Z0-9_]+/", ""));
        jsonObject.set("model.type", "minecraft:model");
        jsonObject.set("model.model", path);
        cases.add(jsonObject);
    }

    private static int createFiles(@NotNull Path inDir, @NotNull TypedJSONArray<JSONObject> cases) {
        int i = 0;

        try (final var stream = Files.list(inDir)) {
            for (final Path innerInPath : stream.toList()) {
                final Path innerOutPath = Path.of(
                    replaceAt(innerInPath, 2, "models").toString()
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
                    i += createFiles(innerInPath, cases);
                }
                else if (innerInPath.toString().endsWith(".png")) {
                    i++;

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
                    final String texturePath = innerOutPath.toString()
                        .replaceAll("^assets\\\\[a-zA-Z0-9_]+\\\\models\\\\", "")
                        .replaceAll("\\.json$", "")
                        .replaceAll("\\\\", "/");

                    addCase(cases, texturePath);

                    texturesObj.set("layer0", texturePath);

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
            endToEnter(e.toString());
            return Integer.MIN_VALUE;
        }

        return i;
    }

    private static boolean loadPackMeta() {
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
            return false;
        }

        OutputBuilder.text("pack.mcmetaを読み込んでいます...")
            .decoration(OutputBuilder.Decoration.FINE)
            .out();
        final JSONObject packMetaObj = new JSONFile(packMeta.toString()).readAsObject();

        if (!packMetaObj.has("pack.pack_format")) {
            endToEnter("pack.mcmetaにpack.pack_formatが欠落しています");
            return false;
        }
        else if (!packMetaObj.has("pack.description")) {
            endToEnter("pack.mcmetaにpack.descriptionが欠落しています");
            return false;
        }

        final int packFormat = packMetaObj.get("pack.pack_format", JSONValueType.NUMBER).intValue();
        final String description = packMetaObj.get("pack.description", JSONValueType.STRING);

        OutputBuilder.text("pack_format: ")
            .append(
                OutputBuilder.text(String.valueOf(packFormat))
                    .color(OutputBuilder.Color.BLUE)
            )
            .newLine()
            .append(
                OutputBuilder.text("description: " + description)
                    .color(OutputBuilder.Color.BLUE)
            )
            .newLine()
            .append(
                OutputBuilder.text("pack.mcmetaのロードに成功しました")
                    .color(OutputBuilder.Color.GREEN)
            )
            .color(OutputBuilder.Color.BLUE)
            .out();

        return true;
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

        scanner.nextLine();
        // scanner.close();
    }

    private static boolean confirm() {
        OutputBuilder.text("正しければy, 間違っていればnを入力してください")
            .decoration(OutputBuilder.Decoration.FINE)
            .out();

        final String c = scanner.nextLine();

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
