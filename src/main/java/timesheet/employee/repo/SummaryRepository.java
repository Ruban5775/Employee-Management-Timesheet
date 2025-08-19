package timesheet.employee.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timesheet.employee.dao.SummaryEntry;

@Repository
public interface SummaryRepository extends JpaRepository<SummaryEntry, Long> {
	SummaryEntry findByUsernameAndPeriod(String username, String period);

	List<SummaryEntry> findByStatusIn(List<String> statuses);

	Integer countByStatus(String string);

	List<SummaryEntry> findByUsername(String username);

	int countByStatusAndUsername(String status, String username);

	@Query("SELECT COUNT(s) FROM SummaryEntry s WHERE s.status = :status "
			+ "AND (:employee IS NULL OR s.username = :employee) "
			+ "AND (:periodFilter IS NULL OR s.period LIKE CONCAT('%/', :month, '/', :year, '%'))")
	int countFiltered(@Param("status") String status, @Param("employee") String employee, @Param("year") String year,
			@Param("month") String month, @Param("periodFilter") String periodFilter // optional, for safe fallback
	);

}
