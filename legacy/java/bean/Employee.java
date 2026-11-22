package smart.bean;

public class Employee {
	private int empid;
	private String empname;
	private String emppnum;
	private String empaddress;
	private String empuname;
	private String emppword;
	private int salesum;
	public boolean valid;

	public Employee() {
		super();
	}

	public Employee(int empid, String empname, String emppnum, String empaddress, String empuname, String emppword,
			int salesum) {
		super();
		this.empid = empid;
		this.empname = empname;
		this.emppnum = emppnum;
		this.empaddress = empaddress;
		this.empuname = empuname;
		this.emppword = emppword;
		this.salesum = salesum;
	}

	public Employee(int empid, String empname, String emppnum, String empaddress, String empuname, String emppword) {
		super();
		this.empid = empid;
		this.empname = empname;
		this.emppnum = emppnum;
		this.empaddress = empaddress;
		this.empuname = empuname;
		this.emppword = emppword;
	}

	public Employee(int empid, String empname, String emppnum, String empaddress, String empuname) {
		super();
		this.empid = empid;
		this.empname = empname;
		this.emppnum = emppnum;
		this.empaddress = empaddress;
		this.empuname = empuname;
	}

	public Employee(String empname, String emppnum, String empaddress, String empuname, String emppword) {
		super();
		this.empname = empname;
		this.emppnum = emppnum;
		this.empaddress = empaddress;
		this.empuname = empuname;
		this.emppword = emppword;
	}

	public Employee(int empid, String empuname, String emppword) {
		super();
		this.empid = empid;
		this.empuname = empuname;
		this.emppword = emppword;
	}

	public Employee(int empid, String empuname) {
		super();
		this.empid = empid;
		this.empuname = empuname;
	}

	public Employee(String empuname, String emppword) {
		super();
		this.empuname = empuname;
		this.emppword = emppword;
	}

	public int getEmpid() {
		return empid;
	}

	public void setEmpid(int empid) {
		this.empid = empid;
	}

	public String getEmpname() {
		return empname;
	}

	public void setEmpname(String empname) {
		this.empname = empname;
	}

	public String getEmppnum() {
		return emppnum;
	}

	public void setEmppnum(String emppnum) {
		this.emppnum = emppnum;
	}

	public String getEmpaddress() {
		return empaddress;
	}

	public void setEmpaddress(String empaddress) {
		this.empaddress = empaddress;
	}

	public String getEmpuname() {
		return empuname;
	}

	public void setEmpuname(String empuname) {
		this.empuname = empuname;
	}

	public String getEmppword() {
		return emppword;
	}

	public void setEmppword(String emppword) {
		this.emppword = emppword;
	}

	public int getSalesum() {
		return salesum;
	}

	public void setSalesum(int salesum) {
		this.salesum = salesum;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
