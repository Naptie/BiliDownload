package me.naptie.bilidownload.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.naptie.bilidownload.Main;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class HttpManager {

	public static URLConnection readUrl(String url, String cookie, boolean post) throws IOException {
		String userAgent = UserAgentManager.getUserAgent();
		if (Main.debug) System.out.println("正在访问 " + url + "，使用 UA“" + userAgent + "”");
		URLConnection request = (new URL(url)).openConnection();
		request.setRequestProperty("User-Agent", userAgent);
		if (!cookie.equals("#"))
			request.setRequestProperty("Cookie", cookie);
		System.setProperty("http.agent", userAgent);
		request.setDoOutput(post);
		request.connect();
		return request;
	}

	public static JSONObject readJsonFromUrl(String url, String cookie) throws IOException {
		return JSON.parseObject(IOUtils.toString((InputStream) readUrl(url, cookie, false).getContent(), StandardCharsets.UTF_8));
	}
}
