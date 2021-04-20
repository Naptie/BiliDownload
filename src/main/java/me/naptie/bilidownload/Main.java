package me.naptie.bilidownload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.naptie.bilidownload.utils.UserAgentManager;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

	private static boolean debug;

	public static void main(String[] args) throws IOException, InterruptedException {
		long beginTime = System.currentTimeMillis();
		debug = args.length > 0 && args[0].equalsIgnoreCase("debug");
		File input = new File("Input.txt");
		Scanner scanner;
		if (input.exists() && input.length() > 0) {
			scanner = new Scanner(input);
			System.out.println("检测到 Input.txt，已切换输入源\n");
		} else {
			scanner = new Scanner(System.in);
		}
		System.out.println("请输入一个 AV 号或 BV 号：");
		String id = scanner.next();
		System.out.println("\n请输入 Cookie 中 SESSDATA 的值（若无请填“#”）：");
		String sessData = scanner.next();
		String infoUrl = "http://api.bilibili.com/x/web-interface/view?" + (id.toLowerCase().startsWith("av") ? "aid=" + id.substring(2) : "bvid=" + id);
		String cookie = sessData.equals("#") ? "#" : "SESSDATA=" + sessData + "; Path=/; Domain=bilibili.com;";
		System.out.println("\n正在获取稿件信息······");
		JSONObject info = readJsonFromUrl(infoUrl, cookie);
		if (info.getIntValue("code") != 0) {
			System.out.println(info.getString("message"));
			return;
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
		int videos = info.getIntValue("videos");
		int cid;
		String name = info.getString("title");
		if (videos > 1) {
			JSONArray pages = info.getJSONArray("pages");
			System.out.println("\n分P：");
			for (int i = 0; i < pages.size(); i++) {
				System.out.println(String.format("%3d", (i + 1)) + ". P" + String.format("%-5d", pages.getJSONObject(i).getIntValue("page")) + "CID：" + pages.getJSONObject(i).getIntValue("cid") + "  时长：" + getFormattedTime(pages.getJSONObject(i).getIntValue("duration"), pages.getJSONObject(i).getIntValue("duration") >= 3600) + "  标题：" + pages.getJSONObject(i).getString("part"));
			}
			System.out.println("请选择分P（输入 1~" + pages.size() + " 之间的整数）：");
			int part = scanner.nextInt();
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
		System.out.println("\n正在获取清晰度信息······");
		String videoUrl1 = "https://api.snm0516.aisee.tv/x/tv/ugc/playurl?avid=" + info.getIntValue("aid") + "&mobi_app=android_tv_yst&fnval=16&qn=120&cid=" + cid + "&platform=android&build=103800&fnver=0";
		String videoUrl2 = "http://api.bilibili.com/x/player/playurl?avid=" + info.getIntValue("aid") + "&cid=" + cid + "&fnval=80&fourk=1";
		JSONObject video1 = readJsonFromUrl(videoUrl1, cookie);
		JSONObject video2 = readJsonFromUrl(videoUrl2, cookie).getJSONObject("data");
		JSONArray qualities1 = video1.getJSONArray("accept_description");
		JSONArray qualities2 = video2.getJSONArray("accept_description");
		JSONArray qualities = summary(qualities1, qualities2, video1);
		System.out.println("\n清晰度：");
		for (int i = 1; i < qualities.size(); i++) {
			System.out.println(String.format("%3d", i) + ". " + qualities.getString(i));
		}
		System.out.println("请选择清晰度（输入 1~" + (qualities.size() - 1) + " 之间的整数）：");
		int quality = scanner.nextInt();
		String videoDownloadUrl;
		if (qualities.getIntValue(0) == 1) {
			if (quality > qualities.size() - 1) {
				System.out.println("输入的数字“" + quality + "”太大，已为您选择最差清晰度 " + qualities.getString(qualities.size() - 1).replaceAll(" +", " "));
				quality = qualities.size() - 1;
				videoDownloadUrl = getVideoDownload(video2, qualities2.size() - 1);
			} else if (quality > qualities1.size())
				videoDownloadUrl = getVideoDownload(video2, quality - qualities1.size() - 1);
			else if (quality > 0)
				videoDownloadUrl = getVideoDownload(video1, quality - 1);
			else {
				System.out.println("输入的数字“" + quality + "”太小，已为您选择最佳清晰度 " + qualities.getString(1).replaceAll(" +", " "));
				quality = 1;
				videoDownloadUrl = getVideoDownload(video1, 0);
			}
		} else {
			if (quality > qualities.size() - 1) {
				System.out.println("输入的数字“" + quality + "”太大，已为您选择最差清晰度 " + qualities.getString(qualities.size() - 1).replaceAll(" +", " "));
				quality = qualities.size() - 1;
				videoDownloadUrl = getVideoDownload(video2, qualities2.size() - 1);
			} else if (quality > 0)
				videoDownloadUrl = getVideoDownload(video2, quality - 1);
			else {
				System.out.println("输入的数字“" + quality + "”太小，已为您选择最佳清晰度 " + qualities.getString(1).replaceAll(" +", " "));
				quality = 1;
				videoDownloadUrl = getVideoDownload(video2, 0);
			}
		}
		System.out.println("\n请输入保存目录：");
		Path path = Paths.get(scanner.next(), name.replaceAll("[/\\\\:*?<>|]", "_"));
		System.out.println("\n下载选项：\n  1. 视频+音频（合并需要 FFmpeg）\n  2. 仅视频\n  3. 仅音频\n请选择下载选项（输入 1~3 之间的整数）：");
		int choice = scanner.nextInt();
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
				System.out.println("\n请输入 ffmpeg.exe 路径（跳过合并请填“#”）：");
				String ffmpeg = scanner.next();
				boolean videoSuccess, audioSuccess;
				System.out.println();
				if (videoDownloadUrl == null) {
					System.out.print("\n无法获取 " + qualities.getString(quality).replaceAll(" +", " ") + " 的视频下载地址，已为您选择最佳清晰度 " + getQualityDescription(video2, video2.getJSONObject("dash").getJSONArray("video").getJSONObject(0).getIntValue("id")));
					videoDownloadUrl = video2.getJSONObject("dash").getJSONArray("video").getJSONObject(0).getString("base_url");
					System.out.println("，下载地址：" + videoDownloadUrl);
				} else {
					System.out.println("\n成功获取 " + qualities.getString(quality).replaceAll(" +", " ") + " 的视频下载地址：" + videoDownloadUrl);
				}
				String audioDownloadUrl = getAudioDownload(video2);
				System.out.println("\n成功获取音频下载地址：" + audioDownloadUrl);
				File video = ffmpeg.equals("#") ? new File(path + ".mp4") : new File(System.getProperty("user.dir"), "tmpVid.mp4");
				File audio = ffmpeg.equals("#") ? new File(path + ".aac") : new File(System.getProperty("user.dir"), "tmpAud.aac");
				System.out.println("\n正在下载视频至 " + video.getAbsolutePath());
				long lenVid = download(videoDownloadUrl, video.getAbsolutePath());
				videoSuccess = video.length() == lenVid;
				System.out.println(videoSuccess ? "\n视频下载完毕" : "\n视频下载失败");
				System.out.println("\n正在下载音频至 " + audio.getAbsolutePath());
				long lenAud = download(audioDownloadUrl, audio.getAbsolutePath());
				audioSuccess = audio.length() == lenAud;
				System.out.println(audioSuccess ? "\n音频下载完毕" : "\n音频下载失败");
				if (videoSuccess && audioSuccess && !ffmpeg.equals("#")) {
					System.out.println("\n正在合并至 " + path + ".mp4");
					File file = merge(ffmpeg.endsWith("ffmpeg.exe") ? new File(ffmpeg) : new File(ffmpeg, "ffmpeg.exe"), video, audio, new File(path + ".mp4"));
					if (file != null) {
						System.out.println("\n合并完毕");
						video.deleteOnExit();
						audio.deleteOnExit();
					} else {
						System.out.println("\n合并失败");
					}
				}
				break;
			}
			case 2: {
				boolean videoSuccess;
				if (videoDownloadUrl == null) {
					System.out.print("\n无法获取 " + qualities.getString(quality).replaceAll(" +", " ") + " 的视频下载地址，已为您选择最佳清晰度 " + getQualityDescription(video2, video2.getJSONObject("dash").getJSONArray("video").getJSONObject(0).getIntValue("id")));
					videoDownloadUrl = video2.getJSONObject("dash").getJSONArray("video").getJSONObject(0).getString("base_url");
					System.out.println("，下载地址：" + videoDownloadUrl);
				} else {
					System.out.println("\n成功获取 " + qualities.getString(quality).replaceAll(" +", " ") + " 的视频下载地址：" + videoDownloadUrl);
				}
				File video = new File(path + ".mp4");
				System.out.println("\n正在下载至 " + video.getAbsolutePath());
				long len = download(videoDownloadUrl, video.getAbsolutePath());
				videoSuccess = video.length() == len;
				System.out.println(videoSuccess ? "\n下载完毕" : "\n下载失败");
				break;
			}
			case 3: {
				boolean audioSuccess;
				String audioDownloadUrl = getAudioDownload(video2);
				System.out.println("\n成功获取音频下载地址：" + audioDownloadUrl);
				File audio = new File(path + ".aac");
				System.out.println("\n正在下载至 " + audio.getAbsolutePath());
				long len = download(audioDownloadUrl, audio.getAbsolutePath());
				audioSuccess = audio.length() == len;
				System.out.println(audioSuccess ? "\n下载完毕" : "\n下载失败");
				break;
			}
		}
		System.out.println("\n程序运行结束，总耗时 " + getFormattedTime(System.currentTimeMillis() - beginTime, "mm:ss.SS"));
	}

	private static String getFormattedTime(int time, boolean hour) {
		String result = "";
		if (hour) {
			result += time / 3600 + ":";
		}
		String min = (time % 3600) / 60 + "";
		result += (min.length() < 2 ? "0" + min : min) + ":";
		String sec = time %  60 + "";
		result += sec.length() < 2 ? "0" + sec : sec;
		return result;
	}

	private static String getFormattedTime(long time, String format) {
		SimpleDateFormat ft = new SimpleDateFormat(format);
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
			output.delete();
		ProcessBuilder pb = new ProcessBuilder(
				ffmpegExecutable.getAbsolutePath(),
				"-i", video.getAbsolutePath(),
				"-i", audio.getAbsolutePath(),
				"-vcodec", "copy",
				"-acodec", "copy",
				output.getAbsolutePath()
		);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();
		if (!output.exists()) {
			return null;
		}
		return output;
	}

	private static JSONArray summary(JSONArray qualities1, JSONArray qualities2, JSONObject video1) {
		boolean watermark = true;
		JSONArray watermarks = video1.getJSONArray("accept_watermark");
		for (int i = 0; i < watermarks.size(); i++) {
			if (!watermarks.getBoolean(i)) {
				watermark = false;
				break;
			}
		}
		JSONArray qualities = new JSONArray();
		if (!watermark) {
			qualities.add(1);
			for (int i = 0; i < qualities1.size(); i++)
				if (!video1.getJSONArray("accept_watermark").getBoolean(i))
					qualities.add(String.format("%-11s", qualities1.getString(i)) + watermark(video1.getJSONArray("accept_watermark").getBoolean(i)));
				else
					qualities.add(qualities1.getString(i));
		} else {
			qualities.add(0);
		}
		for (int i = 0; i < qualities2.size(); i++)
			qualities.add(qualities2.getString(i));
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

	private static long download(String address, String path) {
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
			String progress = "";
			System.out.print("进度：");
			while ((byteRead = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteRead);
				if (!progress.isEmpty())
					for (int i = 0; i < progress.length(); i++)
						System.out.print("\b");
				progress = String.format("%.2f", (fs.getChannel().size() * 100.0 / conn.getContentLengthLong())) + "% (" + String.format("%,f", Math.round(fs.getChannel().size() / 1024.0) / 1024.0) + "MB / " + String.format("%,f", Math.round(conn.getContentLengthLong() / 1024.0) / 1024.0) + "MB)";
				System.out.print(progress);
			}
			System.out.println();
			inStream.close();
			fs.close();
			return conn.getContentLengthLong();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

}
