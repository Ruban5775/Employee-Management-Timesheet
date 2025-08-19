package timesheet.payroll.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timesheet.payroll.dao.ApprovedPayslip;

@Repository
public interface ApprovedPayslipRepo extends JpaRepository<ApprovedPayslip, Long> {

	ApprovedPayslip findByUsernameAndMonth(String username, String month);
}
