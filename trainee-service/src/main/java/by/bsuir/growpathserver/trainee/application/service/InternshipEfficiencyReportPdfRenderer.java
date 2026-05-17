package by.bsuir.growpathserver.trainee.application.service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData.DeadlineRow;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData.InternProgressRow;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData.MentorWorkloadRow;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InternshipEfficiencyReportPdfRenderer {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final String ORGANIZATION_NAME = "GrowPath";
    private static final String REPORT_TITLE = "Отчет об эффективности стажировок";

    public byte[] render(InternshipEfficiencyReportData data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 64, 48);
            PdfWriter writer = PdfWriter.getInstance(document, output);
            FontSet fonts = loadFonts();
            writer.setPageEvent(new ReportPageEvent(data, fonts));
            document.open();
            writeBody(document, data, fonts);
            document.close();
            return output.toByteArray();
        }
        catch (IOException | DocumentException ex) {
            log.error("Failed to render internship efficiency report PDF", ex);
            throw new IllegalStateException("Failed to generate report PDF", ex);
        }
    }

    private void writeBody(Document document, InternshipEfficiencyReportData data, FontSet fonts)
            throws DocumentException {
        addSectionTitle(document, "Сводка по программе", fonts.section());
        PdfPTable summary = new PdfPTable(4);
        summary.setWidthPercentage(100);
        summary.setSpacingAfter(10f);
        addHeaderCell(summary, "Всего задач", fonts.tableHeader());
        addHeaderCell(summary, "Выполнено", fonts.tableHeader());
        addHeaderCell(summary, "Просрочено", fonts.tableHeader());
        addHeaderCell(summary, "% выполнения", fonts.tableHeader());
        addCell(summary, String.valueOf(data.summary().totalTasks()), fonts.normal(), Element.ALIGN_CENTER);
        addCell(summary, String.valueOf(data.summary().completedTasks()), fonts.normal(), Element.ALIGN_CENTER);
        addCell(summary, String.valueOf(data.summary().overdueTasks()), fonts.normal(), Element.ALIGN_CENTER);
        addCell(summary, formatPercent(data.summary().completionRatePercent()), fonts.normal(), Element.ALIGN_CENTER);
        document.add(summary);

        PdfPTable summary2 = new PdfPTable(3);
        summary2.setWidthPercentage(100);
        summary2.setSpacingAfter(12f);
        addHeaderCell(summary2, "В работе", fonts.tableHeader());
        addHeaderCell(summary2, "На проверке", fonts.tableHeader());
        addHeaderCell(summary2, "Среднее время проверки (ч)", fonts.tableHeader());
        addCell(summary2, String.valueOf(data.summary().inProgressTasks()), fonts.normal(), Element.ALIGN_CENTER);
        addCell(summary2, String.valueOf(data.summary().pendingReviews()), fonts.normal(), Element.ALIGN_CENTER);
        addCell(summary2, formatHours(data.summary().averageReviewHours()), fonts.normal(), Element.ALIGN_CENTER);
        document.add(summary2);

        addSectionTitle(document, "Загруженность менторов", fonts.section());
        addMentorTable(document, data, fonts);

        addSectionTitle(document, "Прогресс стажёров потока", fonts.section());
        addInternTable(document, data, fonts);

        addSectionTitle(document, "Анализ дедлайнов (активные задачи)", fonts.section());
        addDeadlineTable(document, data, fonts);

        Paragraph footerNote = new Paragraph("Отчёт сформировал: " + data.generatedByName(), fonts.small());
        footerNote.setSpacingBefore(12f);
        document.add(footerNote);
    }

    private void addMentorTable(Document document, InternshipEfficiencyReportData data, FontSet fonts)
            throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 2.2f, 0.7f, 0.7f, 0.7f, 0.7f, 0.9f, 1f });
        table.setWidthPercentage(100);
        table.setSpacingAfter(10f);
        addHeaderCell(table, "Ментор", fonts.tableHeader());
        addHeaderCell(table, "Стажёров", fonts.tableHeader());
        addHeaderCell(table, "Активных задач", fonts.tableHeader());
        addHeaderCell(table, "На проверке", fonts.tableHeader());
        addHeaderCell(table, "Ср. проверка (ч)", fonts.tableHeader());
        addHeaderCell(table, "Загрузка", fonts.tableHeader());
        addHeaderCell(table, "ID", fonts.tableHeader());
        if (data.mentorWorkload().isEmpty()) {
            addEmptyRow(table, 7, "Нет данных по менторам", fonts);
        }
        else {
            for (MentorWorkloadRow row : data.mentorWorkload()) {
                addCell(table, row.mentorName(), fonts.normal(), Element.ALIGN_LEFT);
                addCell(table, String.valueOf(row.totalInterns()), fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, String.valueOf(row.activeTasks()), fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, String.valueOf(row.pendingReviews()), fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, formatHours(row.averageReviewHours()), fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, row.workloadLabel(), fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, String.valueOf(row.mentorId()), fonts.small(), Element.ALIGN_CENTER);
            }
        }
        document.add(table);
    }

    private void addInternTable(Document document, InternshipEfficiencyReportData data, FontSet fonts)
            throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 2.5f, 0.6f, 0.6f, 0.7f, 0.8f, 0.5f });
        table.setWidthPercentage(100);
        table.setSpacingAfter(10f);
        addHeaderCell(table, "Стажёр", fonts.tableHeader());
        addHeaderCell(table, "Всего", fonts.tableHeader());
        addHeaderCell(table, "Готово", fonts.tableHeader());
        addHeaderCell(table, "%", fonts.tableHeader());
        addHeaderCell(table, "Статус", fonts.tableHeader());
        addHeaderCell(table, "ID", fonts.tableHeader());
        if (data.internProgress().isEmpty()) {
            addEmptyRow(table, 6, "Нет назначенных стажёров", fonts);
        }
        else {
            for (InternProgressRow row : data.internProgress()) {
                addCell(table, row.internName(), fonts.normal(), Element.ALIGN_LEFT);
                addCell(table, String.valueOf(row.totalTasks()), fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, String.valueOf(row.completedTasks()), fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, formatPercent(row.completionRatePercent()), fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, row.behindSchedule() ? "Отстаёт" : "В графике", fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, String.valueOf(row.internId()), fonts.small(), Element.ALIGN_CENTER);
            }
        }
        document.add(table);
    }

    private void addDeadlineTable(Document document, InternshipEfficiencyReportData data, FontSet fonts)
            throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 2.5f, 1.5f, 1.5f, 1.2f, 0.8f, 0.6f, 0.5f });
        table.setWidthPercentage(100);
        table.setSpacingAfter(10f);
        addHeaderCell(table, "Задача", fonts.tableHeader());
        addHeaderCell(table, "Стажёр", fonts.tableHeader());
        addHeaderCell(table, "Ментор", fonts.tableHeader());
        addHeaderCell(table, "Дедлайн", fonts.tableHeader());
        addHeaderCell(table, "Статус", fonts.tableHeader());
        addHeaderCell(table, "Просрочка", fonts.tableHeader());
        addHeaderCell(table, "ID", fonts.tableHeader());
        if (data.deadlines().isEmpty()) {
            addEmptyRow(table, 7, "Нет активных задач с дедлайном", fonts);
        }
        else {
            for (DeadlineRow row : data.deadlines()) {
                addCell(table, row.title(), fonts.normal(), Element.ALIGN_LEFT);
                addCell(table, row.assigneeName(), fonts.normal(), Element.ALIGN_LEFT);
                addCell(table, row.mentorName(), fonts.normal(), Element.ALIGN_LEFT);
                addCell(table, DATE_TIME_FORMAT.format(row.dueDate()), fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, row.status(), fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, row.overdue() ? "Да" : "Нет", fonts.normal(), Element.ALIGN_CENTER);
                addCell(table, String.valueOf(row.taskId()), fonts.small(), Element.ALIGN_CENTER);
            }
        }
        document.add(table);
    }

    private void addSectionTitle(Document document, String title, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(title, font);
        paragraph.setSpacingBefore(6f);
        paragraph.setSpacingAfter(4f);
        document.add(paragraph);
    }

    private void addEmptyRow(PdfPTable table, int colspan, String message, FontSet fonts) {
        PdfPCell cell = new PdfPCell(new Phrase(message, fonts.normal()));
        cell.setColspan(colspan);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 236, 245));
        cell.setPadding(5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4f);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private String formatPercent(double value) {
        return String.format("%.1f%%", value);
    }

    private String formatHours(double value) {
        return String.format("%.1f", value);
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
                    new Font(baseFont, 14, Font.BOLD),
                    new Font(baseFont, 11, Font.BOLD),
                    new Font(baseFont, 9, Font.NORMAL),
                    new Font(baseFont, 8, Font.NORMAL),
                    new Font(baseFont, 9, Font.BOLD)
            );
        }
    }

    private record FontSet(Font title, Font section, Font normal, Font small, Font tableHeader) {
    }

    private static final class ReportPageEvent extends PdfPageEventHelper {

        private final InternshipEfficiencyReportData data;
        private final FontSet fonts;

        private ReportPageEvent(InternshipEfficiencyReportData data, FontSet fonts) {
            this.data = data;
            this.fonts = fonts;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Paragraph header = new Paragraph();
            header.add(new com.lowagie.text.Chunk(ORGANIZATION_NAME + "  |  " + REPORT_TITLE + "\n", fonts.title()));
            header.add(new com.lowagie.text.Chunk(
                    data.program().title() + "  |  "
                            + (data.program().directionName() != null ? data.program().directionName() : "—")
                            + "\n",
                    fonts.small()));
            header.add(new com.lowagie.text.Chunk(
                    "Период программы: " + DATE_FORMAT.format(data.program().startDate())
                            + " — " + DATE_FORMAT.format(data.program().endDate())
                            + "   |   Сформирован: " + DATE_TIME_FORMAT.format(data.generatedAt())
                            + "   |   " + data.reportId(),
                    fonts.small()));

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_LEFT,
                    header,
                    document.left(),
                    document.top() + 10,
                    0
            );

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase("Стр. " + writer.getPageNumber(), fonts.small()),
                    document.right(),
                    document.bottom() - 16,
                    0
            );
        }
    }
}
