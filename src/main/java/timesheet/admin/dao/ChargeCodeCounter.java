package timesheet.admin.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "charge_code_counter")
public class ChargeCodeCounter {

	@Id
	private int id = 1;

	private int lastIncrement;

	public int getLastIncrement() {
		return lastIncrement;
	}

	public void setLastIncrement(int lastIncrement) {
		this.lastIncrement = lastIncrement;
	}
}
