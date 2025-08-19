package timesheet.payroll.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "salary_history")
public class SalaryHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Store employee name directly
	@Column(name = "employee_name", nullable = false)
	private String employeeName;

	private Double oldsalary;

	@Column(name = "salary", nullable = false)
	private Double newsalary;

	@Column(name = "hike_percent")
	private Double hikePercent;

	@Column(name = "reason")
	private String reason;

	@Column(name = "effective_from", nullable = false)
	private String effectiveFrom;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public SalaryHistory() {
	}

	public SalaryHistory(String employeeName, Double oldsalary, Double salary, Double hikePercent, String reason,
			String effectiveFrom) {
		this.employeeName = employeeName;
		this.oldsalary = oldsalary;
		this.newsalary = salary;
		this.hikePercent = hikePercent;
		this.reason = reason;
		this.effectiveFrom = effectiveFrom;
		this.updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public Double getHikePercent() {
		return hikePercent;
	}

	public void setHikePercent(Double hikePercent) {
		this.hikePercent = hikePercent;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getEffectiveFrom() {
		return effectiveFrom;
	}

	public void setEffectiveFrom(String effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Double getOldsalary() {
		return oldsalary;
	}

	public void setOldsalary(Double oldsalary) {
		this.oldsalary = oldsalary;
	}

	public Double getNewsalary() {
		return newsalary;
	}

	public void setNewsalary(Double newsalary) {
		this.newsalary = newsalary;
	}

	@Override
	public String toString() {
		return "SalaryHistory [id=" + id + ", employeeName=" + employeeName + ", oldsalary=" + oldsalary
				+ ", newsalary=" + newsalary + ", hikePercent=" + hikePercent + ", reason=" + reason
				+ ", effectiveFrom=" + effectiveFrom + ", updatedAt=" + updatedAt + "]";
	}

}
