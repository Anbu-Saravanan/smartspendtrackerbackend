package com.SmartSpendExpense.service;

import com.SmartSpendExpense.dto.request.MonthlySummaryDTO;
import com.SmartSpendExpense.model.Budget;
import com.SmartSpendExpense.model.Expense;
import com.SmartSpendExpense.repository.BudgetRepository;
import com.SmartSpendExpense.repository.ExpenseRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private BudgetRepository budgetRepository;

    public MonthlySummaryDTO getMonthlySummary(String userId, int month, int year) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth()); // last day of month

        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);
        // 2. Calculate total spent and per-category totals

        BigDecimal totalSpent = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        for (Expense e : expenses) {
            categoryTotals.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
            totalSpent = totalSpent.add(e.getAmount());

        }

        // 3. Get all budgets for the user for the same month/year
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

        Map<String, BigDecimal> budgetsMap = new HashMap<>();
        for (Budget b : budgets) {
            budgetsMap.put(b.getCategory(), b.getLimitAmount());
        }

        // 4. Build and return DTO
        return new MonthlySummaryDTO(month, year, totalSpent, categoryTotals, budgetsMap);
    }

    //Downloading pdf
    public void generatePdfReport(String userId, int month, int year, OutputStream out) throws Exception {
        MonthlySummaryDTO summary = getMonthlySummary(userId, month, year);

        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("Monthly Summary Report"));
        document.add(new Paragraph("Month: " + month + " / Year: " + year));
        document.add(new Paragraph("Total Spent: " + summary.getTotalSpent()));

        document.add(new Paragraph("Category Totals:"));
        for (Map.Entry<String, BigDecimal> entry : summary.getCategoryTotals().entrySet()) {
            document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
        }

        document.add(new Paragraph("Budgets:"));
        for (Map.Entry<String, BigDecimal> entry : summary.getBudgets().entrySet()) {
            document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
        }

        document.close();
    }

    //Excel Report Downloading
    public void generateExcelReport(String userId, int month, int year, OutputStream out) throws Exception {
        MonthlySummaryDTO summary = getMonthlySummary(userId, month, year);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Monthly Summary");

        int rowIdx = 0;
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue("Month");
        row.createCell(1).setCellValue("Year");
        row.createCell(2).setCellValue("Total Spent");
        Row row2 = sheet.createRow(rowIdx++);
        row2.createCell(0).setCellValue(month);
        row2.createCell(1).setCellValue(year);
        row2.createCell(2).setCellValue(summary.getTotalSpent().doubleValue());

        row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue("Category");
        row.createCell(1).setCellValue("Spent");
        for (Map.Entry<String, BigDecimal> entry : summary.getCategoryTotals().entrySet()) {
            row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().doubleValue());
        }

        row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue("Budget Category");
        row.createCell(1).setCellValue("Budget Limit");
        for (Map.Entry<String, BigDecimal> entry : summary.getBudgets().entrySet()) {
            row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().doubleValue());
        }

        workbook.write(out);
        workbook.close();
    }


}
