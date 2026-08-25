package ke.co.skyworld.internship.util.infra.excel;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;



public class WorkbookBuilder implements AutoCloseable {

    private final Workbook workbook;
    private final StyleRegistry styles;
    private final boolean streaming;

    public WorkbookBuilder() {
        this(false, StyleRegistry.Theme.defaults());
    }

    public WorkbookBuilder(boolean streaming) {
        this(streaming, StyleRegistry.Theme.defaults());
    }

    public WorkbookBuilder(boolean streaming, StyleRegistry.Theme theme) {
        this.streaming = streaming;
        this.workbook = streaming ? new SXSSFWorkbook(500) : new XSSFWorkbook(); // 500-row in-memory window
        this.styles = new StyleRegistry(workbook, theme);
    }

    public Sheet sheet(String name) {
        return workbook.createSheet(sanitizeSheetName(name));
    }

    public StyleRegistry styles() {
        return styles;
    }

    public Workbook raw() {
        return workbook;
    }

    /**
     * Excel sheet names: max 31 chars, no \ / ? * [ ] :
     */
    private String sanitizeSheetName(String name) {
        String cleaned = name.replaceAll("[\\\\/?*\\[\\]:]", "-");
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }


    public byte[] build() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } finally {
            if (streaming) {
                ((SXSSFWorkbook) workbook).dispose(); // deletes temp files backing the streaming window
            }
        }
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}
