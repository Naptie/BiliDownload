package me.naptie.bilidownload.objects;

import me.naptie.bilidownload.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 原帖：https://blog.csdn.net/fzy629442466/article/details/85601315
@SuppressWarnings("deprecation")
public class Downloader {
	private final URL url;
	private final File file;
	private final List<DownloadThread> THREADS = new ArrayList<>();
	private final int THREAD_AMOUNT;
	private long threadLen, totalLen;
	private List<Map.Entry<Long, Long>> status;

	public Downloader(String address, String path, int threadAmount) throws IOException {
		url = new URL(address);
		file = new File(path);
		THREAD_AMOUNT = threadAmount;
	}

	public Downloader(String address, String path, List<Map.Entry<Long, Long>> status) throws IOException {
		url = new URL(address);
		file = new File(path);
		THREAD_AMOUNT = status.size();
		this.status = status;
	}

	public List<Map.Entry<Long, Long>> cancel() {
		List<Map.Entry<Long, Long>> status = new ArrayList<>();
		for (int i = 0; i < THREAD_AMOUNT; i++) {
			Map.Entry<Long, Long> entry = THREADS.get(i).getToDownload();
			if (!THREADS.get(i).isFinished()) {
				status.add(entry);
//				if (Main.debug) System.out.println("线程" + i + "已下载到 " + entry.getKey() + "B，目标为 " + entry.getValue() + "B");
			}
			THREADS.get(i).stop();
			if (Main.debug) System.out.println("已中止线程" + i);
		}
		return status;
	}

	public short download() {
		File disk = Paths.get(file.getAbsolutePath()).getRoot().toFile().getAbsoluteFile();
		if (disk.getUsableSpace() < totalLen) {
			return -1;
		}
		if (status == null) {
			return -2;
		}

		for (int i = 0; i < THREAD_AMOUNT; i++) {
			DownloadThread thread = new DownloadThread(i);
			THREADS.add(thread);
			thread.setStart(status.get(i).getKey());
			thread.setEnd(status.get(i).getValue());
			thread.start();
		}
		return 0;
	}

	public long download(long totalLen) throws IOException {
		File disk = Paths.get(file.getAbsolutePath()).getRoot().toFile().getAbsoluteFile();
		if (disk.getUsableSpace() < totalLen) {
			return -1;
		}
		this.totalLen = totalLen;
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Referer", "https://www.bilibili.com");
		conn.setConnectTimeout(5000);
		threadLen = (totalLen + THREAD_AMOUNT - 1) / THREAD_AMOUNT; // 计算每个线程要下载的长度
		RandomAccessFile raf = new RandomAccessFile(file, "rws"); // 在本地创建一个和服务端大小相同的文件
		raf.setLength(totalLen); // 设置文件的大小
		raf.close();

		for (int i = 0; i < THREAD_AMOUNT; i++) { // 开启 THREAD_AMOUNT 条线程, 每个线程下载一部分数据到本地文件中
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

	public List<DownloadThread> getThreads() {
		return THREADS;
	}

	public boolean isInterrupted(DownloadThread thread) {
		return !thread.isFinished() && thread.isInterrupted();
	}

	public class DownloadThread extends Thread {
		private final int id;
		private long downloaded = 0L, start = -1, end = -1;
		private boolean finished = false;

		public DownloadThread(int id) {
			this.id = id;
		}

		public void run() {
			if (start == -1) {
				start = id * threadLen; // 起始位置
				if (start >= totalLen) {
					finished = true;
					this.stop();
				}
			}
			if (end == -1) {
				end = (id + 1) * threadLen - 1; // 结束位置
				if (end >= totalLen) {
					end = totalLen - 1;
				}
			}
			if (Main.debug) System.out.println("线程" + id + "开始下载 " + start + "B - " + end + "B 之间的数据");

			try {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestProperty("Range", "bytes=" + start + "-" + end); // 设置当前线程下载的范围
				conn.setRequestProperty("Referer", "https://www.bilibili.com");
				InputStream in = conn.getInputStream();
				RandomAccessFile raf = new RandomAccessFile(file, "rws");
				raf.seek(start); // 设置保存数据的位置

				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) != -1) {
					raf.write(buffer, 0, len);
					downloaded += len;
//					System.out.println("线程" + id + "已下载 " + downloaded / 1024.0 / 1024.0 + "MB");
				}
				raf.close();
//				System.out.println("线程" + id + "下载完毕");
				finished = true;
				this.stop();
			} catch (IOException e) {
				this.stop();
				System.out.println("\n线程" + id + "在请求 " + start + "B - " + end + "B 范围间的数据时遇到了以下错误：\n" + e.getLocalizedMessage());
			}
		}

		public void setStart(long start) {
			this.start = start;
		}

		public void setEnd(long end) {
			this.end = end;
		}

		public Map.Entry<Long, Long> getToDownload() {
			return new Map.Entry<Long, Long>() {
				@Override
				public Long getKey() {
					return start + downloaded;
				}

				@Override
				public Long getValue() {
					return end;
				}

				@Override
				public Long setValue(Long value) {
					return null;
				}
			};
		}

		public long getDownloaded() {
			return downloaded;
		}

		public boolean isFinished() {
			return finished;
		}
	}
}
