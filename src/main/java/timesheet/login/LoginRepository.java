package timesheet.login;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timesheet.admin.dao.Employeedao;

@Repository
public interface LoginRepository extends JpaRepository<Employeedao, Integer> {

	Employeedao findByeMail(String eMail);
}
