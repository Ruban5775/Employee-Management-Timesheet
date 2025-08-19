package timesheet.payroll.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import timesheet.payroll.dao.SalaryHistory;

public interface SalaryHistoryRepo extends JpaRepository<SalaryHistory, Long> {

	List<SalaryHistory> findByEmployeeNameOrderByEffectiveFromDesc(String employeeName);

}
