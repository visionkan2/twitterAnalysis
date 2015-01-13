import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class parserTest {
	private List<String> cookies;
	private HttpsURLConnection conn;

	private final String USER_AGENT = "Mozilla/5.0";
	private static final String USER_NAME = "zzzzddddgtuwup@gmail.com";
	private static final String PASS_WORD = "ggggddddzzzz1234";

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

	public boolean testIfLogin(String html) {
		Document doc = Jsoup.parse(html);
		Elements head = doc.getElementsByTag("head");
		Elements title = head.get(0).getElementsByTag("title");
		if (title.get(0).text().equals("Login on Twitter")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean testGetJson(String html) {
		return html.charAt(0) == '{';
	}

	public String getFormParams(String html, String username, String password)
			throws UnsupportedEncodingException {

		// System.out.println("Extracting form's data...");

		Document doc = Jsoup.parse(html);

		// Google form id
		Elements loginform = doc.getElementsByClass("js-signin");
		Elements inputElements = loginform.get(0).getElementsByTag("input");
		Map<String, String> map = new HashMap<String, String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			if (key.equals("session[username_or_email]"))
				value = username;
			else if (key.equals("session[password]"))
				value = password;
			map.put(URLEncoder.encode(key, "UTF-8"),
					URLEncoder.encode(value, "UTF-8"));
		}
		// build parameters list
		StringBuilder result = new StringBuilder();
		result.append("session%5Busername_or_email%5D=")
				.append(map.get("session%5Busername_or_email%5D")).append('&')
				.append("session%5Bpassword%5D=")
				.append(map.get("session%5Bpassword%5D")).append('&')
				.append("authenticity_token=")
				.append(map.get("authenticity_token")).append('&')
				.append("scribe_log=").append(map.get("scribe_log"))
				.append('&').append("redirect_after_login=")
				.append(map.get("redirect_after_login")).append('&')
				.append("authenticity_token=")
				.append(map.get("authenticity_token"));

		return result.toString();
	}

	private String GetPageContent(String url) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// default is GET
		conn.setRequestMethod("GET");

		conn.setUseCaches(false);

		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				// System.out.println("cookie is " + cookie.split(";", 1)[0]);
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		} else {
			// System.out.println("cookie is empty");
		}
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
		System.out.println("==============================");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Get the response cookies
		setCookies(conn.getHeaderFields().get("set-cookie"));
		return response.toString();
	}

	private String sendPost(String url, String postParams, String referer)
			throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Host", "twitter.com");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language",
				"en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		for (String cookie : this.cookies) {
			// System.out.println("post cookie is " + cookie.split(";", 1)[0]);
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
		// conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("origin", "https://twitter.com");
		// conn.setRequestProperty("Referer", "https://twitter.com/login?"+
		// refererSuffix);
		conn.setRequestProperty("Referer", referer);
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length",
				Integer.toString(postParams.length()));

		conn.setDoOutput(true);
		conn.setDoInput(true);
		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		// System.out.println("header is "+ conn.getHeaderFields());
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);
		System.out.println("==============================");

		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		setCookies(conn.getHeaderFields().get("set-cookie"));
		return response.toString();
	}

	public void login() throws Exception {
		CookieHandler.setDefault(new CookieManager());
		String url = "https://twitter.com/login";
		String html = GetPageContent(url);
		String paramsList = getFormParams(html, USER_NAME, PASS_WORD);
		html = sendPost("https://twitter.com/sessions", paramsList, url);
		// System.out.println(html);
	}

	@Deprecated
	public List<String> getFollowersfromSpider(String currentUser)
			throws Exception {
		CookieHandler.setDefault(new CookieManager());
		String url = "https://twitter.com/" + currentUser + "/followers";
		String html = GetPageContent(url);
		if (testIfLogin(html)) {
			// if need login
			String paramsList = getFormParams(html, USER_NAME, PASS_WORD);
			String[] paramsArray = paramsList.split("&");
			String refererSuffix = null;
			for (String s : paramsArray) {
				if (s.contains("redirect_after_login")) {
					refererSuffix = s;
				}
			}
			String referer = "https://twitter.com/login?" + refererSuffix;
			html = sendPost("https://twitter.com/sessions", paramsList, referer);
		}
		return getFollowersFromHtml(html);
	}

	public List<String> getFollowersFromHtml(String html) {
		Document doc = Jsoup.parse(html);
		Elements timeline = doc.getElementsByClass("GridTimeline");
		Elements screenNames = timeline.get(0)
				.getElementsByClass("ProfileCard");

		List<String> resultList = new ArrayList<>();
		for (Element e : screenNames) {
			resultList.add(e.attr("data-screen-name"));
		}
		return resultList;
	}

	public List<String> getFollowersFromJsonHtml(String html) {
		Document doc = Jsoup.parse(html);
		Elements screenNames = doc.getElementsByClass("ProfileCard");
		List<String> resultList = new ArrayList<>();
		for (Element e : screenNames) {
			resultList.add(e.attr("data-screen-name"));
		}
		return resultList;
	}

	public String sendAjaxRequest(String url) throws Exception {
		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// default is GET
		conn.setRequestMethod("GET");
		conn.setUseCaches(false);

		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept",
				"application/json, text/javascript, */*; q=0.01");
		conn.setRequestProperty("Accept-Language",
				"en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				// System.out.println("cookie is " + cookie.split(";", 1)[0]);
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
//		int responseCode = conn.getResponseCode();
//		System.out.println("\nSending ajax request to URL : " + url);
//		System.out.println("Response Code : " + responseCode);
//		System.out.println("==============================");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Get the response cookies
		setCookies(conn.getHeaderFields().get("set-cookie"));
		return response.toString();
	}
	
	public List<String> getFollowers(String currentUser) throws Exception {
		return twitterAjaxDBInterface(currentUser, "followers");
	}
	
	public List<String> getFollowing(String currentUser) throws Exception{
		return twitterAjaxDBInterface(currentUser, "following");
	}
	
	public List<String> twitterAjaxDBInterface(String currentUser, String instruction) throws Exception {
		String cursor = "-1";
		boolean hasMoreItems = true;
		List<String> result = new ArrayList<>();

		while (hasMoreItems) {
			String url = "https://twitter.com/"
					+ currentUser
					+ "/"+instruction+"/users?cursor="
					+ cursor
					+ "&cursor_index=&cursor_offset=&include_available_features=1&include_entities=1&is_forward=true";
			String response = sendAjaxRequest(url);
			if (testGetJson(response)) {
				JSONObject jsonObject = new JSONObject(response);		
				String html = jsonObject.getString("items_html");
				cursor = jsonObject.getString("cursor");
				hasMoreItems = jsonObject.getBoolean("has_more_items");
				result.addAll(getFollowersFromJsonHtml(html));
			} else {
				login();
			}
		}
		return result;
	}

	public static void main(String[] args) throws Exception {
		parserTest test = new parserTest();
		CookieHandler.setDefault(new CookieManager());
//		long start = System.currentTimeMillis();
		List<String> followersList = test.getFollowers("BobbyMooreBFK");
//		long end = System.currentTimeMillis();
		System.out.println(followersList.size());
		System.out.println(followersList);
		
		List<String> followingList = test.getFollowing("BobbyMooreBFK");
		System.out.println(followingList.size());
//		System.out.println(end-start);
	}
}
