package com.gmail.takenokoii78;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class OutputBuilder {
    private String text;

    private Color textColor;

    private Color backgroundColor;

    private final Set<Decoration> decorations = new HashSet<>();

    private OutputBuilder(@NotNull String text) {
        this.text = text;
    }

    public @NotNull OutputBuilder color(@NotNull Color color) {
        this.textColor = color;
        return this;
    }

    public @NotNull OutputBuilder backgroundColor(@NotNull Color color) {
        this.backgroundColor = color;
        return this;
    }

    public @NotNull OutputBuilder decoration(@NotNull Decoration decoration) {
        this.decorations.add(decoration);
        return this;
    }

    public @NotNull OutputBuilder append(@NotNull OutputBuilder builder) {
        text += END;

        final var copy = builder.copy();

        if (copy.textColor == null) {
            copy.textColor = textColor;
        }

        if (copy.backgroundColor == null) {
            copy.backgroundColor = backgroundColor;
        }

        if (copy.decorations.isEmpty()) {
            copy.decorations.addAll(decorations);
        }

        text += copy.build();

        return this;
    }

    public @NotNull OutputBuilder space() {
        return append(text(" "));
    }

    public @NotNull OutputBuilder newLine() {
        return append(text("\n"));
    }

    private @NotNull OutputBuilder copy() {
        final OutputBuilder builder = new OutputBuilder(text);
        builder.decorations.addAll(decorations);
        builder.textColor = textColor;
        builder.backgroundColor = backgroundColor;
        return builder;
    }

    private @NotNull String build() {
        final StringBuilder stringBuilder = new StringBuilder();

        if (textColor != null) {
            stringBuilder.append(textColor.getAsTextAscii());
        }

        if (backgroundColor != null) {
            stringBuilder.append(backgroundColor.getAsBackGroundAscii());
        }

        decorations.forEach(decoration -> stringBuilder.append(decoration.ansi));

        return stringBuilder
            .append(text)
            .append(END)
            .toString();
    }

    public void out(@NotNull PrintStream stream) {
        stream.println(build());
    }

    public void out() {
        out(System.out);
    }

    public static @NotNull OutputBuilder text(@NotNull String text) {
        return new OutputBuilder(text);
    }

    public static final class Color {
        private final String ansi;

        private Color(@NotNull String ansi) {
            this.ansi = ansi;
        }

        private @NotNull String getAsTextAscii() {
            return ansi.replace("x", "3");
        }

        private @NotNull String getAsBackGroundAscii() {
            return ansi.replace("x", "4");
        }

        public static final Color BLACK = new Color("\u001b[00;x0m");

        public static final Color RED = new Color("\u001b[00;x1m");

        public static final Color GREEN = new Color("\u001b[00;x2m");

        public static final Color YELLOW = new Color("\u001b[00;x3m");

        public static final Color PURPLE = new Color("\u001b[00;x4m");

        public static final Color PINK = new Color("\u001b[00;x5m");

        public static final Color BLUE = new Color("\u001b[00;x6m");

        public static final Color WHITE = new Color("\u001b[00;x7m");

        private static final String INT = "\u001b[x8;5;nm";

        private static final String RGB = "\u001b[x8;2;r;g;bm";

        public static @NotNull Color ofInt(int color) {
            return new Color(INT.replace("n", String.valueOf(color)));
        }

        public static @NotNull Color ofRGB(int red, int green, int blue) {
            return new Color(RGB
                .replace("r", String.valueOf(red))
                .replace("g", String.valueOf(green))
                .replace("b", String.valueOf(blue))
            );
        }
    }

    public enum Decoration {
        BOLD("\u001b[1m"),

        FINE("\u001b[2m"),

        ITALIC("\u001b[3m"),

        UNDERLINED("\u001b[4m"),

        BRINK("\u001b[5m"),

        FAST_BRINK("\u001b[6m"),

        HIDDEN("\u001b[8m");

        private final String ansi;

        Decoration(@NotNull String ansi) {
            this.ansi = ansi;
        }
    }

    private static final String END = "\u001b[00m";
}
