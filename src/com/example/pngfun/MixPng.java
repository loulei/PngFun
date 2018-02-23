package com.example.pngfun;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class MixPng {
	public static String FILENAME = "hidetext";

	public static void decodeFile() {
		try {
			BufferedImage bufferedImage = ImageIO.read(new File("file/" + FILENAME + ".png"));
			int length = bufferedImage.getRGB(0, 0);
			byte[] data = data(bufferedImage, length);
			String header = header(data);
			byte[] raw = new byte[data.length - header.length() - 1];
			System.arraycopy(data, header.length() + 1, raw, 0, raw.length);
			data = raw;
			File file = new File("file/" + header + ".dec");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String header(byte[] data) {
		int i = 0;
		String header = "";
		while (data[i] != 0) {
			header += (char) data[i++];
		}
		return header;
	}

	private static byte[] data(BufferedImage bufferedImage, int length) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		byte[] dataTmp = new byte[width * height * 4 - 4];
		for (int h = 0; h < height; h++) {
			for (int w = (h == 0 ? 1 : 0); w < width; w++) {
				int i = (w - 1 + h * width) * 4;
				int rgb = bufferedImage.getRGB(w, h);
				dataTmp[i + 0] = (byte) ((rgb >> 24) & 0xff);
				dataTmp[i + 1] = (byte) ((rgb >> 16) & 0xff);
				dataTmp[i + 2] = (byte) ((rgb >> 8) & 0xff);
				dataTmp[i + 3] = (byte) (rgb & 0xff);
			}
		}
		byte[] data = new byte[length];
		System.arraycopy(dataTmp, 0, data, 0, length);
		return data;
	}

	public static void encodeFile() {
		try {
			int[] datas = byteToInt(header(Files.readAllBytes(Paths.get("file/" + FILENAME)), FILENAME));
			int width = nearestSquareDivisor(datas.length);
			// int height = datas.length / width;
			int height = width;
			int[] newDatas = new int[width * height];
			System.arraycopy(datas, 0, newDatas, 0, datas.length);
			BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					bufferedImage.setRGB(w, h, newDatas[w + h * width]);
				}
			}
			ImageIO.write(bufferedImage, "png", new File("file/" + FILENAME + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static byte[] header(byte[] bytes, String input) {
		char[] charray = new File(input).getName().toCharArray();
		byte[] head = new byte[charray.length];
		for (int i = 0; i < charray.length; i++) {
			head[i] = (byte) charray[i];
		}
		byte[] header = new byte[head.length + bytes.length + 1];
		System.arraycopy(head, 0, header, 0, head.length);
		System.arraycopy(bytes, 0, header, head.length + 1, bytes.length);
		return header;
	}

	private static int[] byteToInt(byte[] bytes) {
		int length = bytes.length;
		if (bytes.length / 4 * 4 != bytes.length) {
			byte[] bys = new byte[bytes.length / 4 * 4 + 4];
			System.arraycopy(bytes, 0, bys, 0, bytes.length);
			bytes = bys;
		}
		int[] ints = new int[bytes.length / 4 + 1];
		ints[0] = length;
		for (int i = 0; i < bytes.length; i += 4) {
			ints[i / 4 + 1] = ((bytes[i] & 0xff) << 24) | ((bytes[i + 1] & 0xff) << 16) | ((bytes[i + 2] & 0xff) << 8) | (bytes[i + 3] & 0xff);
		}
		return ints;
	}

	private static int nearestSquareDivisor(int num) {
		// int divisor = (int) Math.sqrt(num);
		// int divided = num/divisor;
		// while(divisor*divided != num){
		// divisor--;
		// divided = num/divisor;
		// }
		// return divisor;
		return (int) Math.ceil(Math.sqrt(num));
	}
}
