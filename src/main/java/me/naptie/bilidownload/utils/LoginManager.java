package me.naptie.bilidownload.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.naptie.bilidownload.objects.Frame;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class LoginManager {

	private static String oauthKey;
	private static File image;
	public static String sessData = "*Not_Yet_Prepared*";

	public static String showQRCode(boolean tv) throws IOException {
//		JSONObject result = tv ? HttpManager.readJsonFromUrl("http://passport.bilibili.com/x/passport-tv-login/qrcode/auth_code", "#") : HttpManager.readJsonFromUrl("http://passport.bilibili.com/qrcode/getLoginUrl", "#");
		JSONObject result = HttpManager.readJsonFromUrl("http://passport.bilibili.com/qrcode/getLoginUrl", "#");
		if (result.getIntValue("code") != 0) {
			System.out.println("无法获取二维码");
			return "";
		}
		String url = result.getJSONObject("data").getString("url");
		oauthKey = result.getJSONObject("data").getString("oauthKey");
		String imageName = "QRC" + oauthKey + ".png";
		image = new File(System.getProperty("user.dir"), imageName);
		image.deleteOnExit();
		QRCodeUtil.generateQRImage(600, 600, url, null, System.getProperty("user.dir"), imageName, "png");
		System.out.println("请使用B站 APP 扫描窗口中显示的二维码，完成扫码后请关闭窗口");
		Frame frame = new Frame("扫码登录", imageName, 600, 600);
		return oauthKey;
	}

	public static void login() {
		try {
			URLConnection request = HttpManager.readUrl("http://passport.bilibili.com/qrcode/getLoginInfo?oauthKey=" + oauthKey, "#", true);
			JSONObject result = JSON.parseObject(IOUtils.toString((InputStream) request.getContent(), StandardCharsets.UTF_8));
			if (result.getIntValue("code") != 0) {
				System.out.println("无法验证二维码");
				sessData = "";
				return;
			}
			if (result.getBoolean("status")) {
				String cookie = request.getHeaderField("Set-Cookie");
				System.out.println(cookie);
				sessData = cookie;
			} else {
				int error = result.getIntValue("data");
				switch (error) {
					case -1: System.out.println("密钥错误");
					case -2: System.out.println("密钥超时");
					case -4: System.out.println("未扫描二维码");
					case -5: System.out.println("未确认登录");
				}
				sessData = "";
			}
		} catch (IOException e) {
			e.printStackTrace();
			sessData = "";
		}
	}
}
