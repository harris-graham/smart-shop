package smart.bean;

public class SmartCart {
	private Smartphone smart;
	private int cartqty;
	
	public SmartCart() {
		super();
	}

	public SmartCart(Smartphone smart, int cartqty) {
		super();
		this.smart = smart;
		this.cartqty = cartqty;
	}
	
	public Smartphone getSmart() {
		return smart;
	}
	
	public void setSmart(Smartphone smart) {
		this.smart = smart;
	}
	
	public int getCartqty() {
		return cartqty;
	}
	
	public void setCartqty(int cartqty) {
		this.cartqty = cartqty;
	}
}
