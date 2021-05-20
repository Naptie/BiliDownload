package me.naptie.bilidownload.objects;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// 原帖：https://blog.csdn.net/fzy629442466/article/details/85601315
public class Downloader {
	private static final int THREAD_AMOUNT = 32;
	private final URL url;
	private final File file;
	private final List<DownloadThread> THREADS = new ArrayList<>();
	private long threadLen;

	public Downloader(String address, String path) throws IOException {
		url = new URL(address);
		file = new File(path);
	}

	public long download(long totalLen) throws IOException {
		File disk = Paths.get(file.getAbsolutePath()).getRoot().toFile().getAbsoluteFile();
		if (disk.getUsableSpace() < totalLen) {
			return -1;
		}
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Referer", "https://www.bilibili.com");
		conn.setConnectTimeout(5000);
//		while (conn.getContentLength() == -1) {
//			conn.connect();
//			System.out.println("正在获取文件大小······");
//		}
//		int totalLen = conn.getContentLength();                                     // 获取文件长度
		threadLen = (totalLen + THREAD_AMOUNT - 1) / THREAD_AMOUNT;                 // 计算每个线程要下载的长度
		RandomAccessFile raf = new RandomAccessFile(file, "rws");             // 在本地创建一个和服务端大小相同的文件
		raf.setLength(totalLen);                                                    // 设置文件的大小
		raf.close();

		for (int i = 0; i < THREAD_AMOUNT; i++) {                                   // 开启8条线程, 每个线程下载一部分数据到本地文件中
			DownloadThread thread = new DownloadThread(i);
			THREADS.add(thread);
			thread.start();
		}
		return conn.getContentLengthLong();
	}

	public long getDownloaded() {
		long downloaded = 0L;
		for (int i = 0; i < THREAD_AMOUNT; i++) {
			downloaded += THREADS.get(i).getDownloaded();
		}
		return downloaded;
	}

	private class DownloadThread extends Thread {
		private final int id;
		private long downloaded = 0L;

		public DownloadThread(int id) {
			this.id = id;
		}

		public void run() {
			long start = id * threadLen;                                             // 起始位置
			long end = id * threadLen + threadLen - 1;                               // 结束位置
//			System.out.println("线程" + id + ": " + start + " - " + end);

			try {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestProperty("Range", "bytes=" + start + "-" + end);     // 设置当前线程下载的范围
				conn.setRequestProperty("Referer", "https://www.bilibili.com");
				InputStream in = conn.getInputStream();
				RandomAccessFile raf = new RandomAccessFile(file, "rws");
				raf.seek(start);                                                    // 设置保存数据的位置

				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) != -1) {
					raf.write(buffer, 0, len);
					downloaded += len;
//					System.out.println("线程" + id + "已下载 " + downloaded / 1024.0 / 1024.0 + "MB");
				}
				raf.close();
//				System.out.println("线程" + id + "下载完毕");
			} catch (IOException e) {
				System.out.println("\n线程" + id + "在请求 " + start + " - " + end + " 范围间的数据时遇到了以下错误：");
				e.printStackTrace();
			}
		}

		public long getDownloaded() {
			return downloaded;
		}
	}
}
