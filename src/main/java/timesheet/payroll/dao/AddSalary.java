package timesheet.payroll.dao;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AddSalary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@JsonProperty("E-name")
	private String employeename;

	@JsonProperty("doj")
	private String DOJ;

	@JsonProperty("Salary_M")
	private String Monthsalary;

	private String Yearsalary;
	private String Bankaccount;

	private String reason;

	@JsonProperty("effective")
	private String effectiveFrom;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmployeename() {
		return employeename;
	}

	public void setEmployeename(String employeename) {
		this.employeename = employeename;
	}

	public String getDOJ() {
		return DOJ;
	}

	public void setDOJ(String dOJ) {
		DOJ = dOJ;
	}

	public String getMonthsalary() {
		return Monthsalary;
	}

	public void setMonthsalary(String monthsalary) {
		Monthsalary = monthsalary;
	}

	public String getYearsalary() {
		return Yearsalary;
	}

	public void setYearsalary(String yearsalary) {
		Yearsalary = yearsalary;
	}

	public String getBankaccount() {
		return Bankaccount;
	}

	public void setBankaccount(String bankaccount) {
		Bankaccount = bankaccount;
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

	@Override
	public String toString() {
		return "AddSalary [id=" + id + ", employeename=" + employeename + ", DOJ=" + DOJ + ", Monthsalary="
				+ Monthsalary + ", Yearsalary=" + Yearsalary + ", Bankaccount=" + Bankaccount + ", reason=" + reason
				+ ", effectiveFrom=" + effectiveFrom + "]";
	}

}
