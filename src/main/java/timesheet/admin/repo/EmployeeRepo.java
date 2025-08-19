package timesheet.admin.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import timesheet.admin.dao.Employeedao;

public interface EmployeeRepo extends JpaRepository<Employeedao, Integer> {

	Employeedao findByeName(String name);

	Employeedao findByeMail(String email);

	List<Employeedao> findBystatus(String status);

	@Query("SELECT e FROM Employeedao e WHERE e.status = 'active' AND e.eName NOT IN (SELECT s.employeename FROM AddSalary s)")
	List<Employeedao> findActiveEmployeesWithoutSalary();

	@Query("SELECT e.eName FROM Employeedao e WHERE e.status = 'active'")
	List<String> findAllUsernames();

	boolean existsByeMail(String geteMail);

}
