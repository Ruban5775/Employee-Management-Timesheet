package timesheet.employee.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "empexpense_table")
public class EmpExpensedao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@JsonProperty("username")
	private String username;

	@JsonProperty("period")
	private String period;

	@JsonProperty("expenseType")
	private String expenseType;

	@JsonProperty("amount")
	private double amount;

	@JsonProperty("invoice")
	private String invoiceNumber;

	@JsonProperty("gst")
	private String gstNumber;

	@JsonProperty("receipt")
	private String receiptPath;

	@JsonProperty("description")
	private String description;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getExpenseType() {
		return expenseType;
	}

	public void setExpenseType(String expenseType) {
		this.expenseType = expenseType;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getGstNumber() {
		return gstNumber;
	}

	public void setGstNumber(String gstNumber) {
		this.gstNumber = gstNumber;
	}

	public String getReceiptPath() {
		return receiptPath;
	}

	public void setReceiptPath(String receiptPath) {
		this.receiptPath = receiptPath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "EmpExpensedao [id=" + id + ", username=" + username + ", period=" + period + ", expenseType="
				+ expenseType + ", amount=" + amount + ", invoiceNumber=" + invoiceNumber + ", gstNumber=" + gstNumber
				+ ", receiptPath=" + receiptPath + ", description=" + description + "]";
	}

}