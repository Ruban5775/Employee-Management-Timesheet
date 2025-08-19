package timesheet.admin.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timesheet.admin.dao.Codedao;

@Repository
public interface CodeRepo extends JpaRepository<Codedao, Integer> {

}
