package timesheet.employee.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import timesheet.employee.dao.Preference;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {
	Optional<Preference> findByEmployeenameAndPeriod(String employeename, String period); // ✅ Fix uniqueness
}
