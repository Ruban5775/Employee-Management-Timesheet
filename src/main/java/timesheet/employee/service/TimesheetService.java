package timesheet.employee.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import timesheet.employee.dao.SummaryEntry;
import timesheet.employee.dao.TimesheetEntry;
import timesheet.employee.repo.SummaryRepository;
import timesheet.employee.repo.TimesheetRepository;

@Service
public class TimesheetService {

	@Autowired
	private TimesheetRepository timesheetRepository;

	@Autowired
	private SummaryRepository summaryRepository;

	public List<TimesheetEntry> getTimesheet(String username, String period) {
		return timesheetRepository.findByUsernameAndPeriod(username, period);
	}

	public void saveOrUpdateTimesheet(List<TimesheetEntry> timesheetEntries) {
		for (TimesheetEntry entry : timesheetEntries) {
			Optional<TimesheetEntry> existingEntry = timesheetRepository
					.findByUsernameAndPeriodAndCellIndex(entry.getUsername(), entry.getPeriod(), entry.getCellIndex());

			if (existingEntry.isPresent()) {
				TimesheetEntry updateEntry = existingEntry.get();

				if (entry.getHours() == null || entry.getHours().trim().isEmpty()) {

					timesheetRepository.delete(updateEntry);
				} else {

					updateEntry.setChargeCode(entry.getChargeCode());
					updateEntry.setHours(entry.getHours());
					timesheetRepository.save(updateEntry);
				}
			} else {
				if (entry.getHours() != null && !entry.getHours().trim().isEmpty()) {

					timesheetRepository.save(entry);
				}
			}
		}
	}

	public boolean approveTimesheet(String username, String period) {

		SummaryEntry timesheet = summaryRepository.findByUsernameAndPeriod(username, period);

		if (timesheet != null) {
			timesheet.setStatus("Approved");
			summaryRepository.save(timesheet);
			return true;
		}

		return false;
	}

	public boolean raiseIssue(String username, String period, String issueMessage) {

		SummaryEntry timesheet = summaryRepository.findByUsernameAndPeriod(username, period);

		if (timesheet != null) {

			timesheet.setStatus("Issue");
			summaryRepository.save(timesheet);

			return true;
		}
		return false;
	}
}
