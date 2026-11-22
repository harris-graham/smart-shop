package smart.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.tomcat.util.codec.binary.Base64;
import org.mindrot.jbcrypt.BCrypt;

import smart.bean.Customer;
import smart.bean.Employee;
import smart.bean.SmartCart;
import smart.bean.Smartphone;
import smart.dao.SmartDao;

/**
 * Servlet implementation class SmartServlet
 */
@WebServlet("/")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 1, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024
		* 100)
public class SmartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SmartDao smartDao;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		smartDao = new SmartDao();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getServletPath();
		switch (action) {
		case "/smartLogin":
			smartLogin(request, response);
			break;
		case "/smartLogout":
			smartLogout(request, response);
			break;
		case "/smartShop":
			log(request, response);
			smartShop(request, response);
			break;
		case "/addToCart":
			addToCart(request, response);
			break;
		case "/remFromCart":
			remFromCart(request, response);
			break;
		case "/smartSale":
			smartSale(request, response);
			break;
		case "/saleSelect":
			saleSelect(request, response);
			break;
		case "/smartMore":
			smartMore(request, response);
			break;
		case "/smartInsert":
			smartInsert(request, response);
			break;
		case "/smartSelect":
			log(request, response);
			smartSelect(request, response);
			break;
		case "/smartUpdate":
			smartUpdate(request, response);
			break;
		case "/smartDelete":
			smartDelete(request, response);
			break;
		case "/empInsert":
			empInsert(request, response);
			break;
		case "/empSelect":
			log(request, response);
			empSelect(request, response);
			break;
		case "/empUpdate":
			empUpdate(request, response);
			break;
		case "/empAccUpdate":
			empAccUpdate(request, response);
			break;
		case "/empDelete":
			empDelete(request, response);
			break;
		case "/custInsert":
			custInsert(request, response);
			response.sendRedirect("custSelect");
			break;
		case "/custQuiInsert":
			custInsert(request, response);
			response.sendRedirect("smartShop");
			break;
		case "/custSelect":
			log(request, response);
			custSelect(request, response);
			break;
		case "/custUpdate":
			custUpdate(request, response);
			break;
		case "/custDelete":
			custDelete(request, response);
			break;
		default:
			request.getRequestDispatcher("SmartLogin.jsp").forward(request, response);
			break;
		}
	}

	private void smartLogin(HttpServletRequest request, HttpServletResponse response) {
		Employee emp = new Employee();
		emp.setEmpuname(request.getParameter("empuname"));
		emp.setEmppword(request.getParameter("emppword"));
		try {
			emp = SmartDao.smartLogin(emp);
			if (emp.isValid()) {
				HttpSession hs = request.getSession(true);
				hs.setAttribute("session", emp);
				response.sendRedirect("smartShop");
			} else
				response.sendRedirect("SmartLogin.jsp");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void smartLogout(HttpServletRequest request, HttpServletResponse response) {
		try {
			HttpSession hs = request.getSession(true);
			hs.removeAttribute("session");
			response.sendRedirect("SmartLogin.jsp");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void log(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Employee emp = new Employee();
		HttpSession hs = request.getSession(true);
		emp = (Employee) hs.getAttribute("session");
		if (emp == null) {
			request.getRequestDispatcher("SmartLogin.jsp").forward(request, response);
		}
	}

	private void smartShop(HttpServletRequest request, HttpServletResponse response) {
		try {
			List<Smartphone> smartList = smartDao.smartSelect();
			request.setAttribute("smartList", smartList);
			List<Customer> custList = smartDao.custSelect();
			request.setAttribute("custList", custList);
			request.getRequestDispatcher("SmartShop.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		SmartDao sd = new SmartDao();
		HttpSession hs = request.getSession();
		if (hs.getAttribute("cart") == null) {
			List<SmartCart> cart = new ArrayList<SmartCart>();
			cart.add(new SmartCart(sd.smartSelectByID(request.getParameter("smartid")),
					Integer.parseInt(request.getParameter("saleqty"))));
			hs.setAttribute("cart", cart);
		} else {
			@SuppressWarnings("unchecked")
			List<SmartCart> cart = (List<SmartCart>) hs.getAttribute("cart");
			int index = exist(request.getParameter("smartid"), cart);
			if (index == -1) {
				cart.add(new SmartCart(sd.smartSelectByID(request.getParameter("smartid")),
						Integer.parseInt(request.getParameter("saleqty"))));
			} else {
				int qty = cart.get(index).getCartqty() + Integer.parseInt(request.getParameter("saleqty"));
				cart.get(index).setCartqty(qty);
			}
			hs.setAttribute("cart", cart);
		}
		response.sendRedirect("smartShop");
	}

	private int exist(String smartid, List<SmartCart> cart) {
		for (int i = 0; i < cart.size(); i++) {
			if (cart.get(i).getSmart().getSmartid().equalsIgnoreCase(smartid)) {
				return i;
			}
		}
		return -1;
	}

	protected void remFromCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		@SuppressWarnings("unchecked")
		List<SmartCart> cart = (List<SmartCart>) session.getAttribute("cart");
		int index = exist(request.getParameter("smartid"), cart);
		cart.remove(index);
		session.setAttribute("cart", cart);
		response.sendRedirect("smartShop");
	}

	private void smartSale(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession hs = request.getSession();
		@SuppressWarnings("unchecked")
		List<SmartCart> cart = (List<SmartCart>) hs.getAttribute("cart");
		Employee emp = (Employee) hs.getAttribute("session");
		int custid = Integer.parseInt(request.getParameter("custid"));
		int empid = emp.getEmpid();
		for (int i = cart.size(); i > 0; i--) {
			Smartphone smart = cart.get(0).getSmart();
			try {
				smartDao.smartSale(cart.get(0).getCartqty(), cart.get(0).getCartqty() * smart.getSmartprice(),
						smart.getSmartid(), custid, empid);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			cart.remove(0);
		}
		response.sendRedirect("smartShop");
	}

	private void smartMore(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int smartqty = Integer.parseInt(request.getParameter("smartqty"));
		String smartid = request.getParameter("smartid");
		Smartphone smart = new Smartphone(smartid, smartqty);
		try {
			smartDao.smartMore(smart);
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.sendRedirect("smartSelect");
	}

	private void smartInsert(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String smartname = request.getParameter("smartname");
		float smartprice = Float.parseFloat(request.getParameter("smartprice"));
		int smartqty = Integer.parseInt(request.getParameter("smartqty"));
		int smartram = Integer.parseInt(request.getParameter("smartram"));
		int smartstorage = Integer.parseInt(request.getParameter("smartstorage"));
		Part si = request.getPart("smartimg");
		String simg = si.getSubmittedFileName();
		if (simg != "") {
		for (Part part : request.getParts()) {
			part.write("C:/JSP Upload/" + simg);
		}
		File file = new File("C:/JSP Upload/" + simg);
		FileInputStream fis = new FileInputStream(file);
		byte bt[] = new byte[(int) file.length()];
		fis.read(bt);
		String smartimg = Base64.encodeBase64String(bt);
		fis.close();
		Smartphone smart = new Smartphone(smartname, smartprice, smartqty, smartram, smartstorage, smartimg);
		try {
			smartDao.smartInsert(smart);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		} else {
			Smartphone smart = new Smartphone(smartname, smartprice, smartqty, smartram, smartstorage);
			try {
				smartDao.smartInsertWImg(smart);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		response.sendRedirect("smartSelect");
	}

	private void empInsert(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String empname = request.getParameter("empname");
		String emppnum = request.getParameter("emppnum");
		String empaddress = request.getParameter("empaddress");
		String empuname = request.getParameter("empuname");
		String emppword = BCrypt.hashpw(request.getParameter("emppword"), BCrypt.gensalt(12));
		Employee emp = new Employee(empname, emppnum, empaddress, empuname, emppword);
		try {
			smartDao.empInsert(emp);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		response.sendRedirect("empSelect");
	}

	private void custInsert(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String custname = request.getParameter("custname");
		String custpnum = request.getParameter("custpnum");
		String custaddress = request.getParameter("custaddress");
		Customer cust = new Customer(custname, custpnum, custaddress);
		try {
			smartDao.custInsert(cust);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void saleSelect(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.getRequestDispatcher("SaleList.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void smartSelect(HttpServletRequest request, HttpServletResponse response) {
		try {
			List<Smartphone> smartList = smartDao.smartSelect();
			request.setAttribute("smartList", smartList);
			request.getRequestDispatcher("SmartList.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void empSelect(HttpServletRequest request, HttpServletResponse response) {
		try {
			List<Employee> empList = smartDao.empSelect();
			request.setAttribute("empList", empList);
			request.getRequestDispatcher("EmpList.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void custSelect(HttpServletRequest request, HttpServletResponse response) {
		try {
			List<Customer> custList = smartDao.custSelect();
			request.setAttribute("custList", custList);
			request.getRequestDispatcher("CustList.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void smartSelectByID(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException {
//		int smartid = Integer.parseInt(request.getParameter("smartid"));
//		Smartphone smart;
//		try {
//			smart = smartDao.smartSelectByID(smartid);
//			RequestDispatcher rd = request.getRequestDispatcher("SmartForm.jsp");
//			request.setAttribute("smart", smart);
//			rd.forward(request, response);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void empSelectByID(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException {
//		int empid = Integer.parseInt(request.getParameter("empid"));
//		Employee emp;
//		try {
//			emp = smartDao.empSelectByID(empid);
//			RequestDispatcher rd = request.getRequestDispatcher("EmpForm.jsp");
//			request.setAttribute("emp", emp);
//			rd.forward(request, response);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	private void smartUpdate(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String smartid = request.getParameter("smartid");
		String smartname = request.getParameter("smartname");
		float smartprice = Float.parseFloat(request.getParameter("smartprice"));
		int smartqty = Integer.parseInt(request.getParameter("smartqty"));
		int smartram = Integer.parseInt(request.getParameter("smartram"));
		int smartstorage = Integer.parseInt(request.getParameter("smartstorage"));
		Part si = request.getPart("smartimg");
		String simg = si.getSubmittedFileName();
		if (simg != "") {
			for (Part part : request.getParts()) {
				part.write("C:/JSP Upload/" + simg);
			}
			File file = new File("C:/JSP Upload/" + simg);
			FileInputStream fis = new FileInputStream(file);
			byte bt[] = new byte[(int) file.length()];
			fis.read(bt);
			String smartimg = Base64.encodeBase64String(bt);
			fis.close();
			Smartphone smart = new Smartphone(smartid, smartname, smartprice, smartqty, smartram, smartstorage,
					smartimg);
			try {
				smartDao.smartUpdate(smart);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			Smartphone smart = new Smartphone(smartid, smartname, smartprice, smartqty, smartram, smartstorage);
			try {
				smartDao.smartUpdateWImg(smart);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		response.sendRedirect("smartSelect");
	}

	private void empUpdate(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int empid = Integer.parseInt(request.getParameter("empid"));
		String empname = request.getParameter("empname");
		String emppnum = request.getParameter("emppnum");
		String empaddress = request.getParameter("empaddress");
		String empuname = request.getParameter("empuname");
		if (request.getParameter("emppword") != "") {
			String emppword = BCrypt.hashpw(request.getParameter("emppword"), BCrypt.gensalt(12));
			Employee emp = new Employee(empid, empname, emppnum, empaddress, empuname, emppword);
			try {
				smartDao.empUpdate(emp);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			Employee emp = new Employee(empid, empname, emppnum, empaddress, empuname);
			try {
				smartDao.empUpdateWP(emp);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		response.sendRedirect("empSelect");
	}

	private void empAccUpdate(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int empid = Integer.parseInt(request.getParameter("empid"));
		String empuname = request.getParameter("empuname");
		if (request.getParameter("emppword") != "") {
			String emppword = BCrypt.hashpw(request.getParameter("emppword"), BCrypt.gensalt(12));
			Employee emp = new Employee(empid, empuname, emppword);
			try {
				smartDao.empAccUpdate(emp);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			Employee emp = new Employee(empid, empuname);
			try {
				smartDao.empAccUpdateWP(emp);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		response.sendRedirect("smartShop");
	}

	private void custUpdate(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int custid = Integer.parseInt(request.getParameter("custid"));
		String custname = request.getParameter("custname");
		String custpnum = request.getParameter("custpnum");
		String custaddress = request.getParameter("custaddress");
		Customer cust = new Customer(custid, custname, custpnum, custaddress);
		try {
			smartDao.custUpdate(cust);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		response.sendRedirect("custSelect");
	}

	private void smartDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int smartid = Integer.parseInt(request.getParameter("smartid"));
		try {
			smartDao.smartDelete(smartid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.sendRedirect("smartSelect");
	}

	private void empDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int empid = Integer.parseInt(request.getParameter("empid"));
		try {
			smartDao.empDelete(empid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.sendRedirect("empSelect");
	}

	private void custDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int custid = Integer.parseInt(request.getParameter("custid"));
		try {
			smartDao.custDelete(custid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.sendRedirect("custSelect");
	}

}
