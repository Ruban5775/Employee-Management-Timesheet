package timesheet.employee.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import timesheet.employee.dao.EmpExpensedao;

public interface EmpExpenseRepository extends JpaRepository<EmpExpensedao, Integer> {
	List<EmpExpensedao> findByUsernameAndPeriod(String username, String period);

	boolean existsById(Long id);

	void deleteById(Long id);
}
