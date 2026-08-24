package com.skyworld.util.infra.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;



public class TableWriter<T> {

    private final Sheet sheet;
    private final StyleRegistry styles;
    private final List<ColumnDef<T>> columns;
    private final int startRow;
    private final int startCol;
    private final boolean zebraStripe;

    private TableWriter(Builder<T> b) {
        this.sheet = b.sheet;
        this.styles = b.styles;
        this.columns = b.columns;
        this.startRow = b.startRow;
        this.startCol = b.startCol;
        this.zebraStripe = b.zebraStripe;
    }

    public static <T> Builder<T> builder(Sheet sheet, StyleRegistry styles) {
        return new Builder<>(sheet, styles);
    }

    /**
     * Writes a report title directly above where a table will be rendered, using the shared
     * {@link StyleRegistry.Kind#TITLE} style. Independent of column count/width so it can be
     * called before {@code render} regardless of how many columns the table ends up with.
     *
     * @return the row index immediately after the title row.
     */
    public static int writeTitle(Sheet sheet, StyleRegistry styles, int row, int col, String text) {
        Row titleRow = sheet.getRow(row) != null ? sheet.getRow(row) : sheet.createRow(row);
        Cell cell = titleRow.createCell(col);
        CellWriter.write(cell, text);
        cell.setCellStyle(styles.get(StyleRegistry.Kind.TITLE));
        return row + 1;
    }

    /**
     * @return the row index immediately after the last row written.
     */
    public int render(List<T> data) {
        int rowIdx = writeHeader();
        rowIdx = writeRows(data, rowIdx);
        applyColumnWidths();
        return rowIdx;
    }

    /**
     * Writes a totals row directly below rendered data (typically at the row index returned by
     * {@link #render}): a right-aligned label in the first column and a bold, top-bordered value
     * per entry in {@code totalsByColumnIndex}, keyed by 0-based column offset from this writer's
     * startCol.
     *
     * @return the row index immediately after the totals row.
     */
    public int renderTotals(int atRow, String label, Map<Integer, BigDecimal> totalsByColumnIndex) {
        Row row = sheet.createRow(atRow);

        Cell labelCell = row.createCell(startCol);
        CellWriter.write(labelCell, label);
        labelCell.setCellStyle(styles.get(StyleRegistry.Kind.TOTAL_LABEL));

        for (Map.Entry<Integer, BigDecimal> entry : totalsByColumnIndex.entrySet()) {
            Cell cell = row.createCell(startCol + entry.getKey());
            CellWriter.write(cell, entry.getValue());
            cell.setCellStyle(styles.get(StyleRegistry.Kind.TOTAL_VALUE));
        }
        return atRow + 1;
    }

    private int writeHeader() {
        Row header = sheet.createRow(startRow);
        header.setHeightInPoints(20f);
        CellStyle headerStyle = styles.get(StyleRegistry.Kind.HEADER);

        for (int i = 0; i < columns.size(); i++) {
            Cell cell = header.createCell(startCol + i);
            cell.setCellValue(columns.get(i).header());
            cell.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, startRow + 1);
        return startRow + 1;
    }

    private int writeRows(List<T> data, int firstDataRow) {
        int rowIdx = firstDataRow;

        for (int r = 0; r < data.size(); r++) {
            T item = data.get(r);
            Row row = sheet.createRow(rowIdx);
            boolean altRow = zebraStripe && (r % 2 == 1);

            for (int c = 0; c < columns.size(); c++) {
                ColumnDef<T> col = columns.get(c);
                Cell cell = row.createCell(startCol + c);
                CellWriter.write(cell, col.valueExtractor().apply(item));
                cell.setCellStyle(styles.get(styles.alternate(col.styleKind(), altRow)));
            }
            rowIdx++;
        }
        return rowIdx;
    }

    private void applyColumnWidths() {
        for (int i = 0; i < columns.size(); i++) {
            ColumnDef<T> col = columns.get(i);
            if (col.widthChars() > 0) {
                sheet.setColumnWidth(startCol + i, col.widthChars() * 256);
            } else {
                sheet.autoSizeColumn(startCol + i);
            }
        }
    }

    public static final class Builder<T> {
        private final Sheet sheet;
        private final StyleRegistry styles;
        private List<ColumnDef<T>> columns;
        private int startRow = 0;
        private int startCol = 0;
        private boolean zebraStripe = true;

        private Builder(Sheet sheet, StyleRegistry styles) {
            this.sheet = sheet;
            this.styles = styles;
        }

        public Builder<T> columns(List<ColumnDef<T>> columns) {
            this.columns = columns;
            return this;
        }

        public Builder<T> startAt(int row, int col) {
            this.startRow = row;
            this.startCol = col;
            return this;
        }

        public Builder<T> zebraStripe(boolean enabled) {
            this.zebraStripe = enabled;
            return this;
        }

        public TableWriter<T> build() {
            if (columns == null || columns.isEmpty()) {
                throw new IllegalStateException("columns must be set before build()");
            }
            return new TableWriter<>(this);
        }
    }
}
