package smart.bean;

public class Customer {
	private int custid;
	private String custname;
	private String custpnum;
	private String custaddress;
	private int salesum;

	public Customer(int custid, String custname, String custpnum, String custaddress, int salesum) {
		super();
		this.custid = custid;
		this.custname = custname;
		this.custpnum = custpnum;
		this.custaddress = custaddress;
		this.salesum = salesum;
	}

	public Customer(int custid, String custname, String custpnum, String custaddress) {
		super();
		this.custid = custid;
		this.custname = custname;
		this.custpnum = custpnum;
		this.custaddress = custaddress;
	}

	public Customer(String custname, String custpnum, String custaddress) {
		super();
		this.custname = custname;
		this.custpnum = custpnum;
		this.custaddress = custaddress;
	}

	public int getCustid() {
		return custid;
	}

	public void setCustid(int custid) {
		this.custid = custid;
	}

	public String getCustname() {
		return custname;
	}

	public void setCustname(String custname) {
		this.custname = custname;
	}

	public String getCustpnum() {
		return custpnum;
	}

	public void setCustpnum(String custpnum) {
		this.custpnum = custpnum;
	}

	public String getCustaddress() {
		return custaddress;
	}

	public void setCustaddress(String custaddress) {
		this.custaddress = custaddress;
	}

	public int getSalesum() {
		return salesum;
	}

	public void setSalesum(int salesum) {
		this.salesum = salesum;
	}
}
