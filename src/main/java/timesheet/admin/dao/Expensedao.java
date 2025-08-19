package timesheet.admin.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "expense_table")
public class Expensedao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@JsonProperty("Ex-code")
	private String Ex_code;

	@JsonProperty("Ex-type")
	private String Ex_type;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEx_code() {
		return Ex_code;
	}

	public void setEx_code(String ex_code) {
		Ex_code = ex_code;
	}

	public String getEx_type() {
		return Ex_type;
	}

	public void setEx_type(String ex_type) {
		Ex_type = ex_type;
	}

	@Override
	public String toString() {
		return "Expensedao [id=" + id + ", Ex_code=" + Ex_code + ", Ex_type=" + Ex_type + "]";
	}

}
