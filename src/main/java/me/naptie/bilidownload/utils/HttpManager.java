package me.naptie.bilidownload.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.naptie.bilidownload.Main;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpManager {

	public static HttpURLConnection readUrl(String url, String cookie, boolean post, boolean tv) throws IOException {
		return readUrl(url, cookie, null, post, tv);
	}

	public static HttpURLConnection readUrl(String url, String cookie, String userAgent, boolean post, boolean tv) throws IOException {
		if (userAgent == null) {
			userAgent = tv ? UserAgentManager.getTVUserAgent() : UserAgentManager.getUserAgent();
		}
		if (Main.debug) System.out.println("正在访问 " + url + "，使用 UA“" + userAgent + "”");
		HttpURLConnection request = (HttpURLConnection) (new URL(url)).openConnection();
		request.setRequestProperty("User-Agent", userAgent);
		if (!cookie.equals("#")) {
			request.setRequestProperty("Cookie", cookie);
		}
		System.setProperty("http.agent", userAgent);
		request.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
		request.setRequestProperty("Accept", "*/*");
		if (post) {
			request.setRequestMethod("POST");
		}
		request.connect();
		if (request.getResponseCode() != 200) {
			throw new IOException("服务器返回状态码 " + request.getResponseCode());
		}
		return request;
	}

	public static JSONObject readJsonFromUrl(String url, String cookie, boolean tv) throws IOException {
		return JSON.parseObject(IOUtils.toString((InputStream) readUrl(url, cookie, false, tv).getContent(), StandardCharsets.UTF_8));
	}

	public static JSONObject readJsonFromUrl(String url, String cookie, String userAgent, boolean tv) throws IOException {
		return JSON.parseObject(IOUtils.toString((InputStream) readUrl(url, cookie, userAgent, false, tv).getContent(), StandardCharsets.UTF_8));
	}
}
