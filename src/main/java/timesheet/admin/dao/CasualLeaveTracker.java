package timesheet.admin.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CasualLeaveTracker {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;

	private int year;

	private int month;

	private boolean isTaken = false;

	private boolean clCarriedForward = false; // ✅ NEW FIELD

	public CasualLeaveTracker() {
	}

	public CasualLeaveTracker(String username, int year, int month) {
		this.username = username;
		this.year = year;
		this.month = month;
		this.isTaken = false;
		this.clCarriedForward = false; // ✅ initialize
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

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public boolean isTaken() {
		return isTaken;
	}

	public void setTaken(boolean isTaken) {
		this.isTaken = isTaken;
	}

	public boolean isClCarriedForward() {
		return clCarriedForward;
	}

	public void setClCarriedForward(boolean clCarriedForward) {
		this.clCarriedForward = clCarriedForward;
	}

	@Override
	public String toString() {
		return "CasualLeaveTracker [id=" + id + ", username=" + username + ", year=" + year + ", month=" + month
				+ ", isTaken=" + isTaken + ", clCarriedForward=" + clCarriedForward + "]";
	}

}