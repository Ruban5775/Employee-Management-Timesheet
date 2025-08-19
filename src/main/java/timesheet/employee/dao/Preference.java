package timesheet.employee.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "preferences")
public class Preference {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonProperty("Employeename")
	@Column(nullable = false)
	private String employeename;

	@Column(nullable = false)
	private String period;

	@Column(columnDefinition = "TEXT")
	private String approvers;

	@Column(columnDefinition = "TEXT")
	private String reviewers;

	@Column(columnDefinition = "TEXT")
	private String delegator;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmployeename() {
		return employeename;
	}

	public void setEmployeename(String employeename) {
		this.employeename = employeename;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getApprovers() {
		return approvers;
	}

	public void setApprovers(String approvers) {
		this.approvers = approvers;
	}

	public String getReviewers() {
		return reviewers;
	}

	public void setReviewers(String reviewers) {
		this.reviewers = reviewers;
	}

	public String getDelegator() {
		return delegator;
	}

	public void setDelegator(String delegator) {
		this.delegator = delegator;
	}

	@Override
	public String toString() {
		return "Preference [id=" + id + ", employeename=" + employeename + ", period=" + period + ", approvers="
				+ approvers + ", reviewers=" + reviewers + ", delegator=" + delegator + "]";
	}

}
