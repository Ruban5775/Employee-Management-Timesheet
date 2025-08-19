package timesheet.payroll.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import timesheet.payroll.dao.MonthlySummary;

public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Long> {
	Optional<MonthlySummary> findByUsernameAndMonth(String username, String month);

	List<MonthlySummary> findByMonth(String month);

	List<MonthlySummary> findByMonthAndIsPayslipGeneratedFalse(String month);

}
