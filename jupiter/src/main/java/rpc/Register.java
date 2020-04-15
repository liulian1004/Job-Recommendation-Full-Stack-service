package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import db.MySqlConnection;

/**
 * Servlet implementation class Register
 */
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	//为了安全原因，用户信息要写在dopost body 里面
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject input = RpcHelper.readJSONObject(request);
		String userId = input.getString("user_id");
		String password = input.getString("password");
		String firstname = input.getString("first_name");
		String lastname = input.getString("last_name");

		MySqlConnection connection = new MySqlConnection();
		JSONObject obj = new JSONObject();
		if (connection.addUser(userId, password, firstname, lastname)) {
			obj.put("status", "OK");
		} else {
			//按primary key判断 - user id
			//如果重复了，就不能注册了
			obj.put("status", "User Already Exists");
		}
		connection.close();
		RpcHelper.writeJsonObject(response, obj);
	}

}
