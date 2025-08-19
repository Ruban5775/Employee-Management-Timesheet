package timesheet.employee;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import jakarta.transaction.Transactional;
import timesheet.admin.dao.AllowedLeaves;
import timesheet.admin.dao.Assignment;
import timesheet.admin.dao.CasualLeaveTracker;
import timesheet.admin.repo.AllowedLeavesRepository;
import timesheet.admin.repo.AssignmentRepository;
import timesheet.admin.repo.CasualLeaveTrackerRepo;
import timesheet.employee.dao.EmpExpensedao;
import timesheet.employee.dao.Preference;
import timesheet.employee.dao.SummaryEntry;
import timesheet.employee.dao.TimesheetEntry;
import timesheet.employee.repo.EmpExpenseRepository;
import timesheet.employee.repo.PreferenceRepository;
import timesheet.employee.repo.SummaryRepository;
import timesheet.employee.repo.TimesheetRepository;
import timesheet.employee.service.TimesheetService;
import timesheet.notification.NotificationService;
import timesheet.payroll.MonthlySummaryService;

@RestController
public class TimesheetController {

	@Autowired
	private TimesheetService timesheetService;

	@Autowired
	private TimesheetRepository timesheetRepository;

	@Autowired
	private PreferenceRepository preferenceRepository;

	@Autowired
	private SummaryRepository summaryRepository;

	@Autowired
	private AssignmentRepository assignrepo;

	@Autowired
	private AllowedLeavesRepository allowedleaverepo;

	@Autowired
	private MonthlySummaryService monthlySummaryService;

	@Autowired
	private CasualLeaveTrackerRepo casualLeaveTrackerRepo;

	private EmpExpenseRepository expenseRepository;

	private final NotificationService notificationService;
	private final SimpMessagingTemplate messagingTemplate; 

	private static final String UPLOAD_DIR = "uploads/receipts/";

	// ✅ Correct Constructor Injection

	public TimesheetController(TimesheetService timesheetService, NotificationService notificationService,
			SimpMessagingTemplate messagingTemplate, EmpExpenseRepository expenseRepository) {
		this.timesheetService = timesheetService;
		this.notificationService = notificationService;
		this.messagingTemplate = messagingTemplate;
		this.expenseRepository = expenseRepository;
	}

	@GetMapping("/getTimesheet")
	public ResponseEntity<List<TimesheetEntry>> getTimesheet(@RequestParam String username,
			@RequestParam String period) {

		List<TimesheetEntry> entries = timesheetService.getTimesheet(username, period);

		return ResponseEntity.ok(entries);
	}

	@GetMapping("/checkLeaveBalance")
	public ResponseEntity<Boolean> checkLeaveBalance(@RequestParam String username, @RequestParam String type) {
		int year = LocalDate.now().getYear();
		AllowedLeaves leave = allowedleaverepo.findByUsernameAndYear(username, year);

		if (leave == null)
			return ResponseEntity.ok(false);

		if (type.endsWith("Sick Leave")) {
			return ResponseEntity.ok(leave.getSickTaken() < leave.getSickAllowed());
		}

		if (type.endsWith("Optional Leave")) {
			return ResponseEntity.ok(leave.getFloatingTaken() < leave.getFloatingAllowed());
		}

		if (type.endsWith("Casual Leave")) {
			return ResponseEntity.ok(leave.getCasualTaken() < leave.getCasualAllowed());
		}

		return ResponseEntity.ok(true);
	}

	@PostMapping("/saveTimesheet")
	public ResponseEntity<String> saveTimesheet(@RequestBody List<TimesheetEntry> newEntries) {
		if (newEntries.isEmpty()) {
			return ResponseEntity.badRequest().body("No timesheet data provided");
		}

		String username = newEntries.get(0).getUsername();
		String period = newEntries.get(0).getPeriod();

		String[] split = period.split(" - ");
		String startDate = split[0];
		int currentYear = Integer.parseInt(startDate.split("/")[2]);
		int currentMonth = Integer.parseInt(startDate.split("/")[1]);

		AllowedLeaves leave = allowedleaverepo.findByUsernameAndYear(username, currentYear);
		if (leave == null) {
			return ResponseEntity.status(400).body("Leave record not found for user.");
		}

		List<TimesheetEntry> existingEntriesThisMonth = timesheetRepository.findByUsername(username).stream()
				.filter(e -> {
					if (e.getPeriod() == null || !e.getPeriod().contains(" - "))
						return false;
					String[] periodSplit = e.getPeriod().split(" - ");
					String[] dateParts = periodSplit[0].split("/");
					int month = Integer.parseInt(dateParts[1]);
					int year = Integer.parseInt(dateParts[2]);
					return month == currentMonth && year == currentYear;
				}).collect(Collectors.toList());

		long existingCLCount = existingEntriesThisMonth.stream()
				.filter(e -> e.getChargeCode() != null && e.getChargeCode().endsWith("Casual Leave")).count();

		long newCLCount = newEntries.stream()
				.filter(e -> e.getChargeCode() != null && e.getChargeCode().endsWith("Casual Leave")).count();

		int earnedCL = leave.getEarncasualLeave();
		int totalCLAllowed = earnedCL + 1;

		if (existingCLCount + newCLCount > totalCLAllowed) {
			return ResponseEntity.badRequest()
					.body("Failed to Save timesheet! ⚠ You entered more Casual Leaves than allowed (" + totalCLAllowed
							+ "). Please reduce CL entries.");
		}

		timesheetService.saveOrUpdateTimesheet(newEntries);

		List<TimesheetEntry> allUserEntries = timesheetRepository.findByUsername(username);

		double totalSL = 0;
		double totalFL = 0;

		for (TimesheetEntry entry : allUserEntries) {
			String entryPeriod = entry.getPeriod();
			if (entryPeriod == null || !entryPeriod.contains(" - "))
				continue;

			int entryYear = Integer.parseInt(entryPeriod.split(" - ")[0].split("/")[2]);
			if (entryYear != currentYear)
				continue;

			String code = entry.getChargeCode();
			if (code == null || entry.getHours() == null)
				continue;

			double hours = 0;
			try {
				hours = Double.parseDouble(entry.getHours());
			} catch (NumberFormatException e) {
				continue; // Skip invalid hours
			}

			if (code.endsWith("Sick Leave")) {
				if (hours >= 4.5 && hours < 9) {
					totalSL += 0.5;
				} else if (hours >= 9) {
					totalSL += 1;
				}
				// Less than 4.5 is not counted as leave
			} else if (code.endsWith("Optional Leave")) totalFL++;
		}

		if (totalSL > leave.getSickAllowed()) {
			return ResponseEntity.badRequest().body(
					"⚠ You are exceeding your Sick Leave limit. Please choose another leave or mark as Loss of Pay.");
		}
		if (totalFL > leave.getFloatingAllowed()) {
			return ResponseEntity.badRequest().body(
					"⚠ You are exceeding your Floating Leave limit. Please choose another leave or mark as Loss of Pay.");
		}

		leave.setSickTaken(totalSL);
		leave.setFloatingTaken(totalFL);
		allowedleaverepo.save(leave);

		// 🔁 Handle Casual Leave tracking
		List<TimesheetEntry> entriesForMonth = allUserEntries.stream().filter(e -> {
			if (e.getPeriod() == null || !e.getPeriod().contains(" - "))
				return false;
			String[] splitPeriod = e.getPeriod().split(" - ");
			String[] dateParts = splitPeriod[0].split("/");
			int month = Integer.parseInt(dateParts[1]);
			int year = Integer.parseInt(dateParts[2]);
			return month == currentMonth && year == currentYear;
		}).collect(Collectors.toList());

		Set<String> submittedPeriods = entriesForMonth.stream().map(TimesheetEntry::getPeriod)
				.collect(Collectors.toSet());

		if (submittedPeriods.size() >= 2) {
			CasualLeaveTracker tracker = casualLeaveTrackerRepo.findByUsernameAndYearAndMonth(username, currentYear,
					currentMonth);

			if (tracker != null) {
				long clDaysTaken = entriesForMonth.stream()
						.filter(e -> e.getChargeCode() != null && e.getChargeCode().endsWith("Casual Leave")).count();

				int currentEarnedCL = leave.getEarncasualLeave();
				int monthlyCL = 1;
				int totalAvailableCL = currentEarnedCL + monthlyCL;

				if (clDaysTaken > totalAvailableCL) {
					return ResponseEntity.badRequest().body(
							"⚠ You are exceeding your available Casual Leave limit. Please choose another leave or mark as Loss of Pay.");
				}

				boolean wasTakenBefore = tracker.isTaken();
				boolean wasCarriedForward = tracker.isClCarriedForward(); // 👈 Add this field to tracker (boolean)

				if (clDaysTaken == 0) {
					// ✅ No CL taken this month
					if (!wasCarriedForward) {
						leave.setEarncasualLeave(currentEarnedCL + 1); // Increase only once
						tracker.setClCarriedForward(true); // ✅ mark that earnedCL was increased
					}
					tracker.setTaken(false);
					if (wasTakenBefore) {
						leave.setCasualTaken(leave.getCasualTaken() - 1); // If previously marked taken, now remove
					}
				} else {
					// ✅ CL was taken this month
					int usedEarnedCL = (int) Math.min(clDaysTaken, currentEarnedCL);
					int usedMonthlyCL = (int) Math.min(monthlyCL, clDaysTaken - usedEarnedCL);
					int unusedMonthlyCL = monthlyCL - usedMonthlyCL;

					// Update earnedCL
					leave.setEarncasualLeave(currentEarnedCL - usedEarnedCL + unusedMonthlyCL);

					// 🔁 Revert if earlier carried forward but now CL is taken
					if (wasCarriedForward && tracker.isClCarriedForward()) {
						leave.setEarncasualLeave(leave.getEarncasualLeave() - 1);
						tracker.setClCarriedForward(false);
					}

					int newTotalUsed = usedEarnedCL + usedMonthlyCL;
					int currentCasualTaken = leave.getCasualTaken();

					if (!wasTakenBefore && newTotalUsed > 0) {
						leave.setCasualTaken(currentCasualTaken + newTotalUsed);
					} else if (wasTakenBefore && newTotalUsed == 0) {
						leave.setCasualTaken(currentCasualTaken - 1);
					}

					tracker.setTaken(true);
				}

				casualLeaveTrackerRepo.save(tracker);
				allowedleaverepo.save(leave);
			}
		}

		return ResponseEntity.ok("Timesheet saved successfully");
	}

	@DeleteMapping("/deleteRow")
	public ResponseEntity<?> deleteRow(@RequestParam String chargeCode, @RequestParam String period) {
		try {
			String trimmedChargeCode = chargeCode.trim();

			List<TimesheetEntry> rowsToDelete = timesheetRepository.findByChargeCodeAndPeriod(trimmedChargeCode,
					period);

			if (rowsToDelete.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Collections.singletonMap("error", "Row not found for the selected period"));
			}

			timesheetRepository.deleteAll(rowsToDelete);

			return ResponseEntity.ok(Collections.singletonMap("success", true));
		} catch (Exception e) {
			e.printStackTrace(); // 🔥 Print error in logs
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("error", "Failed to delete row"));
		}
	}

	@GetMapping("/getSummary")
	public ResponseEntity<Map<String, Object>> getSummary(@RequestParam String username, @RequestParam String period) {

		List<TimesheetEntry> entries = timesheetService.getTimesheet(username, period);

		float totalHours = 0;
		float totalAbsences = 0;
		float totalExpense = 0;
		float totallop = 0;

		Map<String, Float> chargeCodeTotals = new HashMap<>();

		float casualLeaveThisMonth = 0;
		float sickLeaveThisYear = 0;
		float paidLeave = 0;
		float floating = 0;

		for (TimesheetEntry entry : entries) {
			String code = entry.getChargeCode();
			if ("Company Code".equals(code) || "Work Location".equals(code)) {
				continue;
			}

			float hours = (entry.getHours() == null || entry.getHours().isEmpty()) ? 0
					: Float.parseFloat(entry.getHours());
			float leaveDays = hours / 9.0f;

			chargeCodeTotals.put(code, chargeCodeTotals.getOrDefault(code, 0f) + hours);
			totalHours += hours;

			if (code.endsWith("Casual Leave")) {
				casualLeaveThisMonth += leaveDays;
			} else if (code.endsWith("Sick Leave")) {
				sickLeaveThisYear += leaveDays;
			} else if (code.endsWith("Loss of Pay")) {
				paidLeave += leaveDays;
			} else if (code.endsWith("Optional Leave")) {
				floating += leaveDays;
			}

			if (code.endsWith("Leave") || code.endsWith("Loss of Pay")) {
				totalAbsences += hours;
			}
			if (code.endsWith("Loss of Pay")) {
				totallop += hours;

			}
		}

		List<EmpExpensedao> expenses = expenseRepository.findByUsernameAndPeriod(username, period);
		for (EmpExpensedao expense : expenses) {
			totalExpense += expense.getAmount();
		}

		List<Map<String, String>> processedEntries = new ArrayList<>();
		for (Map.Entry<String, Float> entry : chargeCodeTotals.entrySet()) {
			Map<String, String> row = new HashMap<>();
			row.put("chargeCode", entry.getKey());
			row.put("hours", String.valueOf(entry.getValue()));
			processedEntries.add(row);
		}

		Map<String, Object> summaryData = new HashMap<>();
		summaryData.put("totalHours", totalHours);
		summaryData.put("totalAbsences", totalAbsences);
		summaryData.put("totallop", totallop);
		summaryData.put("casualLeaveDays", casualLeaveThisMonth);
		summaryData.put("sickLeaveDays", sickLeaveThisYear);
		summaryData.put("paidLeaveDays", paidLeave);
		summaryData.put("floating", floating);
		summaryData.put("totalExpense", totalExpense);
		summaryData.put("entries", processedEntries);

		int currentYear = LocalDate.now().getYear();
		AllowedLeaves allowedLeave = allowedleaverepo.findByUsernameAndYear(username, currentYear);
		Map<String, Float> allowedLeaveMap = new HashMap<>();

		float earnedleave = allowedLeave.getEarncasualLeave();
		float sickleave = (float) (allowedLeave.getSickAllowed() - allowedLeave.getSickTaken());
		float floatingleave = (float) (allowedLeave.getFloatingAllowed() - allowedLeave.getFloatingTaken());
		float casualevae = (allowedLeave.getCasualAllowed() - allowedLeave.getCasualTaken());

		if (allowedLeave != null) {
			allowedLeaveMap.put("earnedLeave", earnedleave);
			allowedLeaveMap.put("sickLeave", sickleave);
			allowedLeaveMap.put("floatingLeave", floatingleave);
			allowedLeaveMap.put("casualLeave", casualevae);
		}

		summaryData.put("allowedLeave", allowedLeaveMap);

		return ResponseEntity.ok(summaryData);
	}

	@PostMapping("/sendForApproval")
	public ResponseEntity<Map<String, Object>> sendForApproval(@RequestBody Map<String, String> request) {
		String username = request.get("username");
		String period = request.get("period");
		String status = request.getOrDefault("status", "Pending");

		// ✅ Fetch summary data
		ResponseEntity<Map<String, Object>> responseEntity = getSummary(username, period);
		Map<String, Object> summary = responseEntity.getBody();

		if (summary == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("success", false, "message", "Failed to generate summary"));
		}

		saveSummary(username, period, summary, status);

		if (status.equals("Pending")) {
			notificationService
					.sendAdminNotification(username + " is waiting for Timesheet approval on this Period: " + period);
		} else if (status.equals("Approved")) {
			notificationService.sendNotification(username, "Your timesheet for " + period + " has been Submited.");
		}

		if (isSecondPeriod(period)) {
			String month = extractMonthFromPeriod(period);
			try {
				monthlySummaryService.generateMonthlySummary(username, month);
			} catch (Exception e) {
				System.err.println("Monthly summary skipped: " + e.getMessage());
			}
		}

		return ResponseEntity.ok(Map.of("success", true));
	}

	private boolean isSecondPeriod(String period) {
		return period.startsWith("16/");
	}

	private String extractMonthFromPeriod(String period) {
		String[] parts = period.split(" ")[0].split("/");
		return parts[2] + "-" + parts[1]; // 2025-05
	}

	public void saveSummary(String username, String period, Map<String, Object> summaryData, String status) {

		SummaryEntry existingEntry = summaryRepository.findByUsernameAndPeriod(username, period);

		if (existingEntry != null) {

			existingEntry.setSummaryData(new Gson().toJson(summaryData));
			existingEntry.setStatus(status);
			summaryRepository.save(existingEntry);
		} else {

			SummaryEntry newEntry = new SummaryEntry(username, period, summaryData, status);
			summaryRepository.save(newEntry);
		}
	}

	@GetMapping("/getAllSummaries")
	public ResponseEntity<List<Map<String, Object>>> getAllSummaries() {
		List<SummaryEntry> summaries = summaryRepository.findAll(); // Fetch all summaries

		List<Map<String, Object>> responseList = new ArrayList<>();

		for (SummaryEntry summary : summaries) {
			Map<String, Object> summaryData = new HashMap<>();
			summaryData.put("username", summary.getUsername());
			summaryData.put("period", summary.getPeriod());
			summaryData.put("totalHours", summary.getSummaryData().get("totalHours"));
			summaryData.put("totalAbsences", summary.getSummaryData().get("totalAbsences"));
			summaryData.put("entries", summary.getSummaryData().get("entries"));

			responseList.add(summaryData);
		}

		return ResponseEntity.ok(responseList);
	}

	@GetMapping("/getPendingApprovals")
	public ResponseEntity<List<Map<String, String>>> getPendingApprovals() {
		List<String> statuses = Arrays.asList("Pending");
		List<SummaryEntry> pendingSummaries = summaryRepository.findByStatusIn(statuses);

		List<Map<String, String>> responseList = pendingSummaries.stream().map(summary -> {
			Map<String, String> responseMap = new HashMap<>();
			responseMap.put("username", summary.getUsername());
			responseMap.put("period", summary.getPeriod());

			return responseMap;
		}).collect(Collectors.toList());

		return ResponseEntity.ok(responseList);
	}

	@GetMapping("/getApprovalslist")
	public ResponseEntity<List<Map<String, String>>> getApprovalslist() {
		List<String> statuses = Arrays.asList("Approved");
		List<SummaryEntry> pendingSummaries = summaryRepository.findByStatusIn(statuses);

		List<Map<String, String>> responseList = pendingSummaries.stream().map(summary -> {
			Map<String, String> responseMap = new HashMap<>();
			responseMap.put("username", summary.getUsername());
			responseMap.put("period", summary.getPeriod());

			return responseMap;
		}).collect(Collectors.toList());

		return ResponseEntity.ok(responseList);
	}

	@GetMapping("/getIssuelist")
	public ResponseEntity<List<Map<String, String>>> getIssuelist() {
		List<String> statuses = Arrays.asList("Issue");
		List<SummaryEntry> pendingSummaries = summaryRepository.findByStatusIn(statuses);

		List<Map<String, String>> responseList = pendingSummaries.stream().map(summary -> {
			Map<String, String> responseMap = new HashMap<>();
			responseMap.put("username", summary.getUsername());
			responseMap.put("period", summary.getPeriod());

			return responseMap;
		}).collect(Collectors.toList());

		return ResponseEntity.ok(responseList);
	}

	@GetMapping("/counts")
	public ResponseEntity<Map<String, Integer>> getCounts(@RequestParam(required = false) String employee,
			@RequestParam(required = false) String year, @RequestParam(required = false) String month) {

		List<SummaryEntry> allEntries = summaryRepository.findAll();

		// Filter by employee if given
		if (employee != null && !employee.isEmpty()) {
			allEntries = allEntries.stream().filter(entry -> entry.getUsername().equalsIgnoreCase(employee))
					.collect(Collectors.toList());
		}

		if (year != null && !year.isEmpty()) {
			allEntries = allEntries.stream().filter(entry -> {
				String period = entry.getPeriod();
				if (period != null && period.length() >= 10) {
					String yearPart = period.substring(6, 10); // example: "01/06/2025"
					return yearPart.equals(year);
				}
				return false;
			}).collect(Collectors.toList());
		}

		if (month != null && !month.isEmpty()) {
			allEntries = allEntries.stream().filter(entry -> {
				String period = entry.getPeriod();
				if (period != null && period.length() >= 5) {
					String monthPart = period.substring(3, 5); // extract MM from "dd/MM/yyyy"
					return monthPart.equals(month);
				}
				return false;
			}).collect(Collectors.toList());
		}

		// Count based on status
		int pending = (int) allEntries.stream().filter(entry -> "Pending".equalsIgnoreCase(entry.getStatus())).count();
		int approved = (int) allEntries.stream().filter(entry -> "Approved".equalsIgnoreCase(entry.getStatus()))
				.count();
		int issue = (int) allEntries.stream().filter(entry -> "Issue".equalsIgnoreCase(entry.getStatus())).count();

		Map<String, Integer> counts = new HashMap<>();
		counts.put("pending", pending);
		counts.put("approved", approved);
		counts.put("issue", issue);

		return ResponseEntity.ok(counts);
	}

	@PostMapping("/approve")
	public ResponseEntity<Map<String, String>> approveTimesheet(@RequestBody Map<String, String> request) {
		String username = request.get("username");
		String period = request.get("period");

		boolean success = timesheetService.approveTimesheet(username, period);

		if (success) {
			notificationService.sendNotification(username,
					"Your timesheet has been approved on this Period: " + period);

			String month = extractMonthFromPeriod(period);
			try {
				monthlySummaryService.generateMonthlySummary(username, month);
			} catch (Exception e) {

			}

			return ResponseEntity.ok(Collections.singletonMap("message", "Timesheet approved successfully."));
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Collections.singletonMap("message", "Timesheet entry not found."));
	}

	@GetMapping("/getApprovalStatus")
	public ResponseEntity<Map<String, String>> getApprovalStatus(@RequestParam String username,
			@RequestParam String period) {

		SummaryEntry summaryEntry = summaryRepository.findByUsernameAndPeriod(username, period);

		Map<String, String> response = new HashMap<>();
		if (summaryEntry != null) {
			response.put("status", summaryEntry.getStatus());
		} else {
			response.put("status", "Not Found");
		}

		return ResponseEntity.ok(response);
	}

	@PostMapping("/raiseIssue")
	public ResponseEntity<Map<String, String>> raiseIssue(@RequestBody Map<String, String> request) {

		String username = request.get("username");
		String period = request.get("period");
		String issueMessage = request.get("issueMessage");

		boolean success = timesheetService.raiseIssue(username, period, issueMessage);

		if (success) {
			notificationService.sendNotification(username, "Timesheet has an issue on this period: " + period
					+ "<br><strong>The Issue is: " + issueMessage + "</strong>");

			return ResponseEntity.ok(Collections.singletonMap("message", "Issue raised successfully."));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Collections.singletonMap("message", "Timesheet entry not found."));

	}

	@GetMapping("/assigned-employees")
	public List<Assignment> getAssignedEmployees() {

		return assignrepo.findAll();
	}

	@PostMapping("/savePreferences")
	public ResponseEntity<String> savePreferences(@RequestBody Preference preference) {

		Optional<Preference> existingPreference = preferenceRepository
				.findByEmployeenameAndPeriod(preference.getEmployeename(), preference.getPeriod());

		if (existingPreference.isPresent()) {

			Preference updatedPreference = existingPreference.get();
			updatedPreference.setApprovers(preference.getApprovers());
			updatedPreference.setReviewers(preference.getReviewers());
			updatedPreference.setDelegator(preference.getDelegator());

			preferenceRepository.save(updatedPreference);

		} else {

			preferenceRepository.save(preference);

		}

		return ResponseEntity.ok("Preferences saved successfully!");
	}

	@GetMapping("/getPreferences")
	public ResponseEntity<Preference> getPreferences(@RequestParam String period, @RequestParam String employeename) {
		Optional<Preference> preference = preferenceRepository.findByEmployeenameAndPeriod(employeename, period);

		if (preference.isPresent()) {
			return ResponseEntity.ok(preference.get());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/saveEmpExpense")
	public EmpExpensedao saveExpense(@RequestParam("username") String username, @RequestParam("period") String period,
			@RequestParam("expenseType") String expenseType, @RequestParam("amount") double amount,
			@RequestParam("invoice") String invoice, @RequestParam("gst") String gst,
			@RequestParam(value = "receipt", required = false) MultipartFile receipt,
			@RequestParam("description") String description) {

		EmpExpensedao expense = new EmpExpensedao();
		expense.setUsername(username);
		expense.setPeriod(period);
		expense.setExpenseType(expenseType);
		expense.setAmount(amount);
		expense.setInvoiceNumber(invoice);
		expense.setGstNumber(gst);
		expense.setDescription(description);

		if (receipt != null && !receipt.isEmpty()) {
			try {
				String fileName = System.currentTimeMillis() + "_" + receipt.getOriginalFilename();
				Path uploadPath = Paths.get(UPLOAD_DIR);

				// Ensure directory exists
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				Path filePath = uploadPath.resolve(fileName);
				Files.write(filePath, receipt.getBytes());
				expense.setReceiptPath(filePath.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return expenseRepository.save(expense);
	}

	@GetMapping("/getEmpExpenses")
	public List<EmpExpensedao> getExpenses(@RequestParam("username") String username,
			@RequestParam("period") String period) {
		return expenseRepository.findByUsernameAndPeriod(username, period);
	}

	@DeleteMapping("/deleteEmpExpense/{id}")
	@Transactional
	public ResponseEntity<String> deleteExpense(@PathVariable Long id) {
		if (expenseRepository.existsById(id)) {
			expenseRepository.deleteById(id);
			return ResponseEntity.ok("Expense deleted successfully.");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense not found.");
		}
	}

	@GetMapping("/timesheetGrid")
	public Map<String, Map<String, String>> getGrid(@RequestParam String username, @RequestParam String period) {

		List<TimesheetEntry> cells = timesheetRepository.findByUsernameAndPeriod(username, period);
		Map<String, Map<String, String>> grid = new HashMap<>();

		for (TimesheetEntry cell : cells) {
			String[] parts = cell.getCellIndex().split("_");
			String row = parts[0];
			String col = parts[1];
			String val = cell.getChargeCode() + "|" + cell.getHours();

			grid.computeIfAbsent(row, k -> new HashMap<>()).put(col, val);
		}

		return grid;
	}

}
