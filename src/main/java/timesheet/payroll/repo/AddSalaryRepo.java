package timesheet.payroll.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timesheet.payroll.dao.AddSalary;

@Repository
public interface AddSalaryRepo extends JpaRepository<AddSalary, Integer> {

	List<AddSalary> findByEmployeename(String employeename);

}
