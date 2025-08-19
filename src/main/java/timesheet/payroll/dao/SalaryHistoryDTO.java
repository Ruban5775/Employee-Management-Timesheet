package timesheet.payroll.dao;

import java.time.LocalDate;

public class SalaryHistoryDTO {
	private String effectiveFrom;
	private Double oldsalary;
	private Double newsalary;
	private Double hikePercent;
	private String reason;

	public SalaryHistoryDTO(String effectiveFrom, Double oldsalary, Double newsalary, Double hikePercent,
			String reason) {
		this.effectiveFrom = effectiveFrom;
		this.oldsalary = oldsalary;
		this.newsalary = newsalary;
		this.hikePercent = hikePercent;
		this.reason = reason;
	}

	// Getters
	public String getEffectiveFrom() {
		return effectiveFrom;
	}

	public Double getOldsalary() {
		return oldsalary;
	}

	public Double getNewsalary() {
		return newsalary;
	}

	public Double getHikePercent() {
		return hikePercent;
	}

	public String getReason() {
		return reason;
	}
}
