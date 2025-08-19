package timesheet.payroll.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Bankdetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String employeename;
	private String accountHolder;
	private String accountNumber;
	private String ifsc;
	private String bankName;

	private String bankBookPhotoPath;

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

	public String getAccountHolder() {
		return accountHolder;
	}

	public void setAccountHolder(String accountHolder) {
		this.accountHolder = accountHolder;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getIfsc() {
		return ifsc;
	}

	public void setIfsc(String ifsc) {
		this.ifsc = ifsc;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankBookPhotoPath() {
		return bankBookPhotoPath;
	}

	public void setBankBookPhotoPath(String bankBookPhotoPath) {
		this.bankBookPhotoPath = bankBookPhotoPath;
	}

	@Override
	public String toString() {
		return "Bankdetails [id=" + id + ", employeename=" + employeename + ", accountHolder=" + accountHolder
				+ ", accountNumber=" + accountNumber + ", ifsc=" + ifsc + ", bankName=" + bankName
				+ ", bankBookPhotoPath=" + bankBookPhotoPath + "]";
	}

}
