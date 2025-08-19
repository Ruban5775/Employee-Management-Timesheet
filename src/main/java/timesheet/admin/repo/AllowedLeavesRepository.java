package timesheet.admin.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import timesheet.admin.dao.AllowedLeaves;

public interface AllowedLeavesRepository extends JpaRepository<AllowedLeaves, Integer> {

	boolean existsByUsernameAndYear(String username, int year);

	AllowedLeaves findByUsernameAndYear(String username, int year);

}
