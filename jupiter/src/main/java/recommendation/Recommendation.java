package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.MySqlConnection;
import entity.Item;
import external.GitHubClient;

public class Recommendation {

	public List<Item> recommendItems(String userId, double lat, double lon) {
		//creat a list to put recommendataion item
		List<Item> recommendedItems = new ArrayList<>();

		// Step 1, get all favorited itemids(收藏的工作）
		MySqlConnection connection = new MySqlConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);

		// Step 2, get all keywords, sort by count
		//count出现过几次
		// {"software engineer": 6, "backend": 4, "san francisco": 3, "remote": 1}
		Map<String, Integer> allKeywords = new HashMap<>();
		for (String itemId : favoritedItemIds) {
			Set<String> keywords = connection.getKeywords(itemId);
			for (String keyword : keywords) {
				//更新原有的map或直接是0(default)
				allKeywords.put(keyword, allKeywords.getOrDefault(keyword, 0) + 1);
			}
		}
		connection.close();

		List<Entry<String, Integer>> keywordList = new ArrayList<>(allKeywords.entrySet());
		Collections.sort(keywordList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
			return Integer.compare(e2.getValue(), e1.getValue());
		});

		// 只参考前3个favorite
		if (keywordList.size() > 3) {
			keywordList = keywordList.subList(0, 3);
		}

		// Step 3, search based on keywords, filter out favorite items
		Set<String> visitedItemIds = new HashSet<>();
		GitHubClient client = new GitHubClient();

		for (Entry<String, Integer> keyword : keywordList) {
			List<Item> items = client.search(lat, lon, keyword.getKey());

			for (Item item : items) {
				//favoritedItemIds:已经收藏过的就不推荐的
				// vistedItem:已经推荐过的
				if (!favoritedItemIds.contains(item.getItemId()) && !visitedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item);
					visitedItemIds.add(item.getItemId());
				}
			}
		}
		return recommendedItems;
	}

}
