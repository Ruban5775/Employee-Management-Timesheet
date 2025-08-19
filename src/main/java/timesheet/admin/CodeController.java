package timesheet.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import timesheet.admin.dao.Codedao;
import timesheet.admin.dao.Expensedao;
import timesheet.admin.dao.Holidays;
import timesheet.admin.repo.AssignmentRepository;
import timesheet.admin.repo.CodeRepo;
import timesheet.admin.repo.ExpenseRepo;
import timesheet.admin.repo.HolidayRepo;
import timesheet.admin.service.ChargeCodeService;

@Component
@RestController
public class CodeController {

	@Autowired
	private CodeRepo codeRepository;

	@Autowired
	private ExpenseRepo Exrepo;

	@Autowired
	private ChargeCodeService chargeCodeService;

	@Autowired
	private AssignmentRepository assignmentRepository;

	@Autowired
	private HolidayRepo holidayrepo;

	@PostMapping("/addChargeCode")
	public String addChargeCode(@RequestBody Map<String, String> requestData) {

		String codeType = requestData.getOrDefault("codeType", "-");
		String code = requestData.getOrDefault("code", "-");
		String clientName = requestData.getOrDefault("clientName", "-");
		String projectType = requestData.getOrDefault("projectType", "-");
		String startDate = requestData.getOrDefault("startDate", "-");
		String country = requestData.getOrDefault("country", "-");
		String description = requestData.getOrDefault("description", "-");

		Codedao newCode;

		if ("Leave code".equalsIgnoreCase(codeType)) {
			newCode = new Codedao(codeType, code, description);
		} else {
			newCode = new Codedao(codeType, code, clientName, projectType, startDate, country, description);
		}

		codeRepository.save(newCode);

		return "Charge Code added successfully!";
	}

	@GetMapping("/getChargecodes")
	public ResponseEntity<List<Codedao>> getCodes() {
		List<Codedao> Code = codeRepository.findAll();
		return ResponseEntity.ok(Code);
	}

	@PutMapping("/completeChargeCode/{id}")
	@Transactional
	public ResponseEntity<Map<String, Object>> completeChargeCode(@PathVariable int id) {
		Optional<Codedao> chargeCodeOpt = codeRepository.findById(id);

		if (chargeCodeOpt.isPresent()) {
			Codedao chargeCode = chargeCodeOpt.get();

			if (!"Complete".equalsIgnoreCase(chargeCode.getStatus())) {

				assignmentRepository.deleteByChargeCode(chargeCode.getCode());

				chargeCode.setStatus("Complete");
				codeRepository.save(chargeCode);

				Map<String, Object> response = new HashMap<>();
				response.put("success", true);
				return ResponseEntity.ok(response);
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("success", false, "message", "Charge Code is already marked as Complete!"));
			}
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("success", false, "message", "Charge Code not found!"));
	}

	@GetMapping("/getChargecodeById/{id}")
	public ResponseEntity<?> getChargeCodeById(@PathVariable int id) {

		Optional<Codedao> optionalCode = codeRepository.findById(id);

		if (optionalCode.isPresent()) {
			return ResponseEntity.ok(optionalCode.get());
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Code not found");
		}
	}

	@PostMapping("/updateChargeCode")
	public String updateChargeCode(@RequestBody Map<String, String> requestData) {

		int id = Integer.parseInt(requestData.get("id"));
		Optional<Codedao> optionalCode = codeRepository.findById(id);

		if (optionalCode.isPresent()) {
			Codedao code = optionalCode.get();
			code.setCodeType(requestData.get("codeType"));
			code.setCode(requestData.get("code"));
			code.setClientName(requestData.get("clientName"));
			code.setProjectType(requestData.get("projectType"));
			code.setStartDate(requestData.get("startDate"));
			code.setCountry(requestData.get("country"));
			code.setDescription(requestData.get("description"));

			codeRepository.save(code);
			return "Charge Code updated successfully!";
		} else {
			return "Charge Code not found!";
		}
	}

	@DeleteMapping("/deleteChargeCode/{id}")
	public String deleteChargeCode(@PathVariable("id") int id) {
		Optional<Codedao> optionalCode = codeRepository.findById(id);

		if (optionalCode.isPresent()) {
			codeRepository.deleteById(id);
			return "Charge Code deleted successfully!";
		} else {
			return "Charge Code not found!";
		}
	}

	@PostMapping("/addExpenseCode")
	public String addExpense(@RequestBody Expensedao Expense) {
		Exrepo.save(Expense);
		return "Expese Data Saved Successfully";
	}

	@GetMapping("/getExpenseById/{id}")
	public ResponseEntity<?> getExpenseByid(@PathVariable int id) {

		Optional<Expensedao> optionalCode = Exrepo.findById(id);

		if (optionalCode.isPresent()) {
			return ResponseEntity.ok(optionalCode.get());
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Code not found");
		}
	}

	@PostMapping("/updateExpense")
	public String updateExpense(@RequestBody Map<String, String> requestData) {

		int id = Integer.parseInt(requestData.get("id"));
		Optional<Expensedao> optionalCode = Exrepo.findById(id);

		if (optionalCode.isPresent()) {
			Expensedao exp = optionalCode.get();
			exp.setEx_code(requestData.get("Ex-code"));
			exp.setEx_type(requestData.get("Ex-type"));

			Exrepo.save(exp);
			return "Expense Code updated successfully!";
		} else {
			return "Expense Code not found!";
		}
	}

	@DeleteMapping("/deleteExpense/{id}")
	public String deleteExpense(@PathVariable("id") int id) {

		Optional<Expensedao> optionalCode = Exrepo.findById(id);

		if (optionalCode.isPresent()) {
			Exrepo.deleteById(id);
			return "Charge Code deleted successfully!";
		} else {
			return "Charge Code not found!";
		}
	}

	@GetMapping("/getExpensecode")
	public ResponseEntity<List<Expensedao>> getExpense() {
		List<Expensedao> ExCode = Exrepo.findAll();

		return ResponseEntity.ok(ExCode);
	}

	@GetMapping("/getNextCodeIncrement")
	public int getNextCodeIncrement() {
		return chargeCodeService.getNextCodeIncrement();
	}

	@PostMapping("/addHoliday")
	public String addHoliday(@RequestBody Holidays holidays) {
		try {

			String inputDate = holidays.getDate(); // example: 2025-04-01

			LocalDate localDate = LocalDate.parse(inputDate); // Parse yyyy-MM-dd
			String formattedDate = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			int year = localDate.getYear();

			holidays.setDate(formattedDate);
			holidays.setYear(year);

			holidayrepo.save(holidays);

			return "Holiday Data Saved Successfully";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error saving holiday";
		}
	}

	@GetMapping("/getHolidays")
	public ResponseEntity<List<Holidays>> getHolidy() {
		List<Holidays> holiday = holidayrepo.findAll();

		return ResponseEntity.ok(holiday);
	}

	@GetMapping("/getholidaybyid/{id}")
	public ResponseEntity<?> getholidaybyid(@PathVariable int id) {

		Optional<Holidays> optionalCode = holidayrepo.findById(id);

		if (optionalCode.isPresent()) {
			return ResponseEntity.ok(optionalCode.get());
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Code not found");
		}
	}

	@PostMapping("/updateHoliday")
	public String updateHoliday(@RequestBody Map<String, String> requestData) {
		System.out.println(requestData);

		int id = Integer.parseInt(requestData.get("id"));
		Optional<Holidays> optionalCode = holidayrepo.findById(id);

		if (optionalCode.isPresent()) {
			Holidays exp = optionalCode.get();
			exp.setName(requestData.get("holidayname"));

			String inputDate = requestData.get("holidaydate"); // example: 2025-04-01

			LocalDate localDate = LocalDate.parse(inputDate);
			String formattedDate = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			int year = localDate.getYear();

			exp.setDate(formattedDate);
			exp.setYear(year);

			holidayrepo.save(exp);
			return "Holiday updated successfully!";
		} else {
			return "Holiday not found!";
		}
	}

	@GetMapping("/holidaysforUI")
	public List<Holidays> getAllHolidays() {
		return holidayrepo.findAll();
	}

	@DeleteMapping("/deleteholiday/{id}")
	public String deelteholiday(@PathVariable("id") int id) {

		Optional<Holidays> optionalCode = holidayrepo.findById(id);

		if (optionalCode.isPresent()) {
			holidayrepo.deleteById(id);
			return "holiday deleted successfully!";
		} else {
			return "holiday not found!";
		}
	}
}
