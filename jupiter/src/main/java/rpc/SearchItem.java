package rpc;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import db.MySqlConnection;
import entity.Item;

//import org.json.JSONArray;
//import org.json.JSONObject;

import external.GitHubClient;

/**
 * Servlet implementation class SearchItem
 */
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
    //浏览器默认操作是get
    //如果要浏览器需要其他操作，需要单独写程序来改变浏览器的操作
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		
		
		/*
		 * 
		 * 括号里返回的类型，也可以是（“Text/html“)
		 * 这里括号里不写也行，系统会自动读出返回类型
		 * 但是不是好的code style
		response.setContentType("application/json");
		//这里的write的对象是response,用于返回
		PrintWriter writer = response.getWriter();
		JSONObject obj = new JSONObject();
		
		obj.put("username", "abcd");
		// == wrtie.print(obj.toString())
		writer.print(obj);
		*/
		
		/*
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		//浏览器里面传入参数
		//http://localhost:8080/jupiter/search?username=123
		// return username: 123
		//http://localhost:8080/jupiter/search
		//什么也没有返回
		if (request.getParameter("username") != null) {
			JSONObject obj = new JSONObject();
			String username = request.getParameter("username");
			obj.put("username", username);
			writer.print(obj);
		}
		*/
		
		
		//ex:返回jason array的数据
		//response.setContentType("application/json");
		//PrintWriter writer = response.getWriter();
		
		//JSONArray array = new JSONArray();
		//每一条内array返回的顺序和输入的顺序没有关系
		//排序顺序和map的排序顺序一致
		//但是array的顺序和输入一致
		//array.put(new JSONObject().put("name", "abcd").put("address","San Francsio").put("time", "01/01/2019"));
		//array.put(new JSONObject().put("name", "1234").put("address", "San Jose").put("time", "01/01/2018"));

		//返回一个array
		//writer.print(array);
		//RpcHelper.writeJsonArray(response, array);
		//如果没有login,就不能用search功能
		HttpSession session = request.getSession(false);
		if (session == null) {
			//403: forbidden
			response.setStatus(403);
			return;
		}

		String userId = request.getParameter("user_id");
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));

		GitHubClient client = new GitHubClient();
		List<Item> items = client.search(lat, lon, null);
		MySqlConnection connection = new MySqlConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
		connection.close();

		JSONArray array = new JSONArray();
		for (Item item : items) {
			JSONObject obj = item.toJSONObject();
			obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
			array.put(obj);
		}
		RpcHelper.writeJsonArray(response, array);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
