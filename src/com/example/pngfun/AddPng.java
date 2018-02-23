package com.example.pngfun;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

public class AddPng {
	public static final String PNGFILENAME = "bee.png";
	public static final String TARGETFILENAME = "hidetext";
	
	
	public static final byte[] HEADER = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
	public static void hindInPng(){
		try {
			byte[] rawBytes = Files.readAllBytes(Paths.get("file/"+PNGFILENAME));
			byte[] headerBytes = new byte[8];
			
			int index = 0;
			System.arraycopy(rawBytes, index, headerBytes, 0, 8);
			index+=8;
			System.out.println("check header:"+Arrays.equals(HEADER, headerBytes));
			
			List<Chunk> chunks = new ArrayList<>();
			
			while(index < rawBytes.length){
				Chunk chunk = new Chunk();
				byte[] lenBytes = new byte[4];
				System.arraycopy(rawBytes, index, lenBytes, 0, 4);
				index+=4;
				int len = Utils.byteToIntBigEndian(lenBytes);
				chunk.rawlen = lenBytes;
				
				byte[] typeBytes = new byte[4];
				System.arraycopy(rawBytes, index, typeBytes, 0, 4);
				index+=4;
				chunk.rawType = typeBytes;
				
				byte[] payload = new byte[len];
				System.arraycopy(rawBytes, index, payload, 0, payload.length);
				index+=len;
				chunk.payload = payload;
				
				byte[] crcBytes = new byte[4];
				System.arraycopy(rawBytes, index, crcBytes, 0, 4);
				index+=4;
				chunk.rawCrc = crcBytes;
				
				chunks.add(chunk);
			}
			
			for(int i=0; i<chunks.size(); i++){
				Chunk chunk = chunks.get(i);
				System.out.println(chunk.toString());
			}
			
//			byte[] targetBytes = Files.readAllBytes(Paths.get("file/"+TARGETFILENAME));
//			Chunk chunk = new Chunk();
//			chunk.payload = targetBytes;
//			chunk.rawlen = Utils.intToByteBigEndian(targetBytes.length);
//			chunk.rawType = "OOXX".getBytes();
//			CRC32 crc32 = new CRC32();
//			crc32.update(chunk.rawType);
//			crc32.update(chunk.payload);
//			int crc = (int) crc32.getValue();
//			chunk.rawCrc = Utils.intToByteBigEndian(crc);
//			
//			chunks.add(chunks.size()-1, chunk);

			
			byte[] targetBytes = Files.readAllBytes(Paths.get("file/"+TARGETFILENAME));
			Chunk endChunk = chunks.get(chunks.size()-1);
			endChunk.rawlen = Utils.intToByteBigEndian(targetBytes.length);
			endChunk.payload = targetBytes;
			CRC32 crc32 = new CRC32();
			crc32.update(endChunk.rawType);
			crc32.update(endChunk.payload);
			int crc = (int) crc32.getValue();
			endChunk.rawCrc = Utils.intToByteBigEndian(crc);
			
			FileOutputStream fos = new FileOutputStream("file/enc_"+PNGFILENAME);
			fos.write(headerBytes);
			for(int i=0; i<chunks.size(); i++){
				Chunk c = chunks.get(i);
				fos.write(c.rawlen);
				fos.write(c.rawType);
				fos.write(c.payload);
				fos.write(c.rawCrc);
			}
			fos.close();
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void parseFileFromPng(){
		try {
			byte[] pngbytes = Files.readAllBytes(Paths.get("file/enc_"+PNGFILENAME));
			byte[] payload = null;
			int index = 8;
			while(index < pngbytes.length){
				byte[] lenBytes = new byte[4];
				System.arraycopy(pngbytes, index, lenBytes, 0, 4);
				index+=4;
				int len = Utils.byteToIntBigEndian(lenBytes);
				
				byte[] typeBytes = new byte[4];
				System.arraycopy(pngbytes, index, typeBytes, 0, 4);
				index+=4;
				String type = new String(typeBytes);
				if("IEND".equals(type)){
					payload = new byte[len];
					System.arraycopy(pngbytes, index, payload, 0, payload.length);
					break;
				}
				index+=(len+4);
			}
			
			if(payload != null && payload.length > 0){
				FileOutputStream fos = new FileOutputStream("file/"+TARGETFILENAME+".dec");
				fos.write(payload);
				fos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static class Chunk{
		public byte[] rawlen;
		public byte[] rawType;
		public byte[] payload;
		public byte[] rawCrc;
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			CRC32 crc32 = new CRC32();
			crc32.update(rawType);
			crc32.update(payload);
			int crc = (int) crc32.getValue();
			StringBuffer buffer = new StringBuffer();
			buffer.append("chunk type:").append(new String(rawType)).append("\n");
			buffer.append("chunk len:").append(Utils.byteToIntBigEndian(rawlen)).append("\n");
			buffer.append("chunk crc:").append(Utils.byteToIntBigEndian(rawCrc)).append("\n");
			buffer.append("check crc:").append(crc == Utils.byteToIntBigEndian(rawCrc)).append("\n");
			return buffer.toString();
		}
	}
}
