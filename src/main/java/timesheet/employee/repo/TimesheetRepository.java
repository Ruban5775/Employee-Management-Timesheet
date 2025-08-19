package timesheet.employee.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timesheet.employee.dao.TimesheetEntry;

@Repository
public interface TimesheetRepository extends JpaRepository<TimesheetEntry, Long> {

	List<TimesheetEntry> findByUsernameAndPeriod(String username, String period);

	Optional<TimesheetEntry> findByUsernameAndPeriodAndCellIndex(String username, String period, String cellIndex);

	List<TimesheetEntry> findByChargeCodeAndPeriod(String chargeCode, String period);

	void deleteByChargeCode(String chargeCode);

	List<TimesheetEntry> findByUsername(String username);

}
