package me.naptie.bilidownload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.naptie.bilidownload.utils.ConfigManager;
import me.naptie.bilidownload.utils.UserAgentManager;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Main {

	private static boolean debug, hint, isFileInput;
	private static Scanner scanner;
	private static File config;
	private static long beginTime;

	public static void main(String[] args) throws IOException, InterruptedException {
		beginTime = System.currentTimeMillis();
		debug = args.length > 0 && args[0].equalsIgnoreCase("debug");
		config = new File("config.yml");
		setScanner();

		String id = getNumber();

		String cookie = login();

		JSONObject info = getVideoInfo(id, cookie);
		Object[] specified = specify(info);

		Object[] details = getResolutions(info, cookie, (int) specified[0]);

		String[] path = getPath((String) specified[1]);

		download(details, path);

		System.out.println("\n程序运行结束。总运行时间：" + getFormattedTime(System.currentTimeMillis() - beginTime));
	}

	private static void setScanner() throws FileNotFoundException {
		File input = new File("Input.txt");
		if (input.exists() && input.length() > 0) {
			scanner = new Scanner(input);
			isFileInput = true;
			hint = debug;
			System.out.println("检测到 Input.txt，已切换输入源\n");
		} else {
			scanner = new Scanner(System.in);
			isFileInput = false;
			hint = true;
		}
	}

	private static String inputStr() {
		String input = scanner.next();
		if (input.equalsIgnoreCase("*exit")) {
			System.out.println("\n程序运行结束；总运行时间：" + getFormattedTime(System.currentTimeMillis() - beginTime));
			System.exit(0);
		}
		if (debug && hint && isFileInput) System.out.println(input);
		return input;
	}

	private static int inputInt() {
		String input = scanner.next();
		if (input.equalsIgnoreCase("*exit")) {
			System.exit(0);
		}
		if (debug && hint && isFileInput) System.out.println(input);
		return Integer.parseInt(input);
	}

	private static String getNumber() {
		if (hint) System.out.println("请输入一个 AV 号或 BV 号：");
		return inputStr();
	}

	private static String login() throws IOException {
		boolean loginSuccess = false;
		String sessData, cookie = "#";
		if (config.exists()) {
			ConfigManager.init(config);
			Map<String, Object> map = ConfigManager.get();
			if (map == null)
				map = new LinkedHashMap<>();
			if (map.containsKey("sess-data")) {
				sessData = (String) map.get("sess-data");
				cookie = "SESSDATA=" + sessData + "; Path=/; Domain=bilibili.com;";
				JSONObject login = readJsonFromUrl("http://api.bilibili.com/x/web-interface/nav", cookie);
				if (login.getIntValue("code") == 0)
					if (login.getJSONObject("data").getBoolean("isLogin")) {
						if (debug)
							System.out.println("检测到配置文件，已自动填充 SESSDATA\nID：" + login.getJSONObject("data").getString("uname") + "\nUID：" + login.getJSONObject("data").getIntValue("mid"));
						loginSuccess = true;
					}
			}
		}
		while (!loginSuccess) {
			if (hint) System.out.println("\n请输入 Cookie 中 SESSDATA 的值（若无请填“#”）：");
			sessData = inputStr();
			if (sessData.equals("#")) {
				cookie = "#";
				break;
			} else {
				cookie = "SESSDATA=" + sessData + "; Path=/; Domain=bilibili.com;";
				JSONObject login = readJsonFromUrl("http://api.bilibili.com/x/web-interface/nav", cookie);
				if (login.getIntValue("code") == 0)
					if (login.getJSONObject("data").getBoolean("isLogin")) {
						loginSuccess = true;
						System.out.println("登录成功\nID：" + login.getJSONObject("data").getString("uname") + "\nUID：" + login.getJSONObject("data").getIntValue("mid"));
						if (hint) System.out.println("请决定是否保存该 SESSDATA（输入“Y”或“N”）：");
						if (inputStr().equalsIgnoreCase("Y")) {
							if (!config.exists()) config.createNewFile();
							ConfigManager.init(config);
							Map<String, Object> map = ConfigManager.get();
							if (map == null)
								map = new LinkedHashMap<>();
							map.put("sess-data", sessData);
							ConfigManager.dump(map);
							if (hint) System.out.println("已保存 SESSDATA");
						}
					} else {
						System.out.println("登录失败");
					}
				else {
					System.out.println("登录失败");
				}
			}
		}
		return cookie;
	}

	private static JSONObject getVideoInfo(String id, String cookie) throws IOException {
		System.out.println((hint ? "\n" : "") + "正在获取稿件信息······");
		JSONObject info = readJsonFromUrl("http://api.bilibili.com/x/web-interface/view?" + (id.toLowerCase().startsWith("av") ? "aid=" + id.substring(2) : "bvid=" + id), cookie);
		if (info.getIntValue("code") != 0) {
			System.out.println(info.getString("message"));
			System.out.println("\n程序运行结束，错误代码：" + info.getIntValue("code") + "；总运行时间：" + getFormattedTime(System.currentTimeMillis() - beginTime));
			System.exit(info.getIntValue("code"));
		} else {
			info = info.getJSONObject("data");
		}
		System.out.println("\n标题：" + info.getString("title"));
		System.out.println("UP主：" + info.getJSONObject("owner").getString("name"));
		System.out.println("时长：" + getFormattedTime(info.getIntValue("duration"), info.getIntValue("duration") > 3600));
		System.out.println("播放数：" + String.format("%,d", info.getJSONObject("stat").getIntValue("view")));
		System.out.println("弹幕数：" + String.format("%,d", info.getJSONObject("stat").getIntValue("danmaku")));
		System.out.println("获赞数：" + String.format("%,d", info.getJSONObject("stat").getIntValue("like")));
		System.out.println("投币数：" + String.format("%,d", info.getJSONObject("stat").getIntValue("coin")));
		System.out.println("收藏数：" + String.format("%,d", info.getJSONObject("stat").getIntValue("favorite")));
		return info;
	}

	private static Object[] specify(JSONObject info) {
		int cid;
		String name = info.getString("title");
		if (info.getIntValue("videos") > 1) {
			JSONArray pages = info.getJSONArray("pages");
			System.out.println("\n分P：");
			for (int i = 0; i < pages.size(); i++) {
				System.out.println(String.format("%3d", (i + 1)) + ". P" + String.format("%-5d", pages.getJSONObject(i).getIntValue("page")) + "CID：" + pages.getJSONObject(i).getIntValue("cid") + "  时长：" + getFormattedTime(pages.getJSONObject(i).getIntValue("duration"), pages.getJSONObject(i).getIntValue("duration") >= 3600) + "  标题：" + pages.getJSONObject(i).getString("part"));
			}
			if (hint) System.out.println("请选择分P（输入 1~" + pages.size() + " 之间的整数）：");
			int part = inputInt();
			if (part > pages.size()) {
				System.out.println("输入的数字“" + part + "”太大，已为您选择末尾的分P " + pages.getJSONObject(pages.size() - 1).getString("part"));
				part = pages.size();
			}
			if (part < 1) {
				System.out.println("输入的数字“" + part + "”太小，已为您选择开头的分P " + pages.getJSONObject(0).getString("part"));
				part = 1;
			}
			cid = pages.getJSONObject(part - 1).getIntValue("cid");
			name += " [P" + part + "] " + pages.getJSONObject(part - 1).getString("part");
		} else {
			cid = info.getIntValue("cid");
		}
		return new Object[]{cid, name};
	}

	private static Object[] getResolutions(JSONObject info, String cookie, int cid) throws IOException {
		System.out.println("\n正在获取清晰度信息······");
		String videoUrlTV = "https://api.snm0516.aisee.tv/x/tv/ugc/playurl?avid=" + info.getIntValue("aid") + "&mobi_app=android_tv_yst&fnval=16&qn=120&cid=" + cid + "&platform=android&build=103800&fnver=0";
		String videoUrlWeb = "http://api.bilibili.com/x/player/playurl?avid=" + info.getIntValue("aid") + "&cid=" + cid + "&fnval=80&fourk=1";
		JSONObject videoTV = readJsonFromUrl(videoUrlTV, cookie);
		JSONObject videoWeb = readJsonFromUrl(videoUrlWeb, cookie).getJSONObject("data");
		JSONArray qualitiesTV = videoTV.getJSONArray("accept_description");
		JSONArray qualitiesWeb = videoWeb.getJSONArray("accept_description");
		JSONArray qualities = summarize(qualitiesTV, qualitiesWeb, videoTV);
		System.out.println("\n清晰度：");
		for (int i = 1; i < qualities.size(); i++) {
			System.out.println(String.format("%3d", i) + ". " + qualities.getString(i));
		}
		if (hint) System.out.println("请选择清晰度（输入 1~" + (qualities.size() - 1) + " 之间的整数）：");
		int quality = inputInt();
		String videoDownloadUrl;
		if (qualities.getIntValue(0) == 1) {
			if (quality > qualities.size() - 1) {
				System.out.println("输入的数字“" + quality + "”太大，已为您选择最差清晰度 " + qualities.getString(qualities.size() - 1).replaceAll(" +", " "));
				quality = qualities.size() - 1;
				videoDownloadUrl = getVideoDownload(videoWeb, qualitiesWeb.size() - 1);
			} else if (quality > qualitiesTV.size())
				videoDownloadUrl = getVideoDownload(videoWeb, quality - qualitiesTV.size() - 1);
			else if (quality > 0)
				videoDownloadUrl = getVideoDownload(videoTV, quality - 1);
			else {
				System.out.println("输入的数字“" + quality + "”太小，已为您选择最佳清晰度 " + qualities.getString(1).replaceAll(" +", " "));
				quality = 1;
				videoDownloadUrl = getVideoDownload(videoTV, 0);
			}
		} else {
			if (quality > qualities.size() - 1) {
				System.out.println("输入的数字“" + quality + "”太大，已为您选择最差清晰度 " + qualities.getString(qualities.size() - 1).replaceAll(" +", " "));
				quality = qualities.size() - 1;
				videoDownloadUrl = getVideoDownload(videoWeb, qualitiesWeb.size() - 1);
			} else if (quality > 0)
				videoDownloadUrl = getVideoDownload(videoWeb, quality - 1);
			else {
				System.out.println("输入的数字“" + quality + "”太小，已为您选择最佳清晰度 " + qualities.getString(1).replaceAll(" +", " "));
				quality = 1;
				videoDownloadUrl = getVideoDownload(videoWeb, 0);
			}
		}
		return new Object[]{videoDownloadUrl, qualities, quality, videoWeb};
	}

	private static String[] getPath(String name) throws IOException {
		boolean pathSuccess = false;
		String savePath = "";
		if (config.exists()) {
			ConfigManager.init(config);
			Map<String, Object> map = ConfigManager.get();
			if (map == null)
				map = new LinkedHashMap<>();
			if (map.containsKey("save-path")) {
				File file = new File((String) map.get("save-path"));
				if (file.isDirectory()) {
					pathSuccess = true;
					savePath = file.getAbsolutePath();
					if (debug) System.out.println("\n成功获取保存路径：" + savePath);
				}
			}
		}
		while (!pathSuccess) {
			if (hint) System.out.println("\n请输入保存路径：");
			savePath = inputStr();
			File file = new File(savePath);
			if (!file.exists()) {
				if (hint) System.out.println("该目录不存在，请决定是否创建该目录（输入“Y”或“N”）：");
				if (inputStr().equalsIgnoreCase("Y")) {
					pathSuccess = file.mkdirs();
					if (!pathSuccess) System.out.println("创建目录失败");
				}
			} else {
				pathSuccess = true;
			}
			if (pathSuccess) {
				if (hint) System.out.println("请决定是否保存该保存路径（输入“Y”或“N”）：");
				if (inputStr().equalsIgnoreCase("Y")) {
					if (!config.exists()) config.createNewFile();
					ConfigManager.init(config);
					Map<String, Object> map = ConfigManager.get();
					if (map == null)
						map = new LinkedHashMap<>();
					map.put("save-path", savePath);
					ConfigManager.dump(map);
					if (hint) System.out.println("已保存该保存路径");
				}
			}
		}
		return new String[]{savePath, name.replaceAll("[/\\\\:*?<>|]", "_")};
	}

	private static void download(Object[] details, String[] path) throws IOException, InterruptedException {
		String videoDownloadUrl = (String) details[0];
		JSONArray qualities = (JSONArray) details[1];
		int quality = (int) details[2];
		JSONObject videoWeb = (JSONObject) details[3];
		if (hint) System.out.println("\n下载选项：\n  1. 视频+音频（合并需要 FFmpeg）\n  2. 仅视频\n  3. 仅音频\n请选择下载选项（输入 1~3 之间的整数）：");
		int choice = inputInt();
		if (choice > 3) {
			System.out.println("输入的数字“" + choice + "”太大，已为您选择最后一个选项 仅音频");
			choice = 3;
		}
		if (choice < 1) {
			System.out.println("输入的数字“" + choice + "”太小，已为您选择第一个选项 视频+音频（合并需要 FFmpeg）");
			choice = 1;
		}
		switch (choice) {
			case 1: {
				int ffmpegSuccess = 0;
				File ffmpeg = new File(System.getProperty("user.dir"), "null");
				if (config.exists()) {
					ConfigManager.init(config);
					Map<String, Object> map = ConfigManager.get();
					if (map == null)
						map = new LinkedHashMap<>();
					if (map.containsKey("ffmpeg-path")) {
						String ffmpegPath = (String) map.get("ffmpeg-path");
						ffmpeg = ffmpegPath.endsWith("ffmpeg.exe") ? new File(ffmpegPath) : new File(ffmpegPath, "ffmpeg.exe");
						ffmpegSuccess = ffmpeg.exists() ? 1 : 0;
						if (ffmpegSuccess == 1 && debug)
							System.out.println("\n成功获取 FFmpeg 路径：" + ffmpeg.getAbsolutePath());
					}
				}
				while (ffmpegSuccess == 0) {
					if (hint) System.out.println("\n请输入 ffmpeg.exe 目录（跳过合并请填“#”）：");
					String ffmpegPath = inputStr();
					if (ffmpegPath.equals("#")) {
						ffmpegSuccess = -1;
						break;
					}
					ffmpeg = ffmpegPath.endsWith("ffmpeg.exe") ? new File(ffmpegPath) : new File(ffmpegPath, "ffmpeg.exe");
					ffmpegSuccess = ffmpeg.exists() ? 1 : 0;
					if (ffmpegSuccess == 1) {
						if (hint) System.out.println("请决定是否保存 FFmpeg 路径（输入“Y”或“N”）：");
						if (inputStr().equalsIgnoreCase("Y")) {
							if (!config.exists()) config.createNewFile();
							ConfigManager.init(config);
							Map<String, Object> map = ConfigManager.get();
							if (map == null)
								map = new LinkedHashMap<>();
							map.put("ffmpeg-path", ffmpeg.getAbsolutePath());
							ConfigManager.dump(map);
							if (hint) System.out.println("已保存 FFmpeg 路径");
						}
					}
				}
				boolean videoSuccess, audioSuccess;
				if (videoDownloadUrl == null) {
					System.out.print("\n无法获取 " + qualities.getString(quality).replaceAll(" +", " ") + " 的视频下载地址，已为您选择目前可用的最佳清晰度 " + getQualityDescription(videoWeb, videoWeb.getJSONObject("dash").getJSONArray("video").getJSONObject(0).getIntValue("id")));
					videoDownloadUrl = videoWeb.getJSONObject("dash").getJSONArray("video").getJSONObject(0).getString("base_url");
					System.out.println("；下载地址：" + videoDownloadUrl);
				} else {
					System.out.println("\n成功获取 " + qualities.getString(quality).replaceAll(" +", " ") + " 的视频下载地址：" + videoDownloadUrl);
				}
				String audioDownloadUrl = getAudioDownload(videoWeb);
				System.out.println("\n成功获取音频下载地址：" + audioDownloadUrl);
				File video = ffmpegSuccess == -1 ? new File(path[0], path[1] + ".mp4") : new File(path[0], "tmpVid.mp4");
				File audio = ffmpegSuccess == -1 ? new File(path[0], path[1] + ".aac") : new File(path[0], "tmpAud.aac");
				System.out.println("\n正在下载视频至 " + video.getAbsolutePath());
				long lenVid = downloadFromUrl(videoDownloadUrl, video.getAbsolutePath());
				videoSuccess = video.length() == lenVid;
				System.out.println(videoSuccess ? "\n视频下载完毕" : "\n视频下载失败");
				System.out.println("\n正在下载音频至 " + audio.getAbsolutePath());
				long lenAud = downloadFromUrl(audioDownloadUrl, audio.getAbsolutePath());
				audioSuccess = audio.length() == lenAud;
				System.out.println(audioSuccess ? "\n音频下载完毕" : "\n音频下载失败");
				if (videoSuccess && audioSuccess && ffmpegSuccess == 1 && !ffmpeg.getName().equals("null")) {
					System.out.println("\n正在合并至 " + new File(path[0], path[1] + ".mp4").getAbsolutePath());
					File file = merge(ffmpeg, video, audio, new File(path[0], path[1] + ".mp4"));
					if (file != null) {
						System.out.println("合并完毕");
						video.deleteOnExit();
						audio.deleteOnExit();
					} else {
						System.out.println("合并失败");
					}
				}
				break;
			}
			case 2: {
				boolean videoSuccess;
				if (videoDownloadUrl == null) {
					System.out.print("\n无法获取 " + qualities.getString(quality).replaceAll(" +", " ") + " 的视频下载地址，已为您选择目前可用的最佳清晰度 " + getQualityDescription(videoWeb, videoWeb.getJSONObject("dash").getJSONArray("video").getJSONObject(0).getIntValue("id")));
					videoDownloadUrl = videoWeb.getJSONObject("dash").getJSONArray("video").getJSONObject(0).getString("base_url");
					System.out.println("；下载地址：" + videoDownloadUrl);
				} else {
					System.out.println("\n成功获取 " + qualities.getString(quality).replaceAll(" +", " ") + " 的视频下载地址：" + videoDownloadUrl);
				}
				File video = new File(path[0], path[1] + ".mp4");
				System.out.println("\n正在下载至 " + video.getAbsolutePath());
				long len = downloadFromUrl(videoDownloadUrl, video.getAbsolutePath());
				videoSuccess = video.length() == len;
				System.out.println(videoSuccess ? "\n下载完毕" : "\n下载失败");
				break;
			}
			case 3: {
				boolean audioSuccess;
				String audioDownloadUrl = getAudioDownload(videoWeb);
				System.out.println("\n成功获取音频下载地址：" + audioDownloadUrl);
				File audio = new File(path[0], path[1] + ".aac");
				System.out.println("\n正在下载至 " + audio.getAbsolutePath());
				long len = downloadFromUrl(audioDownloadUrl, audio.getAbsolutePath());
				audioSuccess = audio.length() == len;
				System.out.println(audioSuccess ? "\n下载完毕" : "\n下载失败");
				break;
			}
		}
	}

	private static String getFormattedTime(int time, boolean hour) {
		String result = "";
		if (hour) {
			result += time / 3600 + ":";
		}
		String min = (time % 3600) / 60 + "";
		result += (min.length() < 2 ? "0" + min : min) + ":";
		String sec = time % 60 + "";
		result += sec.length() < 2 ? "0" + sec : sec;
		return result;
	}

	private static String getFormattedTime(long time) {
		SimpleDateFormat ft = new SimpleDateFormat("mm:ss.SS");
		return ft.format(time);
	}

	private static File merge(File ffmpegExecutable, File video, File audio, File output) throws IOException, InterruptedException {
		for (File f : Arrays.asList(ffmpegExecutable, video, audio)) {
			if (!f.exists()) {
				System.out.println("指定的文件“" + f.getAbsolutePath() + "”不存在");
				return null;
			}
		}
		if (output.exists())
			//noinspection ResultOfMethodCallIgnored
			output.delete();
		ProcessBuilder builder = new ProcessBuilder(
				ffmpegExecutable.getAbsolutePath(),
				"-i", video.getAbsolutePath(),
				"-i", audio.getAbsolutePath(),
				"-vcodec", "copy",
				"-acodec", "copy",
				output.getAbsolutePath()
		);
		builder.redirectErrorStream(true);
		Process process = builder.start();
		process.waitFor();
		if (!output.exists()) {
			return null;
		}
		return output;
	}

	private static JSONArray summarize(JSONArray qualitiesTV, JSONArray qualitiesWeb, JSONObject videoTV) {
		boolean watermark = true;
		JSONArray watermarks = videoTV.getJSONArray("accept_watermark");
		for (int i = 0; i < watermarks.size(); i++) {
			if (!watermarks.getBoolean(i)) {
				watermark = false;
				break;
			}
		}
		JSONArray qualities = new JSONArray();
		if (!watermark) {
			qualities.add(1);
			for (int i = 0; i < qualitiesTV.size(); i++)
				if (!videoTV.getJSONArray("accept_watermark").getBoolean(i))
					qualities.add(String.format("%-11s", qualitiesTV.getString(i)) + watermark(videoTV.getJSONArray("accept_watermark").getBoolean(i)));
				else
					qualities.add(qualitiesTV.getString(i));
		} else {
			qualities.add(0);
		}
		for (int i = 0; i < qualitiesWeb.size(); i++)
			qualities.add(qualitiesWeb.getString(i));
		return qualities;
	}

	private static String getQualityDescription(JSONObject video, int qNum) {
		JSONArray descriptions = video.getJSONArray("accept_description");
		JSONArray qualityNums = video.getJSONArray("accept_quality");
		for (int i = 0; i < qualityNums.size(); i++) {
			if (qualityNums.getIntValue(i) == qNum) {
				return descriptions.getString(i);
			}
		}
		return null;
	}

	private static String getVideoDownload(JSONObject video, int quality) {
		int qualityNum = video.getJSONArray("accept_quality").getIntValue(quality);
		JSONArray videos = video.getJSONObject("dash").getJSONArray("video");
		for (int i = 0; i < videos.size(); i++) {
			if (videos.getJSONObject(i).getIntValue("id") == qualityNum) {
				return videos.getJSONObject(i).getString("base_url");
			}
		}
		return null;
	}

	private static String getAudioDownload(JSONObject video) {
		JSONArray audios = video.getJSONObject("dash").getJSONArray("audio");
		return audios.getJSONObject(0).getString("base_url");
	}

	private static String watermark(boolean availability) {
		if (availability) return "";
		else return "无水印";
	}

	private static URLConnection readUrl(String url, String cookie) throws IOException {
		String userAgent = UserAgentManager.getUserAgent();
		if (debug) System.out.println("正在访问 " + url + "，使用 UA“" + userAgent + "”");
		URLConnection request = (new URL(url)).openConnection();
		request.setRequestProperty("User-Agent", userAgent);
		if (!cookie.equals("#"))
			request.setRequestProperty("Cookie", cookie);
		System.setProperty("http.agent", userAgent);
		request.connect();
		return request;
	}

	private static JSONObject readJsonFromUrl(String url, String cookie) throws IOException {
		return JSON.parseObject(IOUtils.toString((InputStream) readUrl(url, cookie).getContent(), StandardCharsets.UTF_8));
	}

	private static long downloadFromUrl(String address, String path) {
		long beginTime = System.currentTimeMillis();
		int byteRead;
		URL url;
		try {
			url = new URL(address);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return -1;
		}
		try {
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Referer", "https://www.bilibili.com");
			InputStream inStream = conn.getInputStream();
			FileOutputStream fs = new FileOutputStream(path);

			byte[] buffer = new byte[1024];
			StringBuilder progress = new StringBuilder();
			double total = conn.getContentLengthLong() / 1024.0 / 1024.0;
			System.out.print("进度：");
			while ((byteRead = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteRead);
				int lastByteLength = progress.toString().getBytes().length;
				int lastLength = progress.length();
				if (progress.length() > 0) {
					for (int i = 0; i < lastByteLength; i++)
						System.out.print("\b");
				}
				double downloaded = fs.getChannel().size() / 1024.0 / 1024.0;
				double speed = ((System.currentTimeMillis() - beginTime) / 1000.0 == 0) ? 0 : downloaded / ((System.currentTimeMillis() - beginTime) / 1000.0);
				progress = new StringBuilder(String.format("%.2f", (fs.getChannel().size() * 100.0 / conn.getContentLengthLong())) + "%（" + String.format("%,.3f", downloaded) + "MB / " + String.format("%,.3f", total) + "MB）；速度：" + String.format("%,.3f", speed) + "MB/s；剩余时间：" + String.format("%,.3f", (total - downloaded) / speed) + "s");
				for (int i = 0; i <= lastLength - progress.length(); i++)
					progress.append(" ");
				System.out.print(progress);
			}
			System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
			String timeSpent = "用时：" + String.format("%,.3f", (System.currentTimeMillis() - beginTime) / 1000.0) + "s";
			System.out.print(timeSpent);
			for (int i = 0; i < 13 - timeSpent.length(); i++)
				System.out.print(" ");
			inStream.close();
			fs.close();
			return conn.getContentLengthLong();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

}
