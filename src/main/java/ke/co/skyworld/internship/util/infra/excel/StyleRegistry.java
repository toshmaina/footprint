package ke.co.skyworld.internship.util.infra.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class StyleRegistry {

    private final Workbook workbook;
    private final Theme theme;
    private final Map<Kind, CellStyle> cache = new EnumMap<>(Kind.class);
    private final Map<String, DataFormat> formatCache = new ConcurrentHashMap<>();

    public StyleRegistry(Workbook workbook) {
        this(workbook, Theme.defaults());
    }

    public StyleRegistry(Workbook workbook, Theme theme) {
        this.workbook = workbook;
        this.theme = theme;
    }

    public CellStyle get(Kind kind) {
        return cache.computeIfAbsent(kind, this::build);
    }

    private CellStyle build(Kind kind) {
        return switch (kind) {
            case TITLE -> style(s -> s.setFont(font(f -> {
                f.setBold(true);
                f.setFontHeightInPoints(theme.titleFontHeightInPoints());
                setFontColor(f, theme.titleFontColorHex());
            })));
            case HEADER -> style(s -> {
                s.setFont(font(f -> {
                    f.setBold(theme.headerBold());
                    setFontColor(f, theme.headerFontColorHex());
                }));
                fill(s, theme.headerFillColorHex());
                borderAll(s);
                s.setWrapText(true);
                s.setVerticalAlignment(VerticalAlignment.CENTER);
            });
            case DEFAULT -> style(this::borderAll);
            case DEFAULT_ALT -> style(s -> {
                borderAll(s);
                fill(s, theme.altRowFillColorHex());
            });
            case NUMBER -> style(s -> {
                borderAll(s);
                s.setDataFormat(format("#,##0"));
            });
            case NUMBER_ALT -> style(s -> {
                borderAll(s);
                s.setDataFormat(format("#,##0"));
                fill(s, theme.altRowFillColorHex());
            });
            case CURRENCY -> style(s -> {
                borderAll(s);
                s.setDataFormat(format("#,##0.00"));
            });
            case CURRENCY_ALT -> style(s -> {
                borderAll(s);
                s.setDataFormat(format("#,##0.00"));
                fill(s, theme.altRowFillColorHex());
            });
            case PERCENT -> style(s -> {
                borderAll(s);
                s.setDataFormat(format("0.0%"));
            });
            case DATE -> style(s -> {
                borderAll(s);
                s.setDataFormat(format("dd MMM yyyy"));
            });
            case DATE_ALT -> style(s -> {
                borderAll(s);
                s.setDataFormat(format("dd MMM yyyy"));
                fill(s, theme.altRowFillColorHex());
            });
            case DATETIME -> style(s -> {
                borderAll(s);
                s.setDataFormat(format("dd MMM yyyy HH:mm:ss"));
            });
            case DATETIME_ALT -> style(s -> {
                borderAll(s);
                s.setDataFormat(format("dd MMM yyyy HH:mm:ss"));
                fill(s, theme.altRowFillColorHex());
            });
            case TOTAL_LABEL -> style(s -> {
                s.setFont(font(f -> {
                    f.setBold(true);
                    setFontColor(f, theme.titleFontColorHex());
                }));
                s.setBorderTop(BorderStyle.THIN);
                s.setAlignment(HorizontalAlignment.RIGHT);
            });
            case TOTAL_VALUE -> style(s -> {
                s.setFont(font(f -> {
                    f.setBold(true);
                    setFontColor(f, theme.titleFontColorHex());
                }));
                s.setBorderTop(BorderStyle.THIN);
                s.setDataFormat(format("#,##0.00"));
            });
        };
    }

    private void fill(CellStyle s, String hex) {
        s.setFillForegroundColor(rgb(hex));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private void setFontColor(Font f, String hex) {
        if (f instanceof XSSFFont xssfFont) {
            xssfFont.setColor(rgb(hex));
        }
    }

    private XSSFColor rgb(String hex) {
        int value = Integer.parseInt(hex, 16);
        byte[] bytes = {(byte) ((value >> 16) & 0xFF), (byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF)};
        return new XSSFColor(bytes, null);
    }

    public Kind alternate(Kind base, boolean altRow) {
        if (!altRow) {
            return base;
        }
        return switch (base) {
            case DEFAULT -> Kind.DEFAULT_ALT;
            case NUMBER -> Kind.NUMBER_ALT;
            case CURRENCY -> Kind.CURRENCY_ALT;
            case DATE -> Kind.DATE_ALT;
            case DATETIME -> Kind.DATETIME_ALT;
            default -> base;
        };
    }

    private short format(String pattern) {
        DataFormat fmt = formatCache.computeIfAbsent(pattern, p -> workbook.createDataFormat());
        return fmt.getFormat(pattern);
    }

    private Font font(java.util.function.Consumer<Font> configure) {
        Font f = workbook.createFont();
        configure.accept(f);
        return f;
    }

    private void borderAll(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    private CellStyle style(java.util.function.Consumer<CellStyle> configure) {
        CellStyle s = workbook.createCellStyle();
        configure.accept(s);
        return s;
    }

    public enum Kind {
        TITLE,
        HEADER,
        DEFAULT,
        DEFAULT_ALT,   // zebra-striping row
        NUMBER,
        NUMBER_ALT,
        CURRENCY,
        CURRENCY_ALT,
        PERCENT,
        DATE,
        DATE_ALT,
        DATETIME,
        DATETIME_ALT,
        TOTAL_LABEL,
        TOTAL_VALUE
    }

    public record Theme(
            String headerFillColorHex,
            String headerFontColorHex,
            String altRowFillColorHex,
            String titleFontColorHex,
            short titleFontHeightInPoints,
            boolean headerBold
    ) {

        public static Theme defaults() {
            return new Theme(
                    "34495E",
                    "FFFFFF",
                    "F7F9FA",
                    "2C3E50",
                    (short) 14,
                    true
            );
        }
    }
}
