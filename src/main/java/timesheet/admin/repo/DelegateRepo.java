package timesheet.admin.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import timesheet.admin.dao.Delegatedao;

public interface DelegateRepo extends JpaRepository<Delegatedao, Integer> {

	Delegatedao findBydEmail(String email);
}
