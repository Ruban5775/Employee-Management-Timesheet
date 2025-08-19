package timesheet.employee.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "timesheet_entries")
public class TimesheetEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;

	private String period; // Example: "01/03/2024 - 15/03/2024"

	private String chargeCode;

	private String cellIndex;

	private String hours;

	public TimesheetEntry() {
	}

	public TimesheetEntry(String username, String period, String chargeCode, String cellIndex, String hours) {
		this.username = username;
		this.period = period;
		this.chargeCode = chargeCode;
		this.cellIndex = cellIndex;
		this.hours = hours;
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

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getChargeCode() {
		return chargeCode;
	}

	public void setChargeCode(String chargeCode) {
		this.chargeCode = chargeCode;
	}

	public String getCellIndex() {
		return cellIndex;
	}

	public void setCellIndex(String cellIndex) {
		this.cellIndex = cellIndex;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	@Override
	public String toString() {
		return "TimesheetEntry [id=" + id + ", username=" + username + ", period=" + period + ", chargeCode="
				+ chargeCode + ", cellIndex=" + cellIndex + ", hours=" + hours + "]";
	}

}
