package me.naptie.bilidownload.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import me.naptie.bilidownload.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zch
 * @version v1.0
 */
// 原帖：https://blog.csdn.net/qq_34928194/article/details/103824942
@SuppressWarnings("ResultOfMethodCallIgnored")
public class QRCodeUtil {

	private static final int ON_COLOR = 0xFF000001;

	private static final int OFF_COLOR = 0xFFFFFFFF;

	/**
	 * @param size    二维码尺寸
	 * @param content 二维码内容，必填
	 * @param logo    logo 图片名称，若为空则生成不带 logo 的二维码
	 * @param imgPath 生成二维码文件夹路径
	 * @param imgName 生成二维码图片名称，必填
	 * @param suffix  生成二维码图片后缀类型，如 gif，必填
	 * @author zch
	 */
	public static boolean generateQRImage(Dimension size, String content, String logo, String imgPath, String imgName, String suffix) {
		if (content == null || imgName == null || suffix == null) {
			return false;
		}
		try {
			if (logo != null && !"".equals(logo.trim())) {
				QREncode(size.width, size.height, content, logo, imgPath, imgName, suffix);
			} else {
				QREncode(size.width, size.height, content, imgPath, imgName, suffix);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@SuppressWarnings("DuplicatedCode")
	private static void QREncode(int width, int height, String content, String imgPath, String imgName, String suffix)
			throws Exception {
		File filePath = new File(imgPath);
		if (!filePath.exists())
			filePath.mkdirs();
		File imageFile = new File(imgPath, imgName);
		Map<EncodeHintType, Object> hints = new HashMap<>();
		// 内容编码格式
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		// 指定纠错等级
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		// 设置二维码边的空度，非负数
		hints.put(EncodeHintType.MARGIN, 1);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
		MatrixToImageWriter.writeToPath(bitMatrix, suffix, imageFile.toPath());// 输出原图片
	}

	// 带 logo
	@SuppressWarnings("DuplicatedCode")
	private static void QREncode(int width, int height, String content, String logo, String imgPath, String imgName, String suffix)
			throws Exception {
		File filePath = new File(imgPath);
		if (!filePath.exists())
			filePath.mkdirs();
		File imageFile = new File(imgPath, imgName);
		Map<EncodeHintType, Object> hints = new HashMap<>();
		// 内容编码格式
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		// 指定纠错等级
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		// 设置二维码边的空度，非负数
		hints.put(EncodeHintType.MARGIN, 1);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
		MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig(ON_COLOR, OFF_COLOR);
		BufferedImage bufferedImage = addLogo(MatrixToImageWriter.toBufferedImage(bitMatrix, matrixToImageConfig), Main.class.getClassLoader().getResourceAsStream(logo));
		ImageIO.write(bufferedImage, suffix, imageFile);// 输出带logo图片
	}

	private static BufferedImage addLogo(BufferedImage matrixImage, InputStream logoStream)
			throws IOException {
		// 读取二维码图片，并构建绘图对象
		Graphics2D gs = matrixImage.createGraphics();
		int matrixWidth = matrixImage.getWidth();
		int matrixHeight = matrixImage.getHeight();
		int ratioWidth = matrixWidth * 2 / 10;
		int ratioHeight = matrixHeight * 2 / 10;
		// 读取 logo 图片
		BufferedImage logo = ImageIO.read(logoStream);
		int logoWidth = Math.min(logo.getWidth(null), ratioWidth);
		int logoHeight = Math.min(logo.getHeight(null), ratioHeight);
		int x = (matrixWidth - logoWidth) / 2;
		int y = (matrixHeight - logoHeight) / 2;

		// 绘制
		gs.drawImage(logo, x, y, logoWidth, logoHeight, null);
		gs.setColor(Color.BLACK);
		gs.setBackground(Color.WHITE);

		gs.dispose();
		matrixImage.flush();
		return matrixImage;
	}
}

