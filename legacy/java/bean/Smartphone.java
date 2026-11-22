package smart.bean;

public class Smartphone {
	private String smartid;
	private String smartname;
	private float smartprice;
	private int smartqty;
	private int smartram;
	private int smartstorage;
	private String smartimg;

	public Smartphone() {
		super();
	}

	public Smartphone(String smartid, String smartname, float smartprice, int smartqty, int smartram, int smartstorage, String smartimg) {
		super();
		this.smartid = smartid;
		this.smartname = smartname;
		this.smartprice = smartprice;
		this.smartqty = smartqty;
		this.smartram = smartram;
		this.smartstorage = smartstorage;
		this.smartimg = smartimg;
	}

	public Smartphone(String smartname, float smartprice, int smartqty, int smartram, int smartstorage, String smartimg) {
		super();
		this.smartname = smartname;
		this.smartprice = smartprice;
		this.smartqty = smartqty;
		this.smartram = smartram;
		this.smartstorage = smartstorage;
		this.smartimg = smartimg;
	}

	public Smartphone(String smartid, String smartname, float smartprice, int smartqty, int smartram, int smartstorage) {
		super();
		this.smartid = smartid;
		this.smartname = smartname;
		this.smartprice = smartprice;
		this.smartqty = smartqty;
		this.smartram = smartram;
		this.smartstorage = smartstorage;
	}

	public Smartphone(String smartname, float smartprice, int smartqty, int smartram, int smartstorage) {
		super();
		this.smartname = smartname;
		this.smartprice = smartprice;
		this.smartqty = smartqty;
		this.smartram = smartram;
		this.smartstorage = smartstorage;
	}

	public Smartphone(String smartid, int smartqty) {
		super();
		this.smartid = smartid;
		this.smartqty = smartqty;
	}

	public String getSmartid() {
		return smartid;
	}

	public void setSmartid(String smartid) {
		this.smartid = smartid;
	}

	public String getSmartname() {
		return smartname;
	}

	public void setSmartname(String smartname) {
		this.smartname = smartname;
	}

	public float getSmartprice() {
		return smartprice;
	}

	public void setSmartprice(float smartprice) {
		this.smartprice = smartprice;
	}

	public int getSmartqty() {
		return smartqty;
	}

	public void setSmartqty(int smartqty) {
		this.smartqty = smartqty;
	}

	public int getSmartram() {
		return smartram;
	}

	public void setSmartram(int smartram) {
		this.smartram = smartram;
	}

	public int getSmartstorage() {
		return smartstorage;
	}

	public void setSmartstorage(int smartstorage) {
		this.smartstorage = smartstorage;
	}

	public String getSmartimg() {
		return smartimg;
	}

	public void setSmartimg(String smartimg) {
		this.smartimg = smartimg;
	}
}
