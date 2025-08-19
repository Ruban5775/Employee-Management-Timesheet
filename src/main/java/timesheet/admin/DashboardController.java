package timesheet.admin;

import java.util.HashMap;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import timesheet.admin.dao.Employeedao;
import timesheet.admin.repo.CodeRepo;
import timesheet.admin.repo.EmployeeRepo;
import timesheet.admin.repo.ExpenseRepo;

@RestController
public class DashboardController {

	@Autowired
	private EmployeeRepo EmpRepo;

//    @Autowired
//    private DelegateRepo Delrepo;

	@Autowired
	private CodeRepo codeRepository;

	@Autowired
	private ExpenseRepo Exrepo;

	@GetMapping("/login")
	public ResponseEntity<Resource> Loginmethod() {
		Resource resource = (Resource) new ClassPathResource("static/index.html");
		try {
			return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(resource);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/getEmployeesCount")
	public Long getEmployeescount() {

		return EmpRepo.count();
	}

//	    @GetMapping("/getDelegatorsCount")
//	    public Long getDelegatorscount() {
//	        return Delrepo.count();
//	    }

	@GetMapping("/getChargecodesCount")
	public Long getChargecodescount() {
		return codeRepository.count();
	}

	@GetMapping("/getExpensecodesCount")
	public Long getExpensecodescount() {
		return Exrepo.count();
	}

	@PostMapping("/check-employee-role")
	public ResponseEntity<Map<String, Object>> checkEmployeeRole(@RequestBody Map<String, String> request) {
		String email = request.get("email");

		Employeedao employee = EmpRepo.findByeMail(email);

		// Prepare the response
		Map<String, Object> response = new HashMap<>();
		if (employee != null && "Employee".equals(employee.getAdditionalRole())) {
			response.put("success", true);
			response.put("eName", employee.geteName());
			response.put("additionalRole", employee.getAdditionalRole());
		} else {
			response.put("success", false);
		}

		return ResponseEntity.ok(response);
	}

	/* Employee Switcing */

	@PostMapping("/check-admin-role")
	public ResponseEntity<Map<String, Object>> checkAdminRole(@RequestBody Map<String, String> request) {
		String email = request.get("email");

		Employeedao employee = EmpRepo.findByeMail(email);

//	        Delegatedao delegator = Delrepo.findBydEmail(email); 

		Map<String, Object> response = new HashMap<>();

		// Case 1: If the employee is an Admin
		if (employee != null && "Admin".equals(employee.getE_Role())) {
			response.put("success", true);
			response.put("eName", employee.geteName()); // Admin name
			response.put("role", "Admin"); // Admin role
		}
//	        // Case 2: If the user's email matches a delegator's email (check if they are authorized to switch to Admin)
//	        else if (delegator != null && email.equals(delegator.getdEmail())) {
//	            response.put("success", true);
//	            response.put("eName", delegator.getdName()); // Delegator name
//	            response.put("role", "Admin"); // Admin role
//	        }
		else {
			// If neither case is true, return failure
			response.put("success", false);
		}

		return ResponseEntity.ok(response);
	}

}
