package com.skyworld.util.infra.excel;

import org.apache.poi.ss.usermodel.Cell;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.Date;



public final class CellWriter {

    private CellWriter() {
    }

    public static void write(Cell cell, Object value) {
        switch (value) {
            case null -> cell.setBlank();
            case String s -> cell.setCellValue(s);
            case Boolean b -> cell.setCellValue(b);
            case Number n -> cell.setCellValue(n.doubleValue());
            case LocalDate ld -> cell.setCellValue(ld);
            case LocalDateTime ldt -> cell.setCellValue(ldt);
            case OffsetDateTime odt -> cell.setCellValue(odt.toLocalDateTime());
            case Date d -> cell.setCellValue(d);
            case Temporal t -> cell.setCellValue(t.toString());
            default -> cell.setCellValue(value.toString());
        }
    }
}
