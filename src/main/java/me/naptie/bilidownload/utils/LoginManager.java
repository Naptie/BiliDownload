package me.naptie.bilidownload.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.naptie.bilidownload.Main;
import me.naptie.bilidownload.objects.Frame;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginManager {

	public static String sessData, auth, accessToken;
	private static Map<String, List<String>> headers = new HashMap<>();
	private static JSONObject result = new JSONObject();

	public static void showQRCodeFromWeb() throws IOException {
		sessData = "*SessData_Not_Yet_Prepared*";
		JSONObject result = HttpManager.readJsonFromUrl("https://passport.bilibili.com/qrcode/getLoginUrl", "#", false);
		if (result.getIntValue("code") != 0) {
			System.out.println("无法获取二维码");
			sessData = "";
			return;
		}
		showQRCode(result, false);
	}

	public static void showQRCodeFromTV() throws IOException {
		accessToken = "*Token_Not_Yet_Prepared*";
		String params = "appkey=4409e2ce8ffd12b8&local_id=0&ts=" + System.currentTimeMillis();
		JSONObject result = JSON.parseObject(IOUtils.toString((InputStream) HttpManager.readUrl("https://passport.bilibili.com/x/passport-tv-login/qrcode/auth_code?" + params + "&sign=" + SignUtil.generate(params), "#", true, true).getContent(), StandardCharsets.UTF_8));
		if (result.getIntValue("code") != 0) {
			System.out.println("无法获取二维码；错误代码：" + result.getIntValue("code") + "，错误信息：" + result.getString("message"));
			accessToken = "";
			return;
		}
		showQRCode(result, true);
	}

	private static void showQRCode(JSONObject result, boolean tv) throws IOException {
		String platform = tv ? "TV" : "WEB";
		String url = result.getJSONObject("data").getString("url");
		auth = tv ? result.getJSONObject("data").getString("auth_code") : result.getJSONObject("data").getString("oauthKey");
		String imageName = "QRC" + platform + auth.substring(0, 8) + ".png";
		File image = new File(System.getProperty("user.dir"), imageName);
		image.deleteOnExit();
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		if (size.width >= 500 && size.height >= 500) {
			size.setSize(500, 500);
		} else {
			if (size.width > size.height) {
				//noinspection SuspiciousNameCombination
				size.setSize(size.height, size.height);
			} else {
				//noinspection SuspiciousNameCombination
				size.setSize(size.width, size.width);
			}
		}
		QRCodeUtil.generateQRImage(size, url, null, System.getProperty("user.dir"), imageName, "png");
		System.out.println();
		if (Main.debug) System.out.println(platform + " 端二维码登录所用的密钥为 " + auth);
		System.out.println("请使用B站手机客户端扫描窗口中显示的二维码并确认登录");
		Frame frame = new Frame(platform + " 端二维码登录", imageName, size, tv);
		long begin = System.currentTimeMillis();
		long now;
		if (tv) {
			while (true) {
				now = System.currentTimeMillis();
				if (now - begin > 1000) {
					begin = now;
					if (detectIfTVScanCompletes()) {
						loginTV();
						frame.dispose();
						break;
					}
				}
			}
		} else {
			while (true) {
				now = System.currentTimeMillis();
				if (now - begin > 1000) {
					begin = now;
					if (detectIfWebScanCompletes()) {
						loginWeb();
						frame.dispose();
						break;
					}
				}
			}
		}
	}

	private static boolean detectIfWebScanCompletes() throws IOException {
		URLConnection request = HttpManager.readUrl("https://passport.bilibili.com/qrcode/getLoginInfo?oauthKey=" + auth, "#", true, false);
		result = JSON.parseObject(IOUtils.toString((InputStream) request.getContent(), StandardCharsets.UTF_8));
		if (result.getIntValue("code") == 0) {
			if (result.getBoolean("status")) {
				headers = request.getHeaderFields();
				return true;
			}
		}
		return false;
	}

	private static boolean detectIfTVScanCompletes() throws IOException {
		String params = "appkey=4409e2ce8ffd12b8&auth_code=" + auth + "&local_id=0&ts=" + System.currentTimeMillis();
		URLConnection request = HttpManager.readUrl("https://passport.bilibili.com/x/passport-tv-login/qrcode/poll?" + params + "&sign=" + SignUtil.generate(params), "#", true, true);
		result = JSON.parseObject(IOUtils.toString((InputStream) request.getContent(), StandardCharsets.UTF_8));
		if (result.getIntValue("code") == 0) {
			headers = request.getHeaderFields();
			return true;
		}
		return false;
	}

	public static void loginWeb() {
		if (headers.isEmpty() || result.isEmpty()) {
			try {
				URLConnection request = HttpManager.readUrl("https://passport.bilibili.com/qrcode/getLoginInfo?oauthKey=" + auth, "#", true, false);
				headers = request.getHeaderFields();
				result = JSON.parseObject(IOUtils.toString((InputStream) request.getContent(), StandardCharsets.UTF_8));
			} catch (IOException e) {
				e.printStackTrace();
				sessData = "";
				return;
			}
		}
		if (result.getIntValue("code") != 0) {
			System.out.println("无法验证二维码；错误代码：" + result.getIntValue("code") + "，错误信息：" + result.getString("message"));
			sessData = "";
			return;
		}
		if (result.getBoolean("status")) {
			for (Map.Entry<String, List<String>> header : headers.entrySet()) {
				if (!sessData.equalsIgnoreCase("*SessData_Not_Yet_Prepared*"))
					break;
				for (String string : header.getValue()) {
					if (string.contains("SESSDATA=")) {
						sessData = string.substring(string.indexOf("SESSDATA=") + 9, string.indexOf("SESSDATA=") + 41);
						break;
					}
				}
			}
		} else {
			int error = result.getIntValue("data");
			switch (error) {
				case -1:
					System.out.println("密钥错误");
				case -2:
					System.out.println("密钥超时");
				case -4:
					System.out.println("未扫描二维码");
				case -5:
					System.out.println("未确认登录");
			}
			sessData = "";
		}
		headers = new HashMap<>();
		result = new JSONObject();
	}

	public static void loginTV() {
		if (headers.isEmpty() || result.isEmpty()) {
			try {
				String params = "appkey=4409e2ce8ffd12b8&auth_code=" + auth + "&local_id=0&ts=" + System.currentTimeMillis();
				URLConnection request = HttpManager.readUrl("https://passport.bilibili.com/x/passport-tv-login/qrcode/poll?" + params + "&sign=" + SignUtil.generate(params), "#", true, true);
				headers = request.getHeaderFields();
				result = JSON.parseObject(IOUtils.toString((InputStream) request.getContent(), StandardCharsets.UTF_8));
			} catch (IOException e) {
				e.printStackTrace();
				accessToken = "";
				return;
			}
		}
		if (result.getIntValue("code") != 0) {
			System.out.println("无法验证二维码；错误代码：" + result.getIntValue("code") + "，错误信息：" + result.getString("message"));
			accessToken = "";
			return;
		}
		accessToken = result.getJSONObject("data").getString("access_token");
		headers = new HashMap<>();
		result = new JSONObject();
	}
}
