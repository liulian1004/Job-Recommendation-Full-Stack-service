package external;
//发送request,等待结果

//所以不需要servlet

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class GitHubClient {
	// 把绝大部分网页网址先写好
	// %s以后填充用
	// 这段程序以后能重复调用，更新URL_TEMPLATE以及返回类型（JSONArry）
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	// 如果description是null,默认就是developer
	private static final String DEFAULT_KEYWORD = "developer";

	public List<Item> search(double lat, double lon, String keyword) {
		// corner case 1:
		// 关键字为空
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		// corner case 2:
		// 把浏览器内的输入的特殊字符转换成机器能读懂的字符
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8"); // rick sun --> rick+sun
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// create a HTTP request parameter
		String url = String.format(URL_TEMPLATE, keyword, lat, lon);

		// send HTTP request
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			CloseableHttpResponse response = httpClient.execute(new HttpGet(url));

			// 上面的CloseableHttpResponse可以在不用这个程序
			// httpClient.close();

			// Get Http reponse body
			// 先判断返回值是不是200
			// status code ：200 -->代表返回成功
			if (response.getStatusLine().getStatusCode() != 200) {
				return new ArrayList<>();
			}
			HttpEntity entity = response.getEntity(); // response body
			if (entity == null) {
				return new ArrayList<>();
			}
			// 这里要一段段的，对内存压力少一些
			// 所以buffer reader就是实现一行行读
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));

			StringBuilder responseBody = new StringBuilder();
			String line = null;
			// 把当前数据覆盖null之后，在判断是否是null
			while ((line = reader.readLine()) != null) {
				responseBody.append(line);
			}

			JSONArray array = new JSONArray(responseBody.toString());
			return getItemList(array);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	private List<Item> getItemList(JSONArray array) {
		List<Item> itemList = new ArrayList<>();
		// 连接Monkeylearn client
		List<String> descriptionList = new ArrayList<>();

		for (int i = 0; i < array.length(); i++) {
			// We need to extract keywords from description since GitHub API
			// doesn't return keywords.
			//if not description, choose title
			String description = getStringFieldOrEmpty(array.getJSONObject(i), "description");
			if (description.equals("") || description.equals("\n")) {
				descriptionList.add(getStringFieldOrEmpty(array.getJSONObject(i), "title"));
			} else {
				descriptionList.add(description);
			}
		}

		// We need to get keywords from multiple text in one request since
		// MonkeyLearnAPI has limitation on request per minute.
		List<List<String>> keywords = MonkeyLearnClient
				.extractKeywords(descriptionList.toArray(new String[descriptionList.size()]));

		for (int i = 0; i < array.length(); ++i) {
			JSONObject object = array.getJSONObject(i);

			// builder pattern的调用方法
			// step1 new builder
			ItemBuilder builder = new ItemBuilder();
			// step2 set up new value
			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));
			//set用于keyword去重
			builder.setKeywords(new HashSet<String>(keywords.get(i)));

			// 调用build函数返回item实例
			Item item = builder.build();
			itemList.add(item);
		}

		return itemList;
	}

	// method: 判断输入是否是null
	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		return obj.isNull(field) ? "" : obj.getString(field);
	}

}
