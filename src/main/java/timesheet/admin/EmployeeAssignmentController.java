package timesheet.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import timesheet.admin.dao.Assignment;
import timesheet.admin.dao.AssignmentRequest;
import timesheet.admin.repo.AssignmentRepository;
import timesheet.admin.service.AssigmentEmailService;

@RestController
@RequestMapping("/assignEmployees")
public class EmployeeAssignmentController {

	@Autowired
	private AssigmentEmailService emailService;

	@Autowired
	private AssignmentRepository assignmentRepository;

	@PostMapping
	public ResponseEntity<Map<String, Object>> assignEmployees(@RequestBody AssignmentRequest assignmentRequest) {
		try {

			for (String employeeName : assignmentRequest.getEmployees()) {
				Assignment assignment = new Assignment();
				assignment.setEmployeeName(employeeName);
				assignment.setChargeCode(assignmentRequest.getChargeCode());
				assignment.setDescription(assignmentRequest.getDescription());
				assignmentRepository.save(assignment);
			}

			for (String employeeName : assignmentRequest.getEmployees()) {

				emailService.sendAssignmentEmail(employeeName, assignmentRequest.getChargeCode(),
						assignmentRequest.getDescription());
			}

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Error processing request: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

}
