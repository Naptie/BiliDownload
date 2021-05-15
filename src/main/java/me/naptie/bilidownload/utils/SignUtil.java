package me.naptie.bilidownload.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class SignUtil {

	private static final Map<String, String> KEY_SEC_MAP = new HashMap<String, String>() {{
		put("07da50c9a0bf829f", "25bdede4e1581c836cab73a48790ca6e"); // 安卓	概念版
		put("1d8b6e7d45233436", "560c52ccd288fed045859ed18bffd973"); // 安卓	客户端	一般用途
		put("178cf125136ca8ea", "34381a26236dd1171185c0beb042e1c6"); // 安卓	概念版
		put("37207f2beaebf8d7", "e988e794d4d4b6dd43bc0e89d6e90c43"); // 安卓	biliLink
		put("4409e2ce8ffd12b8", "59b43e04ad6965f34319062b478f83dd"); // TV	客户端
		put("57263273bc6b67f6", "a0488e488d1567960d3a765e8d129f90"); // 安卓	客户端
		put("5dce947fe22167f9", ""); // 安卓	必剪
		put("7d336ec01856996b", "a1ce6983bc89e20a36c37f40c4f1a0dd"); // 安卓	概念版
		put("85eb6835b0a1034e", "2ad42749773c441109bdc0191257a664"); // 未知
		put("8e16697a1b4f8121", "f5dd03b752426f2e623d7badb28d190a"); // 安卓	国际版
		put("aae92bc66f3edfab", "af125a0d5279fd576c1b4418a3e8276d"); // PC	投稿工具
		put("ae57252b0c09105d", "c75875c596a69eb55bd119e74b07cfe3"); // 安卓	国际版
		put("bb3101000e232e27", "36efcfed79309338ced0380abd824ac1"); // 安卓	国际版
		put("bca7e84c2d947ac6", "60698ba2f68e01ce44738920a0ffe768"); // 安卓	客户端	登录专用
		put("cc578d267072c94d", ""); // 安卓	轻视频
		put("cc8617fd6961e070", ""); // 安卓	漫画
		put("iVGUTjsxvpLeuDCf", "aHRmhWMLkdeMuILqORnYZocwMBpMEOdt"); // 安卓	客户端	取流专用
	}};

	/**
	 * 获取给定的参数，通过一定方式生成并返回 API 校验密匙。
	 *
	 * @param params 需要 API 校验密匙的 URL 的参数
	 *            如 appkey=4409e2ce8ffd12b8&auth_code=00021cd91f45209089ee9543b1908f01&local_id=0&ts=1621069332
	 * @return 生成的 API 校验密匙
	 */
	public static String generate(String params) {
		String appKey = "4409e2ce8ffd12b8";
		Map<String, String> paramMap = new TreeMap<>();
		for (String param : params.split("&")) {
			String[] paramKeyAndValue = param.split("=");
			paramMap.put(paramKeyAndValue[0], paramKeyAndValue[1]);
			if (paramKeyAndValue[0].equalsIgnoreCase("appkey")) {
				appKey = paramKeyAndValue[1];
			}
		}
		StringBuilder data = new StringBuilder();
		for (String key : paramMap.keySet()) {
			data.append((data.length() == 0) ? "" : "&").append(key).append("=").append(encodeURIComponent(paramMap.get(key)));
		}
		return DigestUtils.md5Hex(data + KEY_SEC_MAP.get(appKey));
	}

	/**
	 * Encodes the passed String as UTF-8 using an algorithm that's compatible
	 * with JavaScript's <code>encodeURIComponent</code> function. Returns
	 * <code>null</code> if the String is <code>null</code>.
	 *
	 * @param s The String to be encoded
	 * @return the encoded String
	 */
	public static String encodeURIComponent(String s) {
		String result;
		try {
			result = URLEncoder.encode(s, "UTF-8")
					.replaceAll("\\+", "%20")
					.replaceAll("\\%21", "!")
					.replaceAll("\\%27", "'")
					.replaceAll("\\%28", "(")
					.replaceAll("\\%29", ")")
					.replaceAll("\\%7E", "~");
		}
		// This exception should never occur.
		catch (UnsupportedEncodingException e) {
			result = s;
		}
		return result;
	}

}
