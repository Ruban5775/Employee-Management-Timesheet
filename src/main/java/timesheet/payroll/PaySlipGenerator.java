package timesheet.payroll;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import timesheet.payroll.dao.ApprovedPayslip;
import timesheet.payroll.repo.ApprovedPayslipRepo;

@RestController
public class PaySlipGenerator {

	@Autowired
	private ApprovedPayslipRepo approvedPayslip;

	@GetMapping("/PayslipDownload")
	public ResponseEntity<byte[]> downloadPayslip(@RequestParam String username, @RequestParam String month)
			throws IOException, DocumentException {

		username = username.trim();
		month = month.trim();

		ApprovedPayslip approvedPaysliprepo = approvedPayslip.findByUsernameAndMonth(username, month);
		if (approvedPaysliprepo == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document();
		PdfWriter.getInstance(document, baos);
		document.open();

		// Fonts
		Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
		Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
		Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
		Font smallFont = new Font(Font.FontFamily.HELVETICA, 10);

		// Logo (if needed)
		try {
			ClassPathResource imageResource = new ClassPathResource("static/img/logo.png");
			InputStream logoStream = imageResource.getInputStream();
			byte[] logoBytes = logoStream.readAllBytes();
			Image logo = Image.getInstance(logoBytes);
			logo.scaleToFit(100, 50);
			logo.setAlignment(Element.ALIGN_LEFT);
			document.add(logo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		YearMonth ym = YearMonth.parse(approvedPaysliprepo.getMonth(), DateTimeFormatter.ofPattern("yyyy-MM"));
		String formattedMonth = ym.format(DateTimeFormatter.ofPattern("yyyy - MMMM", Locale.ENGLISH)).toUpperCase();

		Paragraph monthPara = new Paragraph("Payslip For " + formattedMonth, titleFont);
		monthPara.setAlignment(Element.ALIGN_CENTER);
		document.add(monthPara);
		document.add(Chunk.NEWLINE);

		// Employee Info
		PdfPTable empTable = new PdfPTable(2);
		empTable.setWidthPercentage(100);
		empTable.setSpacingBefore(10f);
		empTable.setSpacingAfter(10f);

		empTable.addCell(createLabelCell("Name", labelFont));
		empTable.addCell(createValueCell(approvedPaysliprepo.getUsername(), normalFont));

		empTable.addCell(createLabelCell("Designation", labelFont));
		empTable.addCell(createValueCell(approvedPaysliprepo.getDesignation(), normalFont));

		empTable.addCell(createLabelCell("Bank", labelFont));
		empTable.addCell(createValueCell(approvedPaysliprepo.getBankName(), normalFont));

		empTable.addCell(createLabelCell("A/c No.", labelFont));
		empTable.addCell(createValueCell(approvedPaysliprepo.getAccountNumber(), normalFont));

		empTable.addCell(createLabelCell("DOJ", labelFont));
		empTable.addCell(createValueCell(formatDate(approvedPaysliprepo.getOnboardDate()), normalFont));

		empTable.addCell(createLabelCell("STD Days", labelFont));
		empTable.addCell(createValueCell(String.valueOf(approvedPaysliprepo.getStdWorkDays()), normalFont));

		empTable.addCell(createLabelCell("Worked Days", labelFont));
		empTable.addCell(createValueCell(String.valueOf(approvedPaysliprepo.getTotalWorkingDays()), normalFont));

		empTable.addCell(createLabelCell("Paid Leaves", labelFont));
		empTable.addCell(createValueCell(String.valueOf(approvedPaysliprepo.getTotalLeaves().intValue()), normalFont));

		empTable.addCell(createLabelCell("Loss of Pay", labelFont));
		empTable.addCell(createValueCell(String.valueOf(approvedPaysliprepo.getLop().intValue()), normalFont));

		empTable.addCell(createLabelCell("Location", labelFont));
		empTable.addCell(createValueCell(approvedPaysliprepo.getLocation(), normalFont));

		document.add(empTable);

		// --- Salary Split Table with Styled Layout ---
		PdfPTable salaryTable = new PdfPTable(4);
		salaryTable.setWidthPercentage(100);
		salaryTable.setWidths(new float[] { 3f, 2f, 3f, 2f });
		salaryTable.setSpacingBefore(10f);
		salaryTable.setSpacingAfter(10f);

		// Header Row
		salaryTable.addCell(createHeaderCell("Earnings", labelFont));
		salaryTable.addCell(createHeaderCell("Amount in ₹", labelFont));
		salaryTable.addCell(createHeaderCell("Deductions", labelFont));
		salaryTable.addCell(createHeaderCell("Amount in ₹", labelFont));

		// Earnings and Deductions Row
		salaryTable.addCell(createCell("Basic Salary", normalFont, Element.ALIGN_LEFT));
		salaryTable.addCell(createCell(String.format("%,.2f", approvedPaysliprepo.getBasicSalary()), normalFont,
				Element.ALIGN_RIGHT));
		salaryTable.addCell(createCell("Deduction", normalFont, Element.ALIGN_LEFT));
		salaryTable.addCell(createCell(String.format("%,.2f", approvedPaysliprepo.getDeductions()), normalFont,
				Element.ALIGN_RIGHT));

		// Gross Earnings & Deductions Row
		salaryTable.addCell(createCell("Gross Earnings", labelFont, Element.ALIGN_LEFT));
		salaryTable.addCell(createCell(String.format("%,.2f", approvedPaysliprepo.getBasicSalary()), labelFont,
				Element.ALIGN_RIGHT));
		salaryTable.addCell(createCell("Gross Deductions", labelFont, Element.ALIGN_LEFT));
		salaryTable.addCell(createCell(String.format("%,.2f", approvedPaysliprepo.getDeductions()), labelFont,
				Element.ALIGN_RIGHT));

		// Net Pay row (boxed row)
		PdfPCell netPayLabel = new PdfPCell(new Phrase("NET PAY", labelFont));
		netPayLabel.setColspan(3);
		netPayLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
		netPayLabel.setPadding(8);
		netPayLabel.setBorderWidth(1);
		salaryTable.addCell(netPayLabel);

		PdfPCell netPayValue = new PdfPCell(
				new Phrase(String.format("%,.2f", approvedPaysliprepo.getNetPay()), labelFont));
		netPayValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
		netPayValue.setPadding(8);
		netPayValue.setBorderWidth(1);
		salaryTable.addCell(netPayValue);

		document.add(salaryTable);

		document.add(Chunk.NEWLINE);

		// Footer
		String approvedAtFormatted = formatDateTime(approvedPaysliprepo.getApprovedAt());
		String salaryProcessedFormatted = formatDateTime(approvedPaysliprepo.getSalaryProcessAt());

		Paragraph footer = new Paragraph(
				"Payslip Approved at: " + approvedAtFormatted + " | Salary processed at: " + salaryProcessedFormatted,
				smallFont);
		footer.setAlignment(Element.ALIGN_CENTER);
		document.add(footer);

		document.add(Chunk.NEWLINE);

		Paragraph note = new Paragraph(
				"** This is a computer generated payslip and does not require signature and stamp.**", smallFont);
		note.setAlignment(Element.ALIGN_CENTER);
		document.add(note);

		document.close();

		byte[] pdfBytes = baos.toByteArray();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", "Payslip-" + username + "-" + month + ".pdf");

		return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
	}

	// --- Helper Methods ---

	private PdfPCell createLabelCell(String text, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setPadding(5);
		return cell;
	}

	private PdfPCell createValueCell(String text, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setPadding(5);
		return cell;
	}

	private PdfPCell createHeaderCell(String text, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
		cell.setPadding(5);
		return cell;
	}

	private PdfPCell createCell(String text, Font font, int alignment) {
		PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
		cell.setHorizontalAlignment(alignment);
		cell.setPadding(5);
		return cell;
	}

	private String formatDate(String rawDate) {
		try {
			if (rawDate == null || rawDate.trim().equals("-"))
				return "-";

			DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(rawDate, inputFormat);

			DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
			return date.format(outputFormat);
		} catch (Exception e) {
			return rawDate; // fallback
		}
	}

	private String formatDateTime(String rawDateTime) {
		try {
			if (rawDateTime == null || rawDateTime.trim().equals("-"))
				return "-";

			// Replace the colon after the date part only if present
			rawDateTime = rawDateTime.replaceFirst("(\\d{4}-\\d{2}-\\d{2}):", "$1");

			DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			LocalDateTime dateTime = LocalDateTime.parse(rawDateTime.trim(), inputFormat);

			DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd-MMM-yyyy - hh:mm a", Locale.ENGLISH);
			return dateTime.format(outputFormat);
		} catch (Exception e) {
			e.printStackTrace(); // optional for debugging
			return rawDateTime; // fallback
		}
	}

}
