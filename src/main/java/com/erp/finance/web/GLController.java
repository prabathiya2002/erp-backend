package com.erp.finance.web;

import com.erp.finance.service.GLService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/gl")
public class GLController {
    private final GLService glService;

    public GLController(GLService glService) {
        this.glService = glService;
    }

    @GetMapping("/trial-balance")
    public GLService.TrialBalance trialBalance(@RequestParam("period") String period) {
        return glService.computeTrialBalance(period);
    }

    @GetMapping("/trial-balance-range")
    public GLService.TrialBalance trialBalanceByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return glService.computeTrialBalanceByDateRange(start, end);
    }
}
