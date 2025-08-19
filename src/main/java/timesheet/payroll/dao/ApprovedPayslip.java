package timesheet.payroll.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ApprovedPayslip {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;
	private String month;
	private String onboardDate;
	private String designation;

	private Integer stdWorkDays;
	private Integer totalLeaves;
	private Integer totalWorkingDays;
	private Double lop;
	private Double basicSalary;
	private Double deductions;
	private Double netPay;

	private String accountHolder;
	private String bankName;
	private String accountNumber;
	private String location;

	private String SalaryProcessAt;
	private String ApprovedAt;

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

	public String getOnboardDate() {
		return onboardDate;
	}

	public void setOnboardDate(String onboardDate) {
		this.onboardDate = onboardDate;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public Integer getStdWorkDays() {
		return stdWorkDays;
	}

	public void setStdWorkDays(Integer stdWorkDays) {
		this.stdWorkDays = stdWorkDays;
	}

	public Integer getTotalLeaves() {
		return totalLeaves;
	}

	public void setTotalLeaves(Integer totalLeaves) {
		this.totalLeaves = totalLeaves;
	}

	public Integer getTotalWorkingDays() {
		return totalWorkingDays;
	}

	public void setTotalWorkingDays(Integer totalWorkingDays) {
		this.totalWorkingDays = totalWorkingDays;
	}

	public Double getLop() {
		return lop;
	}

	public void setLop(Double lop) {
		this.lop = lop;
	}

	public Double getBasicSalary() {
		return basicSalary;
	}

	public void setBasicSalary(Double basicSalary) {
		this.basicSalary = basicSalary;
	}

	public Double getDeductions() {
		return deductions;
	}

	public void setDeductions(Double deductions) {
		this.deductions = deductions;
	}

	public Double getNetPay() {
		return netPay;
	}

	public void setNetPay(Double netPay) {
		this.netPay = netPay;
	}

	public String getAccountHolder() {
		return accountHolder;
	}

	public void setAccountHolder(String accountHolder) {
		this.accountHolder = accountHolder;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSalaryProcessAt() {
		return SalaryProcessAt;
	}

	public void setSalaryProcessAt(String salaryProcessAt) {
		SalaryProcessAt = salaryProcessAt;
	}

	public String getApprovedAt() {
		return ApprovedAt;
	}

	public void setApprovedAt(String approvedAt) {
		ApprovedAt = approvedAt;
	}

	@Override
	public String toString() {
		return "ApprovedPayslip [id=" + id + ", username=" + username + ", month=" + month + ", onboardDate="
				+ onboardDate + ", designation=" + designation + ", stdWorkDays=" + stdWorkDays + ", totalLeaves="
				+ totalLeaves + ", totalWorkingDays=" + totalWorkingDays + ", lop=" + lop + ", basicSalary="
				+ basicSalary + ", deductions=" + deductions + ", netPay=" + netPay + ", accountHolder=" + accountHolder
				+ ", bankName=" + bankName + ", accountNumber=" + accountNumber + ", location=" + location
				+ ", SalaryProcessAt=" + SalaryProcessAt + ", ApprovedAt=" + ApprovedAt + "]";
	}

}