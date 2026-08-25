package ke.co.skyworld.internship.util.infra.excel;

import java.util.function.Function;

public record ColumnDef<T>(
        String header,
        int widthChars,                       // -1 = don't set explicitly (auto-size handles it)
        Function<T, Object> valueExtractor,
        StyleRegistry.Kind styleKind
) {
    public static <T> ColumnDef<T> text(String header, Function<T, Object> extractor) {
        return new ColumnDef<>(header, -1, extractor, StyleRegistry.Kind.DEFAULT);
    }

    public static <T> ColumnDef<T> number(String header, Function<T, Object> extractor) {
        return new ColumnDef<>(header, -1, extractor, StyleRegistry.Kind.NUMBER);
    }

    public static <T> ColumnDef<T> currency(String header, Function<T, Object> extractor) {
        return new ColumnDef<>(header, -1, extractor, StyleRegistry.Kind.CURRENCY);
    }

    public static <T> ColumnDef<T> percent(String header, Function<T, Object> extractor) {
        return new ColumnDef<>(header, -1, extractor, StyleRegistry.Kind.PERCENT);
    }

    public static <T> ColumnDef<T> date(String header, Function<T, Object> extractor) {
        return new ColumnDef<>(header, -1, extractor, StyleRegistry.Kind.DATE);
    }

    public static <T> ColumnDef<T> dateTime(String header, Function<T, Object> extractor) {
        return new ColumnDef<>(header, -1, extractor, StyleRegistry.Kind.DATETIME);
    }

    public ColumnDef<T> withWidth(int chars) {
        return new ColumnDef<>(header, chars, valueExtractor, styleKind);
    }
}
