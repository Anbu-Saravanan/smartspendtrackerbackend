package com.SmartSpendExpense.controller;

import com.SmartSpendExpense.dto.request.ExpenseRequestDTO;
import com.SmartSpendExpense.dto.response.ExpenseResponseDTO;
import com.SmartSpendExpense.model.Expense;
import com.SmartSpendExpense.model.User;
import com.SmartSpendExpense.repository.ExpenseRepository;
import com.SmartSpendExpense.repository.UserRepository;
import com.SmartSpendExpense.service.ExpenseService;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")

public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    public String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        // Now fetch userId by email:
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/create")
    public ResponseEntity<ExpenseResponseDTO> createExpense(@RequestBody ExpenseRequestDTO expenseDTO) {
        ExpenseResponseDTO created = expenseService.createExpense(expenseDTO, getCurrentUserId());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpenses(
            @RequestParam(required = false) String category,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date,
            @RequestParam(required = false) BigDecimal amount
    ) {
        String userId = getCurrentUserId();
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        // Filter in Java (for simplicity)
        if (category != null && !category.isEmpty())
            expenses = expenses.stream().filter(e -> e.getCategory().equalsIgnoreCase(category)).collect(Collectors.toList());
        if (date != null)
            expenses = expenses.stream().filter(e ->
                    e.getDate() != null &&
                            e.getDate().toInstant().truncatedTo(ChronoUnit.DAYS)
                                    .equals(date.toInstant().truncatedTo(ChronoUnit.DAYS))
            ).collect(Collectors.toList());
        if (amount != null)
            expenses = expenses.stream().filter(e -> e.getAmount().compareTo(amount) == 0).collect(Collectors.toList());

        List<ExpenseResponseDTO> result = expenses.stream()
                .map(expenseService::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }


    @GetMapping("getById/{id}")
    public ResponseEntity<ExpenseResponseDTO> getExpense(@PathVariable String id) {
        return expenseService.getExpenseById(id, getCurrentUserId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(@PathVariable String id, @RequestBody ExpenseRequestDTO updatedDTO) {
        return expenseService.updateExpense(id, updatedDTO, getCurrentUserId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
        boolean deleted = expenseService.deleteExpense(id, getCurrentUserId());
        if (deleted) return ResponseEntity.noContent().build();
        else return ResponseEntity.notFound().build();
    }

    //generated pdf and  excel
    @GetMapping("/export/pdf")
    public void exportExpensesToPdf(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.pdf");

        List<Expense> expenses = expenseRepository.findByUserId(getCurrentUserId());

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        document.add(new Paragraph("Expense Report"));
        PdfPTable table = new PdfPTable(5); // Title, Amount, Category, Type, Date

        table.addCell("Title");
        table.addCell("Amount");
        table.addCell("Category");
        table.addCell("Type");
        table.addCell("Date");

        for (Expense exp : expenses) {
            table.addCell(exp.getTitle());
            table.addCell(exp.getAmount().toString());
            table.addCell(exp.getCategory());
            table.addCell(exp.getType());
            table.addCell(exp.getDate().toString());
        }

        document.add(table);
        document.close();
    }

    @GetMapping("/export/excel")
    public void exportExpensesToExcel(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.xlsx");

        List<Expense> expenses = expenseRepository.findByUserId(getCurrentUserId());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Expenses");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Title");
        header.createCell(1).setCellValue("Amount");
        header.createCell(2).setCellValue("Category");
        header.createCell(3).setCellValue("Type");
        header.createCell(4).setCellValue("Date");

        int rowNum = 1;
        for (Expense exp : expenses) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(exp.getTitle());
            row.createCell(1).setCellValue(exp.getAmount().toString());
            row.createCell(2).setCellValue(exp.getCategory());
            row.createCell(3).setCellValue(exp.getType());
            row.createCell(4).setCellValue(exp.getDate().toString());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
