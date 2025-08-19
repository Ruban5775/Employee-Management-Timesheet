package timesheet.admin.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;
import timesheet.admin.dao.ChargeCodeCounter;

public interface ChargeCountRepo extends JpaRepository<ChargeCodeCounter, Integer> {

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO charge_code_counter (id, last_increment) VALUES (1, 0) ON DUPLICATE KEY UPDATE id = id", nativeQuery = true)
	void initializeCounter();
}
