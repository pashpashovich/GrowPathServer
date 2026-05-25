package by.bsuir.growpathserver.trainee.application.service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.ArtifactLink;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.AssessmentRow;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.CompetencyRow;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.CompletedTaskRow;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InternshipResultReportPdfRenderer {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final String REPORT_TITLE = "Цифровой профиль достижений стажера";

    public byte[] render(InternshipResultReportData data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 48, 48, 72, 56);
            PdfWriter writer = PdfWriter.getInstance(document, output);
            FontSet fonts = loadFonts();
            writer.setPageEvent(new ReportPageEvent(data, fonts));
            document.open();
            writeBody(document, data, fonts);
            document.close();
            return output.toByteArray();
        }
        catch (IOException | DocumentException ex) {
            log.error("Failed to render internship result report PDF", ex);
            throw new IllegalStateException("Failed to generate report PDF", ex);
        }
    }

    private void writeBody(Document document, InternshipResultReportData data, FontSet fonts) throws DocumentException {
        addSectionTitle(document, "Сводка", fonts.section());
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(100);
        summary.setSpacingAfter(12f);
        addSummaryRow(summary, "Рейтинг (последний ассессмент)", formatRating(data.rating().overallRating()), fonts);
        addSummaryRow(summary, "Выполнение ИПР", formatPercent(data.progress().overallProgressPercent()), fonts);
        addSummaryRow(summary, "Средняя оценка по задачам", formatRating(data.progress().averageTaskRating()), fonts);
        addSummaryRow(summary, "Задачи выполнены",
                      data.progress().completedTasks() + " / " + data.progress().totalTasks(), fonts);
        addSummaryRow(summary, "Ментор", nullToDash(data.rating().mentorName()), fonts);
        document.add(summary);

        addSectionTitle(document, "Матрица компетенций (достигнутый уровень)", fonts.section());
        PdfPTable competencyTable = new PdfPTable(new float[] { 3f, 1.2f });
        competencyTable.setWidthPercentage(100);
        competencyTable.setSpacingAfter(12f);
        addHeaderCell(competencyTable, "Компетенция", fonts.tableHeader());
        addHeaderCell(competencyTable, "Достигнутый (из 5)", fonts.tableHeader());
        if (data.competencies().isEmpty()) {
            addCell(competencyTable, "Компетенции программы не заданы", fonts.normal(), Element.ALIGN_LEFT);
            addCell(competencyTable, "—", fonts.normal(), Element.ALIGN_CENTER);
        }
        else {
            for (CompetencyRow row : data.competencies()) {
                addCell(competencyTable, row.name(), fonts.normal(), Element.ALIGN_LEFT);
                addCell(competencyTable, String.format("%.1f", row.achievedLevelOutOfFive()), fonts.normal(),
                        Element.ALIGN_CENTER);
            }
        }
        document.add(competencyTable);

        addSectionTitle(document, "Выполненные задания", fonts.section());
        PdfPTable tasksTable = new PdfPTable(new float[] { 2.5f, 1.2f, 0.6f, 2.3f });
        tasksTable.setWidthPercentage(100);
        tasksTable.setSpacingAfter(12f);
        addHeaderCell(tasksTable, "Задание", fonts.tableHeader());
        addHeaderCell(tasksTable, "Завершено", fonts.tableHeader());
        addHeaderCell(tasksTable, "Оценка", fonts.tableHeader());
        addHeaderCell(tasksTable, "Артефакты / комментарий", fonts.tableHeader());
        if (data.completedTasks().isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Нет завершённых заданий", fonts.normal()));
            empty.setColspan(4);
            empty.setPadding(6f);
            tasksTable.addCell(empty);
        }
        else {
            for (CompletedTaskRow task : data.completedTasks()) {
                addCell(tasksTable, task.title(), fonts.normal(), Element.ALIGN_LEFT);
                addCell(tasksTable,
                        task.completedAt() != null ? DATE_TIME_FORMAT.format(task.completedAt()) : "—",
                        fonts.normal(), Element.ALIGN_CENTER);
                addCell(tasksTable, task.rating() != null ? task.rating().toString() : "—", fonts.normal(),
                        Element.ALIGN_CENTER);
                addCell(tasksTable, buildTaskDetails(task, fonts), Element.ALIGN_LEFT);
            }
        }
        document.add(tasksTable);

        addSectionTitle(document, "Промежуточные ассессменты", fonts.section());
        PdfPTable assessmentTable = new PdfPTable(new float[] { 1.1f, 1.4f, 0.6f, 0.6f, 0.6f, 0.6f, 1.8f });
        assessmentTable.setWidthPercentage(100);
        assessmentTable.setSpacingAfter(12f);
        addHeaderCell(assessmentTable, "Дата", fonts.tableHeader());
        addHeaderCell(assessmentTable, "Этап ИПР", fonts.tableHeader());
        addHeaderCell(assessmentTable, "Общий", fonts.tableHeader());
        addHeaderCell(assessmentTable, "Качество", fonts.tableHeader());
        addHeaderCell(assessmentTable, "Скорость", fonts.tableHeader());
        addHeaderCell(assessmentTable, "Коммуникация", fonts.tableHeader());
        addHeaderCell(assessmentTable, "Комментарий", fonts.tableHeader());
        if (data.assessments().isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Ассессменты отсутствуют", fonts.normal()));
            empty.setColspan(7);
            empty.setPadding(6f);
            assessmentTable.addCell(empty);
        }
        else {
            for (AssessmentRow row : data.assessments()) {
                addCell(assessmentTable,
                        row.date() != null ? DATE_TIME_FORMAT.format(row.date()) : "—",
                        fonts.normal(), Element.ALIGN_CENTER);
                addCell(assessmentTable, nullToDash(row.iprStageTitle()), fonts.normal(), Element.ALIGN_LEFT);
                addCell(assessmentTable, formatRating(row.overallRating()), fonts.normal(), Element.ALIGN_CENTER);
                addCell(assessmentTable, formatNullableRating(row.qualityRating()), fonts.normal(),
                        Element.ALIGN_CENTER);
                addCell(assessmentTable, formatNullableRating(row.speedRating()), fonts.normal(), Element.ALIGN_CENTER);
                addCell(assessmentTable, formatNullableRating(row.communicationRating()), fonts.normal(),
                        Element.ALIGN_CENTER);
                addCell(assessmentTable, nullToDash(row.comment()), fonts.normal(), Element.ALIGN_LEFT);
            }
        }
        document.add(assessmentTable);

        Paragraph footerNote = new Paragraph(
                "Отчёт сформировал: " + data.generatedByName(),
                fonts.small());
        footerNote.setSpacingBefore(16f);
        document.add(footerNote);
    }

    private Phrase buildTaskDetails(CompletedTaskRow task, FontSet fonts) {
        Phrase phrase = new Phrase();
        if (task.reviewComment() != null && !task.reviewComment().isBlank()) {
            phrase.add(new Chunk("Комментарий: " + task.reviewComment() + "\n", fonts.small()));
        }
        for (ArtifactLink artifact : task.artifacts()) {
            Anchor link = new Anchor(artifact.name(), fonts.link());
            link.setReference(artifact.url());
            phrase.add(link);
            phrase.add(Chunk.NEWLINE);
        }
        if (phrase.getChunks().isEmpty()) {
            phrase.add(new Chunk("—", fonts.small()));
        }
        return phrase;
    }

    private void addSectionTitle(Document document, String title, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(title, font);
        paragraph.setSpacingBefore(8f);
        paragraph.setSpacingAfter(6f);
        document.add(paragraph);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, FontSet fonts) {
        addCell(table, label, fonts.normal(), Element.ALIGN_LEFT);
        addCell(table, value, fonts.normal(), Element.ALIGN_LEFT);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 236, 245));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, Phrase phrase, int alignment) {
        PdfPCell cell = new PdfPCell(phrase);
        cell.setPadding(5f);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private String formatPercent(double value) {
        return String.format("%.1f%%", value);
    }

    private String formatRating(double value) {
        return String.format("%.2f", value);
    }

    private String formatNullableRating(Double value) {
        return value != null ? formatRating(value) : "—";
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private FontSet loadFonts() throws IOException {
        try (InputStream fontStream = new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream()) {
            byte[] fontBytes = fontStream.readAllBytes();
            BaseFont baseFont = BaseFont.createFont(
                    "DejaVuSans.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    fontBytes,
                    null
            );
            return new FontSet(
                    new Font(baseFont, 16, Font.BOLD),
                    new Font(baseFont, 12, Font.BOLD),
                    new Font(baseFont, 10, Font.NORMAL),
                    new Font(baseFont, 9, Font.NORMAL),
                    new Font(baseFont, 10, Font.BOLD),
                    new Font(baseFont, 9, Font.NORMAL, Color.BLUE)
            );
        }
    }

    private record FontSet(Font title, Font section, Font normal, Font small, Font tableHeader, Font link) {
    }

    private static final class ReportPageEvent extends PdfPageEventHelper {

        private final InternshipResultReportData data;
        private final FontSet fonts;

        private ReportPageEvent(InternshipResultReportData data, FontSet fonts) {
            this.data = data;
            this.fonts = fonts;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable header = new PdfPTable(1);
            header.setTotalWidth(document.right() - document.left());
            Paragraph headerText = new Paragraph();
            headerText.add(new Chunk(REPORT_TITLE + "\n", fonts.title()));
            headerText.add(new Chunk(
                    data.intern().fullName() + " · "
                            + data.program().directionName() + " · "
                            + data.program().title() + "\n",
                    fonts.small()));
            headerText.add(new Chunk(
                    "Программа: " + DATE_FORMAT.format(data.program().startDate())
                            + " — " + DATE_FORMAT.format(data.program().endDate())
                            + "   |   ИПР: " + DATE_FORMAT.format(data.ipr().startDate())
                            + " — " + DATE_FORMAT.format(data.ipr().endDate())
                            + "   |   " + data.reportId(),
                    fonts.small()));
            PdfPCell headerCell = new PdfPCell(headerText);
            headerCell.setBorder(Rectangle.BOTTOM);
            headerCell.setPaddingBottom(6f);
            header.addCell(headerCell);
            header.writeSelectedRows(0, -1, document.left(), document.top() + 50, writer.getDirectContent());

            String footerLeft = "Сформирован: " + DATE_TIME_FORMAT.format(data.generatedAt());
            String footerRight = "Стр. " + writer.getPageNumber();
            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_LEFT,
                    new Phrase(footerLeft, fonts.small()),
                    document.left(),
                    document.bottom() - 20,
                    0
            );
            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase(footerRight, fonts.small()),
                    document.right(),
                    document.bottom() - 20,
                    0
            );
        }
    }
}
