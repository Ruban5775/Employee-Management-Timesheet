package timesheet.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import timesheet.admin.dao.Employeedao;

@RestController
@RequestMapping("/login")
public class LoginController {

	@Autowired  
	private EmployeeService employeeService;

	@PostMapping
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
		Employeedao employee = employeeService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

		if (employee != null) {

			return ResponseEntity.ok(new LoginResponse(true, employee.getE_Role(), employee.getStatus(),
					employee.geteName(), employee.getAdditionalRole(), employee.getOnboard()));
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new LoginResponse(false, null, null, null, null, null));
		}
	}
}