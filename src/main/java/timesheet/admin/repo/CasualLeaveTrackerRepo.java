package timesheet.admin.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import timesheet.admin.dao.CasualLeaveTracker;

public interface CasualLeaveTrackerRepo extends JpaRepository<CasualLeaveTracker, Long> {
	boolean existsByUsernameAndYearAndMonth(String username, int year, int month);

	CasualLeaveTracker findByUsernameAndYearAndMonth(String username, int currentYear, int currentMonth);

	List<CasualLeaveTracker> findByUsernameAndYear(String oldName, int year);
}
