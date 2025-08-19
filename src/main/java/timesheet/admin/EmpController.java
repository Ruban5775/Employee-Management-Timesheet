package timesheet.admin;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.mail.MessagingException;
import timesheet.admin.dao.AllowedLeaves;
import timesheet.admin.dao.CasualLeaveTracker;
import timesheet.admin.dao.Delegatedao;
import timesheet.admin.dao.Employeedao;
import timesheet.admin.repo.AllowedLeavesRepository;
import timesheet.admin.repo.CasualLeaveTrackerRepo;
import timesheet.admin.repo.DelegateRepo;
import timesheet.admin.repo.EmployeeRepo;
import timesheet.emails.EmailServiceController;

@org.springframework.stereotype.Controller
public class EmpController {

	@Autowired
	private EmployeeRepo EmpRepo;

	@Autowired
	private DelegateRepo Delrepo;

	@Autowired
	private AllowedLeavesRepository allowedLeaveRepo;

	@Autowired
	private EmailServiceController emailservice;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private CasualLeaveTrackerRepo casualLeaveTrackerRepo;

	@GetMapping("/Admin_Dashboard")
	public String M1() {
		return "Admin/Admin_panel";
	}

	@GetMapping("/Employee_Dashboard")
	public String Employee() {
		return "Employee/Employee";
	}

	@PostMapping("/addEmployee")
	public ResponseEntity<String> addEmployee(@RequestBody Employeedao EmpData) throws IOException {

		if (EmpRepo.existsByeMail(EmpData.geteMail())) {
			return ResponseEntity.status(409).body("Employee with this email already exists.");
		}

		EmpRepo.save(EmpData);

		try {
			emailservice.sendEmployeeEmail(EmpData);
		} catch (MessagingException e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Failed to send email.");
		}

		String username = EmpData.geteName();
		LocalDate onboardDate = LocalDate.parse(EmpData.getOnboard());
		int onboardYear = onboardDate.getYear();
		int onboardMonth = onboardDate.getMonthValue();

		int fyStartYear = (onboardMonth >= 4) ? onboardYear : onboardYear - 1;
		int fyEndYear = fyStartYear + 1;

	
		LocalDate today = LocalDate.now();
		int currentMonth = today.getMonthValue();
		int currentYear = today.getYear();
		int currentFyStart = (currentMonth >= 4) ? currentYear : currentYear - 1;
		int currentFyEnd = currentFyStart + 1;

		if (!allowedLeaveRepo.existsByUsernameAndYear(username, fyStartYear)) {
			allowedLeaveRepo.save(new AllowedLeaves(username, fyStartYear));
		}

		// ➕ Set initial casual leave counts for onboard FY
		int monthsPassed;

		if (onboardYear == fyStartYear && onboardMonth >= 4) {
		   
		    monthsPassed = onboardMonth - 4;          
		} else if (onboardYear < fyStartYear) {
		    
		    monthsPassed = 0;
		} else {
		   
		    monthsPassed = 9 + onboardMonth;          
		}

		/* ✨ NEW RULE: if the employee joined after the 15th of the joining month,
		   treat that month’s CL as already consumed. */
		if (onboardDate.getDayOfMonth() > 15) {
		    monthsPassed += 1;                        
		}
	
		monthsPassed = Math.min(Math.max(monthsPassed, 0), 12);
		
		AllowedLeaves allowed = allowedLeaveRepo.findByUsernameAndYear(username, fyStartYear);
		if (allowed != null) {
		    allowed.setBaseCasualTaken(monthsPassed);
		    allowed.setCasualTaken(monthsPassed);
		    allowedLeaveRepo.save(allowed);
		}
		
		for (int m = 4; m <= 12; m++) {
			if (!casualLeaveTrackerRepo.existsByUsernameAndYearAndMonth(username, fyStartYear, m)) {
				casualLeaveTrackerRepo.save(new CasualLeaveTracker(username, fyStartYear, m));
			}
		}
		for (int m = 1; m <= 3; m++) {
			if (!casualLeaveTrackerRepo.existsByUsernameAndYearAndMonth(username, fyEndYear, m)) {
				casualLeaveTrackerRepo.save(new CasualLeaveTracker(username, fyEndYear, m));
			}
		}

		if (fyStartYear < currentFyStart) {

			if (!allowedLeaveRepo.existsByUsernameAndYear(username, currentFyStart)) {
				allowedLeaveRepo.save(new AllowedLeaves(username, currentFyStart));
			}

			for (int m = 4; m <= 12; m++) {
				if (!casualLeaveTrackerRepo.existsByUsernameAndYearAndMonth(username, currentFyStart, m)) {
					casualLeaveTrackerRepo.save(new CasualLeaveTracker(username, currentFyStart, m));
				}
			}

			for (int m = 1; m <= 3; m++) {
				if (!casualLeaveTrackerRepo.existsByUsernameAndYearAndMonth(username, currentFyEnd, m)) {
					casualLeaveTrackerRepo.save(new CasualLeaveTracker(username, currentFyEnd, m));
				}
			}
		}

		return ResponseEntity.ok("Employee Onboarded successfully..!");
	}

	@GetMapping("/getEmployeeById/{id}")
	public ResponseEntity<?> getEmployeeByid(@PathVariable int id) {
		Optional<Employeedao> optionalCode = EmpRepo.findById(id);
		if (optionalCode.isPresent()) {
			return ResponseEntity.ok(optionalCode.get());
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Code not found");
		}
	}

	@PostMapping("/updateEmployee")
	public ResponseEntity<String> updateEmployee(@RequestBody Map<String, String> requestData) throws IOException {

	    int id = Integer.parseInt(requestData.get("id"));
	    Optional<Employeedao> optionalEmp = EmpRepo.findById(id);
	    if (!optionalEmp.isPresent()) {
	        return ResponseEntity.status(404).body("Employee not found!");
	    }
	    Employeedao emp = optionalEmp.get();

	    String newEmail = requestData.get("E-mail");
	    Employeedao existingWithEmail = EmpRepo.findByeMail(newEmail);
	    if (existingWithEmail != null && existingWithEmail.getId() != emp.getId()) {
	        return ResponseEntity.status(409).body("Another employee with this email already exists.");
	    }

	    String oldName = emp.geteName();
	    String newName = requestData.get("E-name");

	    emp.seteName(newName);
	    emp.seteMail(newEmail);
	    emp.setDesignation(requestData.get("E-desg"));
	    emp.setOnboard(requestData.get("onborad"));
	    emp.setE_Role(requestData.get("E-role"));
	    emp.setAdditionalRole("Admin".equalsIgnoreCase(emp.getE_Role()) ? "Employee" : "-");

	    EmpRepo.save(emp);           

	    LocalDate onboardDate = LocalDate.parse(emp.getOnboard());   
	    int onboardYear  = onboardDate.getYear();
	    int onboardMonth = onboardDate.getMonthValue();             
	   
	    int fyStartYear  = (onboardMonth >= 4) ? onboardYear : onboardYear - 1;

	    int monthsPassed;
	    if (onboardYear == fyStartYear && onboardMonth >= 4) {
	        monthsPassed = onboardMonth - 4;          
	    } else if (onboardYear < fyStartYear) {
	        monthsPassed = 0;                         
	    } else {
	        monthsPassed = 9 + onboardMonth;          
	    }

	   
	    if (onboardDate.getDayOfMonth() > 15) {
	        monthsPassed += 1;
	    }
	    monthsPassed = Math.min(Math.max(monthsPassed, 0), 12);      
    
	    AllowedLeaves onboardingFY =
	            allowedLeaveRepo.findByUsernameAndYear(newName, fyStartYear);
	    if (onboardingFY == null) {
	        onboardingFY = new AllowedLeaves(newName, fyStartYear);
	    }
	    onboardingFY.setBaseCasualTaken(monthsPassed);
	    onboardingFY.setCasualTaken(monthsPassed);
	    allowedLeaveRepo.save(onboardingFY);
	  
	    int year = LocalDate.now().getYear();

	    AllowedLeaves leaves = allowedLeaveRepo.findByUsernameAndYear(oldName, year);
	    if (leaves != null) {
	        leaves.setUsername(newName);
	        allowedLeaveRepo.save(leaves);
	    }

	    List<CasualLeaveTracker> trackers =
	            casualLeaveTrackerRepo.findByUsernameAndYear(oldName, year);
	    for (CasualLeaveTracker tracker : trackers) {
	        tracker.setUsername(newName);
	    }
	    casualLeaveTrackerRepo.saveAll(trackers);
	   
	    try {
	        emailservice.updateemployeeemail(emp);
	    } catch (MessagingException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(500).body("Failed to send email.");
	    }

	    return ResponseEntity.ok("Employee updated successfully!");
	}


//    @PostMapping("/updateEmployee")
//    public ResponseEntity<String> updateEmployee(@RequestBody Map<String, String> requestData) throws UnsupportedEncodingException {
//        int employeeId = Integer.parseInt(requestData.get("id"));
//        Optional<Employeedao> optionalEmp = EmpRepo.findById(employeeId);
//
//        if (!optionalEmp.isPresent()) {
//            return ResponseEntity.status(404).body("Employee not found");
//        }
//
//        Employeedao employee = optionalEmp.get();
//        long oldSalary = employee.getSalary();
//        long newSalary = Long.parseLong(requestData.get("salary"));
//
//        boolean salaryChanged = oldSalary != newSalary;
//        boolean roleChanged = !employee.getE_Role().equals(requestData.get("role"));
//
//        // Update salary and role
//        employee.setSalary(newSalary);
//        employee.setE_Role(requestData.get("role"));
//        EmpRepo.save(employee);
//
//        // Send email notifications
//        try {
//            if (salaryChanged) {
//                double percentageHike = ((double) (newSalary - oldSalary) / oldSalary) * 100;
//                if (!roleChanged) {
//                    sendAppraisalEmail(employee, percentageHike);
//                } else {
//                    sendPromotionEmail(employee, percentageHike);
//                }
//            }
//        } catch (MessagingException e) {
//            return ResponseEntity.status(500).body("Update successful, but email failed to send.");
//        }
//
//        return ResponseEntity.ok("Employee updated successfully!");
//    }
//
//    // Email notification for salary appraisal with percentage hike
//    private void sendAppraisalEmail(Employeedao employee, double percentageHike) throws MessagingException, UnsupportedEncodingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//        helper.setFrom("timex@tidyds.com", "Tidy Digital Solutions");
//        helper.setTo(employee.geteMail());
//        helper.setSubject("🎉 Salary Appraisal Notification!");
//
//        String emailContent = "<html><body>"
//                + "<h2>Congratulations, " + employee.geteName() + "!</h2>"
//                + "<p>We are pleased to inform you that, based on your hard work and dedication, your salary has been increased by <b>" 
//                + String.format("%.2f", percentageHike) + "%</b>.</p>"
//                + "<p><b>New Salary:</b> ₹" + employee.getSalary() + "/- per month</p>"
//                + "<p>This increment reflects your valuable contributions to Tidy Digital Solutions. We appreciate your efforts and look forward to your continued excellence.</p>"
//                + "<br>"
//                + "<p>If you have any questions regarding this update, feel free to reach out to the Admin Department.</p>"
//                + "<br>"
//                  + "<img src='cid:logoImage' width='200' alt='Company logo' />"
//                + "<p>Best Regards,<br><b>Tidy Digital Solutions Team</b></p>"
//                + "</body></html>";
//
//        helper.setText(emailContent, true);
//
//        ClassPathResource image = new ClassPathResource("static/img/logo.png");
//        helper.addInline("logoImage", image);
//
//        mailSender.send(message);
//    }
//
//    // Email notification for promotion + salary increase with percentage hike
//    private void sendPromotionEmail(Employeedao employee, double percentageHike) throws MessagingException, UnsupportedEncodingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//        helper.setFrom("timex@tidyds.com", "Tidy Digital Solutions");
//        helper.setTo(employee.geteMail());
//        helper.setSubject("🚀 Promotion & Salary Increase Notification!");
//
//        String emailContent = "<html><body>"
//                + "<h2>Congratulations on Your Promotion, " + employee.geteName() + "!</h2>"
//                + "<p>We are pleased to inform you that you have been promoted to the position of <b>" + employee.getE_Role() + "</b>.</p>"
//                + "<p>As part of this promotion, your salary has been increased by <b>" + String.format("%.2f", percentageHike) + "%</b>.</p>"
//                + "<p><b>New Salary:</b> ₹" + employee.getSalary() + "/- per month</p>"
//                + "<br>"
//                + "<p>We appreciate your valuable contributions to Tidy Digital Solutions and look forward to your continued excellence in this new role.</p>"
//                + "<br>"
//                + "<p>If you have any questions or require further details, please feel free to reach out to the Admin Department.</p>"
//                + "<br>"
//                 + "<img src='cid:logoImage' width='200' alt='Company logo' />"
//                + "<p>Best Regards,<br><b>Tidy Digital Solutions Team</b></p>"
//                + "</body></html>";
//
//        helper.setText(emailContent, true);
//
//        ClassPathResource image = new ClassPathResource("static/img/logo.png");
//        helper.addInline("logoImage", image);
//        mailSender.send(message);
//    }

	@GetMapping("/getEmployees")
	public ResponseEntity<List<Employeedao>> getEmployees() {
		List<Employeedao> employees = EmpRepo.findAll();
		return ResponseEntity.ok(employees);
	}

	@PutMapping("/updateEmployeeStatus/{id}")
	public ResponseEntity<String> updateEmployeeStatus(@PathVariable int id) {
		Optional<Employeedao> employeeOptional = EmpRepo.findById(id);
		if (employeeOptional.isPresent()) {
			Employeedao employee = employeeOptional.get();
			if ("active".equalsIgnoreCase(employee.getStatus())) {
				employee.setStatus("deactive");
			} else {
				employee.setStatus("active");
			}
			EmpRepo.save(employee);
			return ResponseEntity.ok("Employee status updated successfully!");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found.");
		}
	}

	@GetMapping("/getEmployeedata")
	public ResponseEntity<List<Map<String, String>>> getEmployeeData() {

		List<Employeedao> employees = EmpRepo.findBystatus("active");
		List<Map<String, String>> employeeList = new ArrayList<>();

		for (Employeedao emp : employees) {
			Map<String, String> map = new HashMap<>();
			map.put("name", emp.geteName());
			map.put("email", emp.geteMail());
			map.put("designation", emp.getDesignation());
			employeeList.add(map);
		}

		return ResponseEntity.ok(employeeList);
	}

	@PostMapping("/addDelegate")
	public ResponseEntity<String> addDelegator(@RequestBody Delegatedao Dele) {
		Delrepo.save(Dele);
		return ResponseEntity.ok("Delegator Data Saved Successfully");
	}

	@GetMapping("/getDelegator")
	public ResponseEntity<List<Delegatedao>> getDelegators() {
		List<Delegatedao> Delegator = Delrepo.findAll();
		return ResponseEntity.ok(Delegator);
	}

}
