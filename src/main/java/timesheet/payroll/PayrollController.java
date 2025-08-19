package timesheet.payroll;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import timesheet.admin.dao.Employeedao;
import timesheet.admin.dao.Holidays;
import timesheet.admin.repo.EmployeeRepo;
import timesheet.admin.repo.HolidayRepo;
import timesheet.emails.EmailServiceController;
import timesheet.payroll.dao.AddSalary;
import timesheet.payroll.dao.ApprovedPayslip;
import timesheet.payroll.dao.Bankdetails;
import timesheet.payroll.dao.MonthlySummary;
import timesheet.payroll.repo.AddSalaryRepo;
import timesheet.payroll.repo.ApprovedPayslipRepo;
import timesheet.payroll.repo.BankDetailsRepo;
import timesheet.payroll.repo.MonthlySummaryRepository;

@RestController
@RequestMapping("/payslip")
public class PayrollController {

	@Autowired
	private EmployeeRepo EmpRepo;

	@Autowired
	private MonthlySummaryRepository monthlySummaryRepository;

	@Autowired
	private AddSalaryRepo addSalaryrepo;

	@Autowired
	private BankDetailsRepo bankdetailsrepo;

	@Autowired
	private ApprovedPayslipRepo approvedPayslip;

	@Autowired
	private HolidayRepo holidayrepo;

	@Autowired
	private EmailServiceController emailService;

	@GetMapping("/EmployePayslip/{month}")
	public ResponseEntity<List<Map<String, String>>> getUsersForMonth(@PathVariable String month) {

		List<MonthlySummary> summaries = monthlySummaryRepository.findByMonthAndIsPayslipGeneratedFalse(month);

		Set<String> uniqueUsernames = new HashSet<>();
		List<Map<String, String>> result = new ArrayList<>();

		for (MonthlySummary summary : summaries) {
			String username = summary.getUsername();
			if (uniqueUsernames.add(username)) {

				Employeedao emp = EmpRepo.findByeName(username);
				if (emp != null) {
					String displayName = emp.geteName() + " - " + emp.getDesignation();

					Map<String, String> map = new HashMap<>();
					map.put("username", username);
					map.put("display", displayName);
					result.add(map);
				}
			}
		}

		return ResponseEntity.ok(result);
	}

	@GetMapping("/details")
	public ResponseEntity<?> getPayslipDetails(@RequestParam String username,
	                                           @RequestParam String month) {

	    Map<String, Object> result = new HashMap<>();

	    /* ── 1. Parse payslip month (yyyy‑MM) ─────────────────────── */
	    YearMonth yearMonth;
	    try {
	        yearMonth = YearMonth.parse(month);
	    } catch (DateTimeParseException ex) {
	        return ResponseEntity.badRequest()
	                             .body(Map.of("error", "Invalid month format. Expected yyyy‑MM"));
	    }
	    int targetYear  = yearMonth.getYear();
	    int targetMonth = yearMonth.getMonthValue();
	    LocalDate monthStart = yearMonth.atDay(1);
	    LocalDate monthEnd   = yearMonth.atEndOfMonth();

	    /* ── 2. Employee & DOJ ───────────────────────────────────── */
	    Employeedao employee = EmpRepo.findByeName(username);
	    if (employee == null)
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                             .body(Map.of("error", "Employee details not found"));

	    LocalDate doj;
	    try {
	        doj = LocalDate.parse(employee.getOnboard());        // yyyy‑MM‑dd
	    } catch (DateTimeParseException ex) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body(Map.of("error", "Invalid onboard date format"));
	    }
	    boolean isOnboardMonth = doj.getYear() == targetYear &&
	                             doj.getMonthValue() == targetMonth;

	    /* ── 3. Sundays (skip pre‑DOJ if onboarding) ─────────────── */
	    int totalSundays = 0;
	    for (LocalDate d = monthStart; !d.isAfter(monthEnd); d = d.plusDays(1)) {
	        if (d.getDayOfWeek() == DayOfWeek.SUNDAY &&
	           (!isOnboardMonth || !d.isBefore(doj)))
	            totalSundays++;
	    }

	    /* ── 4. Holidays (skip pre‑DOJ if onboarding) ────────────── */
	    DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	    int totalHolidays = 0;
	    for (Holidays h : holidayrepo.findByyear(targetYear)) {
	        try {
	            LocalDate hDate = LocalDate.parse(h.getDate(), df);
	            if (hDate.getMonthValue() == targetMonth &&
	               (!isOnboardMonth || !hDate.isBefore(doj)))
	                totalHolidays++;
	        } catch (DateTimeParseException ignored) {}
	    }

	    /* ── 5. Monthly summary row ─────────────────────────────── */
	    Optional<MonthlySummary> opt =
	            monthlySummaryRepository.findByUsernameAndMonth(username, month);
	    if (opt.isEmpty())
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                             .body(Map.of("error", "Summary not found"));
	    MonthlySummary summary = opt.get();

	    /* ── 6. Std / worked / leave figures (same as old) ───────── */
	    double    stddays      = summary.getTotalWorkingDays() + totalSundays + totalHolidays;
	    double    totalworked  = (summary.getTotalWorkingDays() - summary.getTotalAbsences())
	                          + totalSundays + totalHolidays;
	    double lopDays      = summary.getTotalLOPDays() != null ? summary.getTotalLOPDays() : 0.0;

	    /* ── 7. Pick the salary row valid for this month ─────────── */
	    DateTimeFormatter effFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    Optional<AddSalary> salOpt = addSalaryrepo.findByEmployeename(username)
	        .stream()
	        .filter(s -> {
	            try { return !YearMonth.from(LocalDate.parse(s.getEffectiveFrom(), effFmt))
	                                   .isAfter(yearMonth);
	            } catch (Exception e) { return false; }
	        })
	        .max((a, b) -> {                     // latest ≤ payslip month
	            try {
	                return LocalDate.parse(a.getEffectiveFrom(), effFmt)
	                        .compareTo(LocalDate.parse(b.getEffectiveFrom(), effFmt));
	            } catch (Exception e) { return 0; }
	        });

	    if (salOpt.isEmpty())
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                             .body(Map.of("error", "No valid salary for selected month"));
	    AddSalary salaryRow = salOpt.get();
	    double basicSalary  = Double.parseDouble(salaryRow.getMonthsalary());

	    /* ── 8. Prorated salary (handle onboarding month) ─────────────── */
	    int daysInMonth = yearMonth.lengthOfMonth();          // 30, 31, 28 …
	    double perCalDay = basicSalary / daysInMonth;         // true daily rate

	    LocalDate effDate = LocalDate.parse(salaryRow.getEffectiveFrom(), effFmt);

	    /* Payroll actually starts on the later of DoJ or effectiveFrom */
	    LocalDate payrollStart = doj.isAfter(effDate) ? doj : effDate;

	    /* Is this the month when payrollStart sits? */
	    boolean prorateNeeded =
	            payrollStart.getYear()  == targetYear &&
	            payrollStart.getMonthValue() == targetMonth;

	    /* Days that person is on the rolls in this month            */
	    /* (+1 because we want both start & end dates inclusive)      */
	    long onRollCalDays = prorateNeeded
	            ? ChronoUnit.DAYS.between(payrollStart, monthEnd.plusDays(1))
	            : daysInMonth;

	    /* totalworked already excludes LOP & unpaid absences,        */
	    /* so we treat it as “paid attendance days”.                  */
	    double payableDays = stddays;

	    double grossPay   = perCalDay * payableDays;          // pay only for presence
	    double deductions = perCalDay * lopDays;              // keep old LOP rule
	    double netPay     = grossPay - deductions;


	    /* ── 9. Build response (field names identical) ───────────── */
	    result.put("stddays",      stddays);
	    result.put("totalworked",  totalworked);
	    result.put("totalleaves",  summary.getTotalAbsences());
	    result.put("lop",          lopDays);
	    result.put("basicSalary",  basicSalary);
	    result.put("deduction",    deductions);
	    result.put("netPay",       netPay);

	    result.put("name",         employee.geteName());
	    result.put("onboardDate",  employee.getOnboard());
	    result.put("designation",  employee.getDesignation());

	    return ResponseEntity.ok(result);
	}


	@PostMapping("/approvepayslip")
	public ResponseEntity<?> approvePayslip(@RequestBody ApprovedPayslip payslipData) {

		String username = payslipData.getUsername().trim();

		Bankdetails bankDetails = bankdetailsrepo.findByEmployeenameIgnoreCase(username);
		if (bankDetails == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Bank details not found for employee: " + username));
		}

		payslipData.setAccountHolder(bankDetails.getAccountHolder());
		payslipData.setBankName(bankDetails.getBankName());
		payslipData.setAccountNumber(bankDetails.getAccountNumber());
		payslipData.setLocation("Salem");

		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd: HH:mm");
		String formattedDateTime = now.format(formatter);

		payslipData.setSalaryProcessAt(formattedDateTime);
		payslipData.setApprovedAt(formattedDateTime);

		System.out.println(formattedDateTime);

		approvedPayslip.save(payslipData);

		Optional<MonthlySummary> summaryOpt = monthlySummaryRepository.findByUsernameAndMonth(username,
				payslipData.getMonth());
		if (summaryOpt.isPresent()) {
			MonthlySummary summary = summaryOpt.get();
			summary.setIsPayslipGenerated(true);
			monthlySummaryRepository.save(summary);
		}

		// Fetch employee email and name to send email
		Employeedao empData = EmpRepo.findByeName(username);
		if (empData != null) {
			try {
				emailService.sendPayslipApprovedEmail(empData.geteMail(), empData.geteName(), payslipData.getMonth());
			} catch (MessagingException | IOException e) {
				// Log error but don’t fail the request
				e.printStackTrace();
			}
		}

		return ResponseEntity.ok(Map.of("message", "Payslip approved and saved successfully"));
	}

	@GetMapping("/getPayslipdata")
	public ResponseEntity<List<ApprovedPayslip>> getExpense() {
		List<ApprovedPayslip> Payslip = approvedPayslip.findAll();

		return ResponseEntity.ok(Payslip);
	}

	@GetMapping("/EmpPayslipSummary")
	public ResponseEntity<?> getPayslipSummary(@RequestParam String username, @RequestParam String month) {

		ApprovedPayslip payslip = approvedPayslip.findByUsernameAndMonth(username, month);

		if (payslip != null) {
			return ResponseEntity.ok(payslip);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("Payslip not found for " + username + " and month " + month);
		}
	}

}
