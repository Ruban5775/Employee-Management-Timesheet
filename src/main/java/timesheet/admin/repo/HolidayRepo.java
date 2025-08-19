package timesheet.admin.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import timesheet.admin.dao.Holidays;

public interface HolidayRepo extends JpaRepository<Holidays, Integer> {

	List<Holidays> findByyear(int targetYear);

}
