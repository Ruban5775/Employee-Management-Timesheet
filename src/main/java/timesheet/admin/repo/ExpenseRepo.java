package timesheet.admin.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import timesheet.admin.dao.Expensedao;

public interface ExpenseRepo extends JpaRepository<Expensedao, Integer> {

}
