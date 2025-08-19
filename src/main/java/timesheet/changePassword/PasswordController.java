package timesheet.changePassword;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import timesheet.admin.dao.Employeedao;
import timesheet.admin.repo.EmployeeRepo;

@RestController
public class PasswordController {

	@Autowired
	private EmailService emailService;

	@Autowired
	private EmployeeRepo emrepo;

	@PostMapping("/send-otp")
	public ResponseEntity<OtpResponse> sendOtp(@RequestBody EmailRequest emailRequest)
			throws UnsupportedEncodingException {

		String email = emailRequest.getEmail();

		String otp = emailService.generateOtp();

		emailService.sendOtp(email, otp);

		return ResponseEntity.ok(new OtpResponse(true, "OTP sent successfully to your email"));
	}

	@PostMapping("/change-password")
	public ResponseEntity<OtpResponse> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest)
			throws UnsupportedEncodingException {
		String email = passwordChangeRequest.getEmail();

		Employeedao employee = emrepo.findByeName(email);
		String mail = employee.geteMail();

		String enteredOtp = passwordChangeRequest.getOtp();

		boolean otpValid = emailService.validateOtp(mail, enteredOtp);

		if (!otpValid) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new OtpResponse(false, "Invalid or expired OTP"));
		}

		String newPassword = passwordChangeRequest.getNewPassword();
		boolean passwordUpdated = updatePasswordInDatabase(email, newPassword);

		if (!passwordUpdated) {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new OtpResponse(false, "Failed to update password"));
		}

		emailService.sendPasswordChangedConfirmation(email);

		return ResponseEntity.ok(new OtpResponse(true, "Password changed successfully"));
	}

	private boolean updatePasswordInDatabase(String email, String newPassword) {
		Employeedao employee = emrepo.findByeName(email);

		if (employee != null) {

			employee.setePassword(newPassword);
			emrepo.save(employee);

			return true;
		}

		return false;
	}

}
