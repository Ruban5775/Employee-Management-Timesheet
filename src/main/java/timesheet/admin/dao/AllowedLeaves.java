package timesheet.admin.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AllowedLeaves {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String username;

	private int casualTaken = 0;
	private int casualAllowed = 12;

	private int baseCasualTaken = 0;

	private double sickTaken = 0;
	private double sickAllowed = 6;

	private double floatingTaken = 0;
	private double floatingAllowed = 2;

	private double year;

	private int earncasualLeave = 0;

	public AllowedLeaves() {
	}

	public AllowedLeaves(String username, int year) {
		this.username = username;
		this.year = year;
	}

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

	public int getCasualTaken() {
		return casualTaken;
	}

	public void setCasualTaken(int casualTaken) {
		this.casualTaken = casualTaken;
	}

	public int getCasualAllowed() {
		return casualAllowed;
	}

	public void setCasualAllowed(int casualAllowed) {
		this.casualAllowed = casualAllowed;
	}

	public double getSickTaken() {
		return sickTaken;
	}

	public void setSickTaken(double sickTaken) {
		this.sickTaken = sickTaken;
	}

	public double getSickAllowed() {
		return sickAllowed;
	}

	public void setSickAllowed(double sickAllowed) {
		this.sickAllowed = sickAllowed;
	}

	public double getFloatingTaken() {
		return floatingTaken;
	}

	public void setFloatingTaken(double floatingTaken) {
		this.floatingTaken = floatingTaken;
	}

	public double getFloatingAllowed() {
		return floatingAllowed;
	}

	public void setFloatingAllowed(double floatingAllowed) {
		this.floatingAllowed = floatingAllowed;
	}

	public double getYear() {
		return year;
	}

	public void setYear(double year) {
		this.year = year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getBaseCasualTaken() {
		return baseCasualTaken;
	}

	public void setBaseCasualTaken(int baseCasualTaken) {
		this.baseCasualTaken = baseCasualTaken;
	}

	public int getEarncasualLeave() {
		return earncasualLeave;
	}

	public void setEarncasualLeave(int earncasualLeave) {
		this.earncasualLeave = earncasualLeave;
	}

	@Override
	public String toString() {
		return "AllowedLeaves [id=" + id + ", username=" + username + ", casualTaken=" + casualTaken
				+ ", casualAllowed=" + casualAllowed + ", baseCasualTaken=" + baseCasualTaken + ", sickTaken="
				+ sickTaken + ", sickAllowed=" + sickAllowed + ", floatingTaken=" + floatingTaken + ", floatingAllowed="
				+ floatingAllowed + ", year=" + year + ", earncasualLeave=" + earncasualLeave + "]";
	}

}
