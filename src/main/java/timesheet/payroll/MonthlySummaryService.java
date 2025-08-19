package timesheet.payroll;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import timesheet.admin.dao.Employeedao;
import timesheet.admin.repo.EmployeeRepo;
import timesheet.employee.dao.SummaryEntry;
import timesheet.employee.repo.SummaryRepository;
import timesheet.payroll.dao.MonthlySummary;
import timesheet.payroll.repo.MonthlySummaryRepository;

@Service
public class MonthlySummaryService {

	@Autowired
	private SummaryRepository summaryRepository;

	@Autowired
	private MonthlySummaryRepository monthlySummaryRepository;
	
	  @Autowired                       // NEW
	    private EmployeeRepo employeeRepo;

	   public ResponseEntity<String> generateMonthlySummary(String username, String month) {

	        /* ── 0.  Look‑up employee & onboarding date ───────────────── */
	        Employeedao emp = employeeRepo.findByeName(username);
	        if (emp == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                                 .body("Employee not found.");
	        }

	        LocalDate onboardDate;
	        try {
	            /*
	             * Accept both "yyyy-MM-dd" (2025‑04‑17) and "dd/MM/yyyy" (17/04/2025).
	             * Add other patterns if your DB contains mixed data.
	             */
	            DateTimeFormatter[] fmts = {
	                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
	                DateTimeFormatter.ofPattern("dd/MM/yyyy")
	            };
	            onboardDate = tryParse(emp.getOnboard(), fmts);   // helper shown later
	            if (onboardDate == null) throw new DateTimeParseException("","",0);
	        } catch (DateTimeParseException ex) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                                 .body("Cannot parse onboard date for user " + username);
	        }

	        YearMonth targetYm = YearMonth.parse(month);          // yyyy‑MM
	        YearMonth onboardYm = YearMonth.from(onboardDate);

	        boolean firstMonthSinglePeriod =
	                onboardYm.equals(targetYm) && onboardDate.getDayOfMonth() > 15;

	        /* ── 1.  Fetch summaries needed for this computation ──────── */
	        String period1 = getPeriod1(month);
	        String period2 = getPeriod2(month);

	        SummaryEntry s1 = firstMonthSinglePeriod
	                          ? null                                   // ignore 1st period
	                          : summaryRepository.findByUsernameAndPeriod(username, period1);

	        SummaryEntry s2 = summaryRepository.findByUsernameAndPeriod(username, period2);

	        /* ── 2.  Guard‑rails: make sure the required period(s) exist & approved ─── */
	        if (!firstMonthSinglePeriod && (s1 == null || s2 == null)) {
	            return ResponseEntity.ok("Pending submission of one or both periods.");
	        }
	        if (firstMonthSinglePeriod && s2 == null) {
	            return ResponseEntity.ok("Pending submission of second period.");
	        }

	        if (!firstMonthSinglePeriod) {
	            if (!"Approved".equalsIgnoreCase(s1.getStatus()) ||
	                !"Approved".equalsIgnoreCase(s2.getStatus())) {
	                return ResponseEntity.ok("One or both periods are not yet approved.");
	            }
	        } else {   // single‑period path
	            if (!"Approved".equalsIgnoreCase(s2.getStatus())) {
	                return ResponseEntity.ok("Second period is not yet approved.");
	            }
	        }

	        /* ── 3.  Aggregate numbers ────────────────────────────────── */
	        Map<String,Object> data2 = s2.getSummaryData();  // always needed
	        Map<String,Object> data1 = firstMonthSinglePeriod
	                                   ? Collections.emptyMap()
	                                   : s1.getSummaryData();

	        double totalHours = getDouble(data1.get("totalHours"))
	                          + getDouble(data2.get("totalHours"));
	        double cl   = getDouble(data1.get("casualLeaveDays")) + getDouble(data2.get("casualLeaveDays"));
	        double sl   = getDouble(data1.get("sickLeaveDays"))   + getDouble(data2.get("sickLeaveDays"));
	        double pl   = getDouble(data1.get("paidLeaveDays"))   + getDouble(data2.get("paidLeaveDays"));
	        double abs  = getDouble(data1.get("totalAbsences"))   + getDouble(data2.get("totalAbsences"));

	        double totalWorkingDays = totalHours / 9.0;
	        double lop              = calculateLOP(pl);   // TODO: real formula

	        /* ── 4.  Upsert MonthlySummary ─────────────────────────────── */
	        MonthlySummary summary = monthlySummaryRepository
	                                    .findByUsernameAndMonth(username, month)
	                                    .orElse(new MonthlySummary());

	        summary.setUsername(username);
	        summary.setMonth(month);
	        summary.setCasualLeaveDays(cl);
	        summary.setSickLeaveDays(sl);
	        summary.setTotalAbsences(abs / 9);
	        summary.setTotalLOPDays(lop);
	        summary.setTotalWorkingDays(totalWorkingDays);

	        monthlySummaryRepository.save(summary);

	        return ResponseEntity.ok("Monthly summary generated.");
	    }

	private String getPeriod1(String month) {

		String year = month.substring(0, 4);
		String mm = month.substring(5);
		return "01/" + mm + "/" + year + " - 15/" + mm + "/" + year;
	}

	private String getPeriod2(String month) {

		YearMonth yearMonth = YearMonth.parse(month);
		int lastDay = yearMonth.lengthOfMonth();
		String year = month.substring(0, 4);
		String mm = month.substring(5);
		return "16/" + mm + "/" + year + " - " + lastDay + "/" + mm + "/" + year;
	}

	private double getDouble(Object value) {
		return value != null ? Double.parseDouble(value.toString()) : 0.0;
	}

	private double getDoubleOrZero(Object value) {
		return value != null ? Double.parseDouble(value.toString()) : 0.0;
	}

	private double calculateLOP(double pl) {
		return pl;
	}
	
	private LocalDate tryParse(String raw, DateTimeFormatter[] fmts) {
        for (DateTimeFormatter fmt : fmts) {
            try { return LocalDate.parse(raw, fmt); }
            catch (DateTimeParseException ignored) { }
        }
        return null;
    }

}