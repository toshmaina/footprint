package ke.co.skyworld.internship.util.infra.excel;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class ChartWriter {

    private ChartWriter() {
    }

    /**
     * Convenience spec builder for a chart summarising a table just rendered by
     * {@link TableWriter}. {@code categoryColIndex} and each key in {@code valueColumns} are
     * 0-based column offsets from the table's startCol; {@code valueColumns} maps that offset
     * to the series name to show in the legend.
     *
     * @param tableStartRow the row index the table's header was written at
     * @param tableEndRow   the row index returned by {@link TableWriter#render}, i.e. one past
     *                      the last data row
     */
    public static Spec fromTable(int tableStartRow, int tableStartCol, int tableEndRow,
                                 int categoryColIndex, Map<Integer, String> valueColumns,
                                 Type type, String title,
                                 int anchorCol1, int anchorRow1, int anchorCol2, int anchorRow2) {
        int firstDataRow = tableStartRow + 1;
        int lastDataRow = tableEndRow - 1;
        if (lastDataRow < firstDataRow) {
            throw new IllegalArgumentException("Table has no data rows to chart (start=" + tableStartRow
                    + ", end=" + tableEndRow + ")");
        }

        CellRangeAddress categoryRange = new CellRangeAddress(firstDataRow, lastDataRow,
                tableStartCol + categoryColIndex, tableStartCol + categoryColIndex);

        List<Series> series = valueColumns.entrySet().stream()
                .map(e -> new Series(e.getValue(), new CellRangeAddress(firstDataRow, lastDataRow,
                        tableStartCol + e.getKey(), tableStartCol + e.getKey())))
                .toList();

        return new Spec(type, title, categoryRange, series, anchorCol1, anchorRow1, anchorCol2, anchorRow2);
    }

    /**
     * Renders the chart described by {@code spec} onto {@code sheet} and returns it.
     */
    public static XSSFChart render(XSSFSheet sheet, Spec spec) {
        if (spec.series() == null || spec.series().isEmpty()) {
            throw new IllegalArgumentException("A chart needs at least one series");
        }
        if (spec.type() == Type.PIE && spec.series().size() != 1) {
            throw new IllegalArgumentException("Pie charts take exactly one value series, got "
                    + spec.series().size());
        }

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0,
                spec.anchorCol1(), spec.anchorRow1(), spec.anchorCol2(), spec.anchorRow2());
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(spec.title());
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryDataSource categories =
                XDDFDataSourcesFactory.fromStringCellRange(sheet, spec.categoryRange());

        XDDFChartData data = switch (spec.type()) {
            case PIE -> chart.createData(ChartTypes.PIE, null, null);
            case LINE -> chart.createData(ChartTypes.LINE, categoryAxis(chart), valueAxis(chart));
            case BAR, COLUMN -> chart.createData(ChartTypes.BAR, categoryAxis(chart), valueAxis(chart));
        };

        for (Series s : spec.series()) {
            XDDFNumericalDataSource<Double> values =
                    XDDFDataSourcesFactory.fromNumericCellRange(sheet, s.valueRange());
            XDDFChartData.Series series = data.addSeries(categories, values);
            series.setTitle(s.name(), null);
            if (series instanceof XDDFLineChartData.Series lineSeries) {
                lineSeries.setSmooth(false);
                lineSeries.setMarkerStyle(MarkerStyle.CIRCLE);
            }
        }

        if (data instanceof XDDFBarChartData barData) {
            barData.setBarDirection(spec.type() == Type.BAR ? BarDirection.BAR : BarDirection.COL);
        }

        chart.plot(data);
        return chart;
    }

    /**
     * Builds a chart data map ({@code columnOffset -> seriesName}) preserving insertion order.
     */
    public static Map<Integer, String> series(int columnOffset, String name) {
        Map<Integer, String> map = new LinkedHashMap<>();
        map.put(columnOffset, name);
        return map;
    }

    private static XDDFCategoryAxis categoryAxis(XSSFChart chart) {
        return chart.createCategoryAxis(AxisPosition.BOTTOM);
    }

    private static XDDFValueAxis valueAxis(XSSFChart chart) {
        XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
        valueAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        return valueAxis;
    }

    public enum Type {BAR, COLUMN, LINE, PIE}

    public record Series(String name, CellRangeAddress valueRange) {
    }

    public record Spec(
            Type type,
            String title,
            CellRangeAddress categoryRange,
            List<Series> series,
            int anchorCol1, int anchorRow1, int anchorCol2, int anchorRow2
    ) {
    }
}

