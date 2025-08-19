package timesheet.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import timesheet.admin.dao.Employeedao;

@Service
public class EmployeeService {

	@Autowired
	private LoginRepository LoginRepository;

	public Employeedao authenticate(String email, String password) {

		Employeedao employee = LoginRepository.findByeMail(email);

		if (employee != null && employee.getePassword().equals(password)) {
			return employee;
		}
		return null;
	}
}
