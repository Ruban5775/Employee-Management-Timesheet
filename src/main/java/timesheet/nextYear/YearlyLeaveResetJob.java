package timesheet.nextYear;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import timesheet.admin.dao.AllowedLeaves;
import timesheet.admin.dao.CasualLeaveTracker;
import timesheet.admin.repo.AllowedLeavesRepository;
import timesheet.admin.repo.CasualLeaveTrackerRepo;
import timesheet.admin.repo.EmployeeRepo;

@Component
public class YearlyLeaveResetJob {

	private final AllowedLeavesRepository allowedLeaveRepo;
	private final CasualLeaveTrackerRepo casualLeaveTrackerRepo;
	private final EmployeeRepo employeeRepo;

	public YearlyLeaveResetJob(AllowedLeavesRepository allowedLeaveRepo, CasualLeaveTrackerRepo casualLeaveTrackerRepo,
			EmployeeRepo employeeRepo) {
		this.allowedLeaveRepo = allowedLeaveRepo;
		this.casualLeaveTrackerRepo = casualLeaveTrackerRepo;
		this.employeeRepo = employeeRepo;
	}

	// 🔄 Run this job every year on April 1st at 00:05 AM
	@Scheduled(cron = "0 5 0 1 4 *")
	public void createLeaveRecordsForNewFinancialYear() {
		int fyStartYear = LocalDate.now().getYear(); // Example: 2025
		int fyEndYear = fyStartYear + 1; // FY 2025 → 2026

		List<String> allUsernames = employeeRepo.findAllUsernames();

		for (String username : allUsernames) {

			// ✅ Create AllowedLeaves for new FY if not exists
			if (!allowedLeaveRepo.existsByUsernameAndYear(username, fyStartYear)) {
				AllowedLeaves allowedLeaves = new AllowedLeaves(username, fyStartYear);
				allowedLeaves.setBaseCasualTaken(0);
				allowedLeaves.setCasualTaken(0);
				allowedLeaves.setSickTaken(0);
				allowedLeaves.setFloatingTaken(0);
				allowedLeaveRepo.save(allowedLeaves);
			}

			// 📅 Create CasualLeaveTracker for April (4) to December (12) of FY start year
			for (int month = 4; month <= 12; month++) {
				if (!casualLeaveTrackerRepo.existsByUsernameAndYearAndMonth(username, fyStartYear, month)) {
					CasualLeaveTracker tracker = new CasualLeaveTracker(username, fyStartYear, month);
					casualLeaveTrackerRepo.save(tracker);
				}
			}

			// 📅 Create CasualLeaveTracker for January (1) to March (3) of next calendar
			// year (FY end)
			for (int month = 1; month <= 3; month++) {
				if (!casualLeaveTrackerRepo.existsByUsernameAndYearAndMonth(username, fyEndYear, month)) {
					CasualLeaveTracker tracker = new CasualLeaveTracker(username, fyEndYear, month);
					casualLeaveTrackerRepo.save(tracker);
				}
			}
		}

		System.out.println("📆 Leave records for FY " + fyStartYear + "–" + fyEndYear + " created successfully.");
	}
}
