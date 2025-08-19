package timesheet.payroll.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timesheet.payroll.dao.Bankdetails;

@Repository
public interface BankDetailsRepo extends JpaRepository<Bankdetails, Long> {

	Optional<Bankdetails> findTopByEmployeenameOrderByIdDesc(String employeename);

	Bankdetails findByEmployeenameIgnoreCase(String username);

}
