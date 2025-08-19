package timesheet.admin.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import timesheet.admin.dao.Employeedao;
import timesheet.admin.repo.EmployeeRepo;

@Service
public class AssigmentEmailService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private EmployeeRepo erepo;

	public void sendAssignmentEmail(String employeeName, String chargeCode, String description)
			throws MessagingException, UnsupportedEncodingException {

		String recipientEmail = getEmailForEmployee(employeeName);

		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		String emailContent = "<html><body>" + "<h2>Hello " + employeeName + ",</h2>"
				+ "<p>We hope this message finds you well. We are writing to inform you that you have been assigned to a new charge code for your upcoming tasks. Please find the details below:</p>"
				+ "<table border='0' cellpadding='10' cellspacing='0' style='font-family: Arial, sans-serif; width: 100%; background-color: #f9f9f9;'>"
				+ "<tr>" + "<td><b>Charge Code:</b></td>" + "<td>" + chargeCode + "</td>" + "</tr>" + "<tr>"
				+ "<td><b>Description:</b></td>" + "<td>" + description + "</td>" + "</tr>" + "</table>"
				+ "<p>Kindly ensure you are familiar with the scope and requirements associated with this charge code. Should you have any questions or require clarification, do not hesitate to reach out to the management team.</p>"
				+ "<p>For your reference, please see the company logo below:</p>"
				+ "<img src='cid:logoImage' width='200' alt='Company logo' />" + "<p>Best regards,<br>"
				+ "<strong>Tidy Digital Solutions</strong><br>"
				+ "<i>Your trusted partner in digital transformation</i></p>"
				+ "<footer style='font-size: 0.9em; color: #777;'>"
				+ "<p>If you have received this email in error or have any questions, please contact us at <a href='mailto:info@tidydigital.com'>info@tidydigital.com</a>.</p>"
				+ "</footer>" + "</body></html>";

		helper.setText(emailContent, true);

		helper.setFrom("timex@tidyds.com", "Tidy Digital Solutions");
		helper.setTo(recipientEmail);
		helper.setSubject("Tidyds - Charge code Assignment");

		ClassPathResource image = new ClassPathResource("static/img/logo.png");
		helper.addInline("logoImage", image);

		javaMailSender.send(message);
	}

	public String getEmailForEmployee(String employeeName) {

		Employeedao employee = erepo.findByeName(employeeName);

		if (employee != null) {

			return employee.geteMail();

		} else

			return "No email found for employee " + employeeName;
	}
}
