package timesheet.admin.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "codes_details")
public class Codedao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String codeType;
	private String code;
	private String clientName;
	private String projectType;
	private String startDate;
	private String country;
	private String description;
	private String status;

	public Codedao() {
	}

	public Codedao(String codeType, String code, String clientName, String projectType, String startDate,
			String country, String description) {
		this.codeType = codeType;
		this.code = code;
		this.clientName = clientName;
		this.projectType = projectType;
		this.startDate = startDate;
		this.country = country;
		this.description = description;
		this.status = "Progress";

	}

	public Codedao(String codeType, String code, String description) {
		this.codeType = codeType;
		this.code = code;
		this.description = description;
		this.clientName = "-";
		this.projectType = "-";
		this.startDate = "-";
		this.country = "-";
		this.status = "-";

	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCodeType() {
		return codeType;
	}

	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getProjectType() {
		return projectType;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Codedao [id=" + id + ", codeType=" + codeType + ", code=" + code + ", clientName=" + clientName
				+ ", projectType=" + projectType + ", startDate=" + startDate + ", country=" + country
				+ ", description=" + description + "]";
	}

}
