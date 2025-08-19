package timesheet.admin.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import timesheet.admin.dao.Assignment;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

	@Modifying
	@Transactional
	@Query("DELETE FROM Assignment a WHERE a.chargeCode = :chargeCode")
	void deleteByChargeCode(@Param("chargeCode") String chargeCode);
}
