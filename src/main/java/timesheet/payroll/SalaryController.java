package timesheet.payroll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import timesheet.admin.dao.Employeedao;
import timesheet.admin.dao.Expensedao;
import timesheet.admin.repo.EmployeeRepo;
import timesheet.emails.EmailServiceController;
import timesheet.payroll.dao.AddSalary;
import timesheet.payroll.dao.Bankdetails;
import timesheet.payroll.dao.SalaryEditDto;
import timesheet.payroll.dao.SalaryHistory;
import timesheet.payroll.dao.SalaryHistoryDTO;
import timesheet.payroll.repo.AddSalaryRepo;
import timesheet.payroll.repo.BankDetailsRepo;
import timesheet.payroll.repo.SalaryHistoryRepo;

@RestController
public class SalaryController {

	@Autowired
	private EmployeeRepo EmpRepo;

	@Autowired
	private AddSalaryRepo addSalaryRepo;

	@Autowired
	private BankDetailsRepo bankDetailsrepo;

	@Autowired
	private EmailServiceController emailservice;

	@Autowired
	private SalaryHistoryRepo salaryHistoryRepo;

	@GetMapping("/getEmployeesforSalary")
	public List<Employeedao> getSalaryEligibleEmployees() {
		List<String> salariedEmployeeNames = addSalaryRepo.findAll().stream().map(AddSalary::getEmployeename)
				.collect(Collectors.toList());

		return EmpRepo.findAll().stream().filter(e -> "active".equalsIgnoreCase(e.getStatus()))
				.filter(e -> salariedEmployeeNames.stream().noneMatch(name -> name.equalsIgnoreCase(e.geteName())))
				.collect(Collectors.toList());
	}

	@PostMapping("/addSalary")
	public ResponseEntity<String> addSalary(@RequestBody AddSalary salaryData) {
		try {
			int salaryMonthInt = Integer.parseInt(salaryData.getMonthsalary());
			int salaryYear = salaryMonthInt * 12;

			salaryData.setYearsalary(String.valueOf(salaryYear));
			salaryData.setBankaccount("0");

			// Set default values
			salaryData.setReason("Onboarding");

			Employeedao empData = EmpRepo.findByeName(salaryData.getEmployeename());

			// Save in AddSalary table
			addSalaryRepo.save(salaryData);

			double oldsalary = 0;
			// Save in SalaryHistory table
			SalaryHistory history = new SalaryHistory(salaryData.getEmployeename(), oldsalary,
					Double.valueOf(salaryData.getMonthsalary()), null, "Onboarding", salaryData.getEffectiveFrom());
			salaryHistoryRepo.save(history);

			try {
				emailservice.InitialSalaryEmail(salaryData, empData);
			} catch (MessagingException e) {
				e.printStackTrace();
				return ResponseEntity.status(500).body("Failed to send email.");
			}

			return ResponseEntity.ok("Salary added successfully for " + salaryData.getEmployeename() + "!");
		} catch (NumberFormatException e) {
			return ResponseEntity.badRequest().body("Invalid SalaryMonth: must be a number.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Failed to add salary due to server error.");
		}
	}

	@GetMapping("/getSalaryById/{id}")
	public ResponseEntity<?> getExpenseByid(@PathVariable int id) {

		Optional<AddSalary> optionalCode = addSalaryRepo.findById(id);

		if (optionalCode.isPresent()) {
			return ResponseEntity.ok(optionalCode.get());
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Code not found");
		}
	}

	@GetMapping("/getInitialSalary")
	public ResponseEntity<List<AddSalary>> getCodes() {
		List<AddSalary> Salary = addSalaryRepo.findAll();
		return ResponseEntity.ok(Salary);
	}

	@PostMapping("/updateSalaryWithHike")
	public ResponseEntity<String> updateSalaryWithHike(@RequestBody Map<String, Object> hikeData) {
		try {
			int id = Integer.valueOf(hikeData.get("id").toString());
			String name = hikeData.get("name").toString();
			double currentSalary = Double.parseDouble(hikeData.get("currentSalary").toString());
			double newSalary = Double.parseDouble(hikeData.get("newSalary").toString());
			double hikePercent = Double.parseDouble(hikeData.get("hikePercent").toString());
			String reason = hikeData.get("reason").toString();
			String effectiveFrom = hikeData.get("effectiveFrom").toString();

			// Fetch AddSalary record
			AddSalary salary = addSalaryRepo.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));

			double oldsalary = Double.parseDouble(salary.getMonthsalary());

			// Update AddSalary table
			salary.setMonthsalary(String.valueOf((int) newSalary));
			salary.setYearsalary(String.valueOf((int) newSalary * 12));
			salary.setReason(reason);
			salary.setEffectiveFrom(effectiveFrom);
			addSalaryRepo.save(salary);

			// Save to SalaryHistory table
			SalaryHistory history = new SalaryHistory(name, oldsalary, newSalary, hikePercent, reason, effectiveFrom);
			salaryHistoryRepo.save(history);

			Employeedao empData = EmpRepo.findByeName(name);
			emailservice.SalaryHikeEmail(empData, oldsalary, newSalary, hikePercent, reason);

			return ResponseEntity.ok("Salary hike applied successfully for " + salary.getEmployeename() + "!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Failed to apply salary hike.");
		}
	}

//
	@GetMapping("/getSalaryHistory")
	public ResponseEntity<List<SalaryHistoryDTO>> getSalaryHistory(@RequestParam String employeeName) {
		List<SalaryHistory> historyList = salaryHistoryRepo.findByEmployeeNameOrderByEffectiveFromDesc(employeeName);

		List<SalaryHistoryDTO> dtoList = historyList.stream().map(h -> new SalaryHistoryDTO(h.getEffectiveFrom(),
				h.getOldsalary(), h.getNewsalary(), h.getHikePercent(), h.getReason())).collect(Collectors.toList());

		return ResponseEntity.ok(dtoList);
	}

	@PostMapping("/updateSalaryByEdit")
	public ResponseEntity<String> updateSalaryByEdit(@RequestBody SalaryEditDto dto)
			throws MessagingException, IOException {
		Optional<AddSalary> optionalSalary = addSalaryRepo.findById(dto.getId());

		if (optionalSalary.isPresent()) {
			AddSalary record = optionalSalary.get();

			try {
				int monthly = Integer.parseInt(dto.getUpdatedSalary());
				int yearly = monthly * 12;

				record.setMonthsalary(String.valueOf(monthly));
				record.setYearsalary(String.valueOf(yearly));
				record.setReason(dto.getReason());
				record.setEffectiveFrom(dto.getEffectiveFrom()); // ✅ update date

				addSalaryRepo.save(record);

				double oldsalary = 0;
				SalaryHistory history = new SalaryHistory(dto.getName(), oldsalary,
						Double.valueOf(dto.getUpdatedSalary()), null, dto.getReason(), dto.getEffectiveFrom() // ✅ set
																												// effective
																												// date
																												// in
																												// history
				);
				salaryHistoryRepo.save(history);

				Employeedao empData = EmpRepo.findByeName(dto.getName());
				emailservice.EditedSalary(empData, monthly, yearly);

				return ResponseEntity.ok("Salary updated successfully for " + dto.getName() + "!");
			} catch (NumberFormatException e) {
				return ResponseEntity.badRequest().body("Invalid salary amount.");
			}
		} else {
			return ResponseEntity.status(404).body("Salary record not found for ID: " + dto.getId());
		}
	}

	@PostMapping("/saveBankDetails")
	public ResponseEntity<?> saveBankDetails(@RequestParam("Employeename") String employeename,
			@RequestParam("accountHolder") String accountHolder, @RequestParam("accountNumber") String accountNumber,
			@RequestParam("ifsc") String ifsc, @RequestParam("bankName") String bankName,
			@RequestParam(value = "bankBookPhoto", required = false) MultipartFile bankBookPhoto) {
		try {
			Optional<Bankdetails> existingDetailsOpt = bankDetailsrepo.findTopByEmployeenameOrderByIdDesc(employeename);

			Bankdetails bankDetails = existingDetailsOpt.orElseGet(() -> {
				Bankdetails newDetails = new Bankdetails();
				newDetails.setEmployeename(employeename);
				return newDetails;
			});

			// Set basic details
			bankDetails.setAccountHolder(accountHolder);
			bankDetails.setAccountNumber(accountNumber);
			bankDetails.setIfsc(ifsc);
			bankDetails.setBankName(bankName);

			// Handle bank book photo upload
			if (bankBookPhoto != null && !bankBookPhoto.isEmpty()) {
				String originalFilename = bankBookPhoto.getOriginalFilename();
				String extension = Optional.ofNullable(originalFilename).filter(f -> f.contains("."))
						.map(f -> f.substring(originalFilename.lastIndexOf("."))).orElse("");

				String newFileName = employeename + "_bankbook_" + System.currentTimeMillis() + extension;
				String uploadDir = "uploads/bank/";

				File uploadPath = new File(uploadDir);
				if (!uploadPath.exists())
					uploadPath.mkdirs();

				Path filePath = Paths.get(uploadDir, newFileName);
				Files.copy(bankBookPhoto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

				// Save path to DB
				bankDetails.setBankBookPhotoPath(filePath.toString());
			}

			bankDetailsrepo.save(bankDetails);

			// Update salary status
			List<AddSalary> salaryRecords = addSalaryRepo.findByEmployeename(employeename);
			for (AddSalary record : salaryRecords) {
				record.setBankaccount("1");
			}
			addSalaryRepo.saveAll(salaryRecords);

			return ResponseEntity.ok(Map.of("message", "Bank details saved/updated and salary status updated!"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("message", "Error saving bank details: " + e.getMessage()));
		}
	}

	@GetMapping("/getBankDetails")
	public ResponseEntity<?> getBankDetails(@RequestParam("Employeename") String employeename) {

		try {
			Optional<Bankdetails> bankDetails = bankDetailsrepo.findTopByEmployeenameOrderByIdDesc(employeename);
			if (bankDetails.isPresent()) {
				return ResponseEntity.ok(bankDetails.get());
			} else {
				return ResponseEntity.status(404).body(Map.of("message", "Bank details not found"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("message", "Error fetching bank details: " + e.getMessage()));
		}
	}

}
