package com.SmartSpendExpense.controller;

import com.SmartSpendExpense.dto.request.MonthlySummaryDTO;
import com.SmartSpendExpense.service.DashboardService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public MonthlySummaryDTO getSummary(
        @RequestParam String userId,
        @RequestParam int month,
        @RequestParam int year
    ) {
        return dashboardService.getMonthlySummary(userId, month, year);
    }

    //Downloading pdf
    @GetMapping("/report/pdf")
    public void downloadPdfReport(
            @RequestParam String userId,
            @RequestParam int month,
            @RequestParam int year,
            HttpServletResponse response
    ) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=MonthlySummary.pdf");
        dashboardService.generatePdfReport(userId, month, year, response.getOutputStream());
    }


    // Excel Report Downloading
    @GetMapping("/report/excel")
    public void downloadExcelReport(
            @RequestParam String userId,
            @RequestParam int month,
            @RequestParam int year,
            HttpServletResponse response
    ) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=MonthlySummary.xlsx");
        dashboardService.generateExcelReport(userId, month, year, response.getOutputStream());
    }

}
