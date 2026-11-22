package smart.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import smart.bean.Customer;
import smart.bean.Employee;
import smart.bean.Smartphone;

public class SmartDao {
	private static final String LOGIN = "SELECT * FROM employee WHERE EmpUName = ?";
	private static final String SALE = "INSERT INTO sale (SaleQty, SaleTotal, SmartID, CustID, EmpID) VALUES (?, ?, ?, ?, ?); UPDATE smartphone SET SmartQty = SmartQty - ? WHERE SmartID = ?";
	private static final String SALE_SELECT = "SELECT * FROM sale sa LEFT JOIN smartphone sm ON sa.SmartID = sm.SmartID LEFT JOIN customer c ON sa.CustID = c.CustID LEFT JOIN employee e ON sa.EmpID = e.EmpID ORDER BY SaleDate DESC";
	private static final String SMART_MORE = "UPDATE smartphone SET SmartQty = SmartQty + ? WHERE SmartID = ?";
	private static final String SMART_INSERT = "INSERT INTO smartphone (SmartName, SmartPrice, SmartQty, SmartRAM, SmartStorage, SmartImage) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String SMART_INSERT_WIMG = "INSERT INTO smartphone (SmartName, SmartPrice, SmartQty, SmartRAM, SmartStorage) VALUES (?, ?, ?, ?, ?)";
	private static final String SMART_SELECT = "SELECT * FROM smartphone";
	private static final String SMART_SELECT_BY_ID = "SELECT * FROM smartphone WHERE SmartID = ?";
	private static final String SMART_UPDATE = "UPDATE smartphone SET SmartName = ?, SmartPrice = ?, SmartQty = ?, SmartRAM = ?, SmartStorage = ?, SmartImage = ? WHERE SmartID = ?";
	private static final String SMART_UPDATE_WIMG = "UPDATE smartphone SET SmartName = ?, SmartPrice = ?, SmartQty = ?, SmartRAM = ?, SmartStorage = ? WHERE SmartID = ?";
	private static final String SMART_DELETE = "DELETE FROM smartphone WHERE SmartID = ?";
	private static final String EMP_INSERT = "INSERT INTO employee (EmpName, EmpPNum, EmpAddress, EmpUName, EmpPword) VALUES (?, ?, ?, ?, ?)";
	private static final String EMP_SELECT = "SELECT *, SUM(s.SaleQty) AS SaleSum FROM employee e LEFT JOIN sale s ON s.EmpID = e.EmpID WHERE EmpName != 'Admin' GROUP BY e.EmpID ORDER BY SaleSum DESC";
	private static final String EMP_SELECT_BY_ID = "SELECT * FROM employee WHERE EmpID = ?";
	private static final String EMP_UPDATE = "UPDATE employee SET EmpName = ?, EmpPNum = ?, EmpAddress = ?, EmpUName = ?, EmpPword = ? WHERE EmpID = ?";
	private static final String EMP_UPDATE_WP = "UPDATE employee SET EmpName = ?, EmpPNum = ?, EmpAddress = ?, EmpUName = ? WHERE EmpID = ?";
	private static final String EMP_ACC_UPDATE = "UPDATE employee SET EmpUName = ?, EmpPword = ? WHERE EmpID = ?";
	private static final String EMP_ACC_UPDATE_WP = "UPDATE employee SET EmpUName = ? WHERE EmpID = ?";
	private static final String EMP_DELETE = "DELETE FROM employee WHERE EmpID = ?";
	private static final String CUST_INSERT = "INSERT INTO customer (CustName, CustPNum, CustAddress) VALUES (?, ?, ?)";
	private static final String CUST_SELECT = "SELECT *, SUM(s.SaleQty) AS SaleSum FROM customer c LEFT JOIN sale s ON c.CustID = s.CustID GROUP BY c.CustID ORDER BY SaleSum DESC";
	private static final String CUST_SELECT_BY_ID = "SELECT * FROM customer WHERE CustID = ?";
	private static final String CUST_UPDATE = "UPDATE customer SET CustName = ?, CustPNum = ?, CustAddress = ? WHERE CustID = ?";
	private static final String CUST_DELETE = "DELETE FROM customer WHERE CustID = ?";
	
	public SmartDao() {
		
	}

	protected Connection DB() {
		Connection con = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost/db_smartphone?allowMultiQueries=true", "root", "");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return con;
	}

	protected static Connection staticDB() {
		Connection con = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost/db_smartphone?allowMultiQueries=true", "root", "");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return con;
	}
	
	public static Employee smartLogin(Employee emp) {
		Connection con = null;
		ResultSet rs = null;
		con = staticDB();
		try (PreparedStatement ps = con.prepareStatement(LOGIN)) {
			ps.setString(1, emp.getEmpuname());
			rs = ps.executeQuery();
			rs.next();
			if (BCrypt.checkpw(emp.getEmppword(), rs.getString("EmpPword")) != true) {
				emp.setValid(false);
			} else {
				emp.setEmpid(rs.getInt("EmpID"));
				emp.setEmpname(rs.getString("EmpName"));
				emp.setEmppword(rs.getString("EmpPword"));
				emp.setValid(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {}
				rs = null;
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {}
				con = null;
			}
		}
		return emp;
	}

	public void smartSale(int saleqty, float saletotal, String smartid, int custid, int empid) throws SQLException {
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(SALE)) {
			ps.setInt(1, saleqty);
			ps.setFloat(2, saletotal);
			ps.setString(3, smartid);
			ps.setInt(4, custid);
			ps.setInt(5, empid);
			ps.setInt(6, saleqty);
			ps.setString(7, smartid);
			ps.execute();
		} catch (SQLException e) {
			printSQLException(e);
		}
	}

	public boolean smartMore(Smartphone smart) throws SQLException {
		boolean rowUpdated;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(SMART_MORE);) {
			ps.setInt(1, smart.getSmartqty());
			ps.setString(2, smart.getSmartid());
			rowUpdated = ps.executeUpdate() > 0;
		}
		return rowUpdated;
	}

	public void smartInsert(Smartphone smart) throws SQLException {
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(SMART_INSERT)) {
			ps.setString(1, smart.getSmartname());
			ps.setFloat(2, smart.getSmartprice());
			ps.setInt(3, smart.getSmartqty());
			ps.setInt(4, smart.getSmartram());
			ps.setInt(5, smart.getSmartstorage());
			ps.setString(6, smart.getSmartimg());
			ps.executeUpdate();
		} catch (SQLException e) {
			printSQLException(e);
		}
	}

	public void smartInsertWImg(Smartphone smart) throws SQLException {
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(SMART_INSERT_WIMG)) {
			ps.setString(1, smart.getSmartname());
			ps.setFloat(2, smart.getSmartprice());
			ps.setInt(3, smart.getSmartqty());
			ps.setInt(4, smart.getSmartram());
			ps.setInt(5, smart.getSmartstorage());
			ps.executeUpdate();
		} catch (SQLException e) {
			printSQLException(e);
		}
	}

	public void empInsert(Employee emp) throws SQLException {
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(EMP_INSERT)) {
			ps.setString(1, emp.getEmpname());
			ps.setString(2, emp.getEmppnum());
			ps.setString(3, emp.getEmpaddress());
			ps.setString(4, emp.getEmpuname());
			ps.setString(5, emp.getEmppword());
			ps.executeUpdate();
		} catch (SQLException e) {
			printSQLException(e);
		}
	}

	public void custInsert(Customer customer) throws SQLException {
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(CUST_INSERT)) {
			ps.setString(1, customer.getCustname());
			ps.setString(2, customer.getCustpnum());
			ps.setString(3, customer.getCustaddress());
			ps.executeUpdate();
		} catch (SQLException e) {
			printSQLException(e);
		}
	}

	public ResultSet saleSelect() throws SQLException {
		ResultSet rs = null;
		Connection con = DB();
		PreparedStatement ps = con.prepareStatement(SALE_SELECT);
		rs = ps.executeQuery();
		return rs;
	}

	public List<Smartphone> smartSelect() {
		List<Smartphone> smart = new ArrayList<>();
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(SMART_SELECT);) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String smartid = rs.getString("SmartID");
				String smartname = rs.getString("SmartName");
				float smartprice = rs.getFloat("SmartPrice");
				int smartnb = rs.getInt("SmartQty");
				int smartram = rs.getInt("SmartRAM");
				int smartstorage = rs.getInt("SmartStorage");
				String smartimg = rs.getString("SmartImage");
				smart.add(new Smartphone(smartid, smartname, smartprice, smartnb, smartram, smartstorage, smartimg));
			}
		} catch (SQLException e) {
			printSQLException(e);
		}
		return smart;
	}

	public List<Employee> empSelect() {
		List<Employee> emp = new ArrayList<>();
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(EMP_SELECT);) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int empid = rs.getInt("EmpID");
				String empname = rs.getString("EmpName");
				String emppnum = rs.getString("EmpPNum");
				String empaddress = rs.getString("EmpAddress");
				String empuname = rs.getString("EmpUName");
				String emppword = rs.getString("EmpPword");
				int salesum = rs.getInt("SaleSum");
				emp.add(new Employee(empid, empname, emppnum, empaddress, empuname, emppword, salesum));
			}
		} catch (SQLException e) {
			printSQLException(e);
		}
		return emp;
	}

	public List<Customer> custSelect() {
		List<Customer> customer = new ArrayList<>();
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(CUST_SELECT);) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int custid = rs.getInt("CustID");
				String custname = rs.getString("CustName");
				String custpnum = rs.getString("CustPNum");
				String custaddress = rs.getString("CustAddress");
				int salesum = rs.getInt("SaleSum");
				customer.add(new Customer(custid, custname, custpnum, custaddress, salesum));
			}
		} catch (SQLException e) {
			printSQLException(e);
		}
		return customer;
	}

	public Smartphone smartSelectByID(String smartid) {
		Smartphone smart = null;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(SMART_SELECT_BY_ID);) {
			ps.setString(1, smartid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String smartname = rs.getString("SmartName");
				float smartprice = rs.getFloat("SmartPrice");
				int smartnb = rs.getInt("SmartQty");
				int smartram = rs.getInt("SmartRAM");
				int smartstorage = rs.getInt("SmartStorage");
				String smartimg = rs.getString("SmartImage");
				smart = new Smartphone(smartid, smartname, smartprice, smartnb, smartram, smartstorage, smartimg);
			}
		} catch (SQLException e) {
			printSQLException(e);
		}
		return smart;
	}

	public Employee empSelectByID(int empid) {
		Employee emp = null;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(EMP_SELECT_BY_ID);) {
			ps.setInt(1, empid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String empname = rs.getString("EmpName");
				String emppnum = rs.getString("EmpPNum");
				String empaddress = rs.getString("EmpAddress");
				String empuname = rs.getString("EmpUName");
				String emppword = rs.getString("EmpPword");
				emp = new Employee(empid, empname, emppnum, empaddress, empuname, emppword);
			}
		} catch (SQLException e) {
			printSQLException(e);
		}
		return emp;
	}

	public Customer custSelectByID(int custid) {
		Customer customer = null;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(CUST_SELECT_BY_ID);) {
			ps.setInt(1, custid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String custname = rs.getString("CustName");
				String custpnum = rs.getString("CustPNum");
				String custaddress = rs.getString("CustAddress");
				customer = new Customer(custid, custname, custpnum, custaddress);
			}
		} catch (SQLException e) {
			printSQLException(e);
		}
		return customer;
	}

	public boolean smartUpdate(Smartphone smart) throws SQLException {
		boolean rowUpdated;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(SMART_UPDATE);) {
			ps.setString(1, smart.getSmartname());
			ps.setFloat(2, smart.getSmartprice());
			ps.setInt(3, smart.getSmartqty());
			ps.setInt(4, smart.getSmartram());
			ps.setInt(5, smart.getSmartstorage());
			ps.setString(6, smart.getSmartimg());
			ps.setString(7, smart.getSmartid());
			rowUpdated = ps.executeUpdate() > 0;
		}
		return rowUpdated;
	}

	public boolean smartUpdateWImg(Smartphone smart) throws SQLException {
		boolean rowUpdated;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(SMART_UPDATE_WIMG);) {
			ps.setString(1, smart.getSmartname());
			ps.setFloat(2, smart.getSmartprice());
			ps.setInt(3, smart.getSmartqty());
			ps.setInt(4, smart.getSmartram());
			ps.setInt(5, smart.getSmartstorage());
			ps.setString(6, smart.getSmartid());
			rowUpdated = ps.executeUpdate() > 0;
		}
		return rowUpdated;
	}

	public boolean empUpdate(Employee emp) throws SQLException {
		boolean rowUpdated;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(EMP_UPDATE);) {
			ps.setString(1, emp.getEmpname());
			ps.setString(2, emp.getEmppnum());
			ps.setString(3, emp.getEmpaddress());
			ps.setString(4, emp.getEmpuname());
			ps.setString(5, emp.getEmppword());
			ps.setInt(6, emp.getEmpid());
			rowUpdated = ps.executeUpdate() > 0;
		}
		return rowUpdated;
	}

	public boolean empUpdateWP(Employee emp) throws SQLException {
		boolean rowUpdated;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(EMP_UPDATE_WP);) {
			ps.setString(1, emp.getEmpname());
			ps.setString(2, emp.getEmppnum());
			ps.setString(3, emp.getEmpaddress());
			ps.setString(4, emp.getEmpuname());
			ps.setInt(5, emp.getEmpid());
			rowUpdated = ps.executeUpdate() > 0;
		}
		return rowUpdated;
	}

	public boolean empAccUpdate(Employee emp) throws SQLException {
		boolean rowUpdated;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(EMP_ACC_UPDATE);) {
			ps.setString(1, emp.getEmpuname());
			ps.setString(2, emp.getEmppword());
			ps.setInt(3, emp.getEmpid());
			rowUpdated = ps.executeUpdate() > 0;
		}
		return rowUpdated;
	}

	public boolean empAccUpdateWP(Employee emp) throws SQLException {
		boolean rowUpdated;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(EMP_ACC_UPDATE_WP);) {
			ps.setString(1, emp.getEmpuname());
			ps.setInt(2, emp.getEmpid());
			rowUpdated = ps.executeUpdate() > 0;
		}
		return rowUpdated;
	}

	public boolean custUpdate(Customer customer) throws SQLException {
		boolean rowUpdated;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(CUST_UPDATE);) {
			ps.setString(1, customer.getCustname());
			ps.setString(2, customer.getCustpnum());
			ps.setString(3, customer.getCustaddress());
			ps.setInt(4, customer.getCustid());
			rowUpdated = ps.executeUpdate() > 0;
		}
		return rowUpdated;
	}

	public boolean smartDelete(int smartid) throws SQLException {
		boolean rowDeleted;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(SMART_DELETE);) {
			ps.setInt(1, smartid);
			rowDeleted = ps.executeUpdate() > 0;
		}
		return rowDeleted;
	}

	public boolean empDelete(int empid) throws SQLException {
		boolean rowDeleted;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(EMP_DELETE);) {
			ps.setInt(1, empid);
			rowDeleted = ps.executeUpdate() > 0;
		}
		return rowDeleted;
	}

	public boolean custDelete(int custid) throws SQLException {
		boolean rowDeleted;
		try (Connection con = DB(); PreparedStatement ps = con.prepareStatement(CUST_DELETE);) {
			ps.setInt(1, custid);
			rowDeleted = ps.executeUpdate() > 0;
		}
		return rowDeleted;
	}

	private void printSQLException(SQLException ex) {
		for (Throwable e : ex) {
			if (e instanceof SQLException) {
				e.printStackTrace(System.err);
				System.err.println("SQLState : " + ((SQLException) e).getSQLState());
				System.err.println("Error code : " + ((SQLException) e).getErrorCode());
				System.err.println("Message : " + e.getMessage());
				Throwable th = ex.getCause();
				while (th != null) {
					System.out.println("Cause : " + th);
					th = th.getCause();
				}
			}
		}
	}
	
	public static void main(String[] args) throws SQLException {
		SmartDao sd = new SmartDao();
		Connection con = sd.DB();
		PreparedStatement ps = con.prepareStatement(SALE_SELECT);
		ResultSet rs = ps.executeQuery();
		System.out.println(rs);
	}
}
