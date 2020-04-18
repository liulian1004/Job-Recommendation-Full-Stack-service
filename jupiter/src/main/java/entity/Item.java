package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

//刷选关键字的class
//item是从GitHub client生成，json格式
//但是选择关键字以后，还需要把删选后的内容转换成json
// 返回给browser
public class Item {
	//删选的关键字
	
	//这里用private + get/set  
	//封装性好
	//在set的时候还能筛选一下可以set的条件
	//如果只有读feild数据，只有get既可
	//这里class只需要实行get功能
	private String itemId;
	private String name;
	private String address;
	private Set<String> keywords;
	//用于对不同keyword设置权重
	//private Map<String, Integer> keywords;
	private String imageUrl;
	private String url;
	
	//被step3调用
	private Item(ItemBuilder builder) {
			this.itemId = builder.itemId;
			this.name = builder.name;
			this.address = builder.address;
			this.imageUrl = builder.imageUrl;
			this.url = builder.url;
			this.keywords = builder.keywords;
	}
	

	public String getItemId() {
		return itemId;
	}

	public String getName() {
		//set这里可以增加条件
		//if(name != null)
		return name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public Set<String> getKeywords() {
		return keywords;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getUrl() {
		return url;
	}
	//把刷选好的内容转换成jason
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("item_id", itemId);
		obj.put("name", name);
		obj.put("address", address);
		obj.put("keywords", new JSONArray(keywords));
		obj.put("image_url", imageUrl);
		obj.put("url", url);
		return obj;
	}

	//builder pattern - 用于多个变量的constructor
		//1. 建立privata variable
		//2. 设置set
		//3. 创建build函数，生成特定class的实例，返回给caller
	
		public static class ItemBuilder {
			//step 1
			//default: 空
			private String itemId;
			private String name;
			private String address;
			private String imageUrl;
			private String url;
			private Set<String> keywords;

			//step 2
			public void setItemId(String itemId) {
				this.itemId = itemId;
			}

			public void setName(String name) {
				this.name = name;
			}

			public void setAddress(String address) {
				this.address = address;
			}

			public void setImageUrl(String imageUrl) {
				this.imageUrl = imageUrl;
			}

			public void setUrl(String url) {
				this.url = url;
			}

			public void setKeywords(Set<String> keywords) {
				this.keywords = keywords;
			}
			//step 3: 
			public Item build(){
				//调用item class 中的的constrouctor
				return new Item(this);
			}
			
		}

}
//外部调用builder pattern的statement
//具体实践方法参考GitHubClient函数
	//ItemBuilder builder = new ItemBuilder();
	//builder.setName("abc");
	//builder.setId("1")
	//Item item= builder.build();
