package timesheet.payroll.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "monthly_summary", uniqueConstraints = @UniqueConstraint(columnNames = { "username", "month" }))
public class MonthlySummary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;

	private String month; // Format: YYYY-MM

	private Double totalWorkingDays;

	private Double totalLOPDays;

	private Double sickLeaveDays;

	private Double casualLeaveDays;

	private Double totalAbsences;

	private Boolean isPayslipGenerated = false;

	private LocalDateTime salaryProcessedAt;

	private String approvedBy;

	private LocalDate createdAt = LocalDate.now();

	private LocalDate updatedAt = LocalDate.now();

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDate.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public Double getTotalWorkingDays() {
		return totalWorkingDays;
	}

	public void setTotalWorkingDays(Double totalWorkingDays) {
		this.totalWorkingDays = totalWorkingDays;
	}

	public Double getTotalLOPDays() {
		return totalLOPDays;
	}

	public void setTotalLOPDays(Double totalLOPDays) {
		this.totalLOPDays = totalLOPDays;
	}

	public Double getSickLeaveDays() {
		return sickLeaveDays;
	}

	public void setSickLeaveDays(Double sickLeaveDays) {
		this.sickLeaveDays = sickLeaveDays;
	}

	public Double getCasualLeaveDays() {
		return casualLeaveDays;
	}

	public void setCasualLeaveDays(Double casualLeaveDays) {
		this.casualLeaveDays = casualLeaveDays;
	}

	public Boolean getIsPayslipGenerated() {
		return isPayslipGenerated;
	}

	public void setIsPayslipGenerated(Boolean isPayslipGenerated) {
		this.isPayslipGenerated = isPayslipGenerated;
	}

	public LocalDateTime getSalaryProcessedAt() {
		return salaryProcessedAt;
	}

	public void setSalaryProcessedAt(LocalDateTime salaryProcessedAt) {
		this.salaryProcessedAt = salaryProcessedAt;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDate getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDate updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Double getTotalAbsences() {
		return totalAbsences;
	}

	public void setTotalAbsences(Double totalAbsences) {
		this.totalAbsences = totalAbsences;
	}

	@Override
	public String toString() {
		return "MonthlySummary [id=" + id + ", username=" + username + ", month=" + month + ", totalWorkingDays="
				+ totalWorkingDays + ", totalLOPDays=" + totalLOPDays + ", sickLeaveDays=" + sickLeaveDays
				+ ", casualLeaveDays=" + casualLeaveDays + ", totalAbsences=" + totalAbsences + ", isPayslipGenerated="
				+ isPayslipGenerated + ", salaryProcessedAt=" + salaryProcessedAt + ", approvedBy=" + approvedBy
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}

}
