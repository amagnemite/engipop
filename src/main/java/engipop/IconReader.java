package engipop;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class IconReader {
	
	//signature 0x0 4 bytes
	//version 0x4 8 bytes
	//header length 0xc 4 bytes
	//width 0x10 2 bytes
	//height 0x12 2 bytes
	//flags 0x14 4 bytes
	//frames 0x18 2 bytes, 1 is no animation
	//first frame 0x1a 2 bytes
	//padding0 0x1c 4 bytes
	//reflectivity 0x20 12 bytes 
	//padding1 0x2c 4 bytes
	//bumpmap scale 0x30 4 bytes
	//high res image format 0x34 4 bytes (dxt5, dxt1, etc)
	//mipmap count 0x38 1 byte
	//low res image format 0x39 4 bytes (always dxt1)
	//low res width 0x3d 1 byte
	//low res height 0x3e 1 byte
	
	//depth of largest mipmap 0x3f 2 bytes, 1 if 2d
	//padding2 0x41 3 bytes
	//number of resource 0x44 4 bytes
	//padding3 0x48 8 bytes

	//File file = new File();
	
	public byte[] getImageData() {
		byte[] imageData = null;
		try {
			imageData = openStream(); //it may be better to combine openstream with this?
		} 
		catch (FileNotFoundException e) {
			return null;
		}
		catch (IOException e) {
			return null;
		}
		return imageData;
	}
	
	public int getWidth(byte[] imageData) {
		return (imageData[16] & 0xFF) | (imageData[17] & 0xFF) << 8;
	}
	
	public int getHeight(byte[] imageData) {
		return (imageData[18] & 0xFF) | (imageData[19] & 0xFF) << 8;
	}
	
	public int[] readIcon(byte[] imageData) {
		int headerSize = (imageData[12] & 0xFF) | (imageData[13] & 0xFF) << 8 
				| (imageData[14] & 0xFF) << 16 | (imageData[15] & 0xFF) << 24;
		int width = getWidth(imageData);
		int height = getHeight(imageData);
		int version = imageData[8] & 0xFF;
		int format = imageData[52] & 0xFF;
		int mipmapCount = imageData[56] & 0xFF;
		
		int[] pixels = new int[width * height];
		int index = -1;
		/*
		if(version >= 3) { //only 7.3+ have hires start
			for(int i = 0; i < headerSize; i++) { //get where image data starts in file
				if(imageData[i] == 0x30) {
					if(imageData[i+1] == 0 && imageData[i+2] == 0) {
						index = (imageData[i+4] & 0xFF) | (imageData[i+5] & 0xFF) << 8 | (imageData[i+5] & 0xFF) << 16 | (imageData[i+6] & 0xFF) << 24;
						break;
					}
				}
			}
		}
		else { //need to add/calculate manually
			if(width == height && width >= 16) { //most vtfs are just gonna have the max 16x16
				index = index + 128;
			}
			else { //
				index = (imageData[61] & 0xFF) * (imageData[62] & 0xFF) * 8;
			}
		}
		
		//skip calculating the largest mipmap since that's where we're going
		for(int i = 1; i < mipmapCount; i++) {
			int mWidth = (width / (int) Math.pow(2, i) + 3) / 4;
			int mHeight = (height / (int) Math.pow(2, i) + 3) / 4;
			
			int mipmapSize = mWidth * mHeight * 16;
			
			index = mipmapSize > 16 ? index + mipmapSize : index + 16;
		} */
		
		pixels = readDXT5(imageData);
		/*
		for(int i = 0; i < pixels.length; i++) {
			System.out.println(i + " " + Integer.toHexString(pixels[i]));
		}
		*/
		return pixels;
	}
	
	private int[] readDXT5(byte[] imageData) {
		int headerSize = (imageData[12] & 0xFF) | (imageData[13] & 0xFF) << 8 | (imageData[14] & 0xFF) << 16 | (imageData[15] & 0xFF) << 24;
		int width = getWidth(imageData);
		int height = getHeight(imageData);
		int[] pixels = new int[width * height];
		int index = -1;
		int mipmapCount = imageData[56] & 0xFF;

		
		for(int i = 0; i < headerSize; i++) { //get where image data starts in file
			if(imageData[i] == 0x30) {
				if(imageData[i+1] == 0 && imageData[i+2] == 0) {
					index = (imageData[i+4] & 0xFF) | (imageData[i+5] & 0xFF) << 8 | (imageData[i+5] & 0xFF) << 16 | (imageData[i+6] & 0xFF) << 24;
					break;
				}
			}
		}
		
		//skip calculating the largest mipmap since that's where we're going
		for(int i = 1; i < mipmapCount; i++) {
			int mWidth = (width / (int) Math.pow(2, i) + 3) / 4;
			int mHeight = (height / (int) Math.pow(2, i) + 3) / 4;
			
			int mipmapSize = mWidth * mHeight * 16;
			
			index = mipmapSize > 16 ? index + mipmapSize : index + 16;
		}
		
		int texelWidth = (width + 3) / 4;
		int texelHeight = (height + 3) / 4;
		
		for(int h = 0; h < texelHeight; h++) {
			for(int w = 0; w < texelWidth; w++) {
				int[] alphaLookup = new int[8];
				int[] alpha = new int[16];
				int[] colorLookup = new int[4];
				int[] color = new int[16];
				
				int alpha0 = imageData[index++] & 0xFF;
				int alpha1 = imageData[index++] & 0xFF;
				
				alphaLookup[0] = alpha0;
				alphaLookup[1] = alpha1;
				
				//System.out.println(alpha0 + " " + alpha1);
				
				if(alpha0 > alpha1) {
					alphaLookup[2] = (6 * alpha0 + alpha1) / 7;
					alphaLookup[3] = (5 * alpha0 + 2 * alpha1) / 7;
					alphaLookup[4] = (4 * alpha0 + 3 * alpha1) / 7;
					alphaLookup[5] = (3 * alpha0 + 4 * alpha1) / 7;
					alphaLookup[6] = (2 * alpha0 + 5 * alpha1) / 7;
					alphaLookup[7] = (alpha0 + 6 * alpha1) / 7;
				}
				else {
					alphaLookup[2] = (4 * alpha0 + alpha1) / 5;
					alphaLookup[3] = (3 * alpha0 + 2 * alpha1) / 5;
					alphaLookup[4] = (2 * alpha0 + 3 * alpha1) / 5;
					alphaLookup[5] = (alpha0 + 4 * alpha1) / 5;
					alphaLookup[6] = 0;
					alphaLookup[7] = 255;
				}
				
				int subalphas = (imageData[index++] & 0xFF) | (imageData[index++] & 0xFF) << 8 | (imageData[index++] & 0xFF) << 16;
				alpha[0] = subalphas & 0x07;
				alpha[1] = (subalphas >> 3) & 0x07;
				alpha[2] = (subalphas >> 6) & 0x07;
				alpha[3] = (subalphas >> 9) & 0x07;
				alpha[4] = (subalphas >> 12) & 0x07;
				alpha[5] = (subalphas >> 15) & 0x07;
				alpha[6] = (subalphas >> 18) & 0x07;
				alpha[7] = (subalphas >> 21) & 0x07;
				subalphas = (imageData[index++] & 0xFF) | (imageData[index++] & 0xFF) << 8 | (imageData[index++] & 0xFF) << 16;
				alpha[8] = subalphas & 0x07;
				alpha[9] = (subalphas >> 3) & 0x07;
				alpha[10] = (subalphas >> 6) & 0x07;
				alpha[11] = (subalphas >> 9) & 0x07;
				alpha[12] = (subalphas >> 12) & 0x07;
				alpha[13] = (subalphas >> 15) & 0x07;
				alpha[14] = (subalphas >> 18) & 0x07;
				alpha[15] = (subalphas >> 21) & 0x07;
				
				int color0 = (imageData[index++] & 0xFF) | (imageData[index++] & 0xFF) << 8;
				int color1 = (imageData[index++] & 0xFF) | (imageData[index++] & 0xFF) << 8;
				
				colorLookup[0] = toRGB888(color0);
				colorLookup[1] = toRGB888(color1);
				
				//System.out.println(h + " " + w + " " + color0 + " " + color1);
				
				colorLookup[2] = toRGB888(getColor2(color0, color1));
				colorLookup[3] = toRGB888(getColor3(color0, color1));
				
				int subcolors = imageData[index++] & 0xFF;
				color[0] = subcolors & 0x03;
				color[1] = (subcolors >> 2) & 0x03;
				color[2] = (subcolors >> 4) & 0x03;
				color[3] = (subcolors >> 6) & 0x03;
				subcolors = imageData[index++] & 0xFF;
				color[4] = subcolors & 0x03;
				color[5] = (subcolors >> 2) & 0x03;
				color[6] = (subcolors >> 4) & 0x03;
				color[7] = (subcolors >> 6) & 0x03;
				subcolors = imageData[index++] & 0xFF;
				color[8] = subcolors & 0x03;
				color[9] = (subcolors >> 2) & 0x03;
				color[10] = (subcolors >> 4) & 0x03;
				color[11] = (subcolors >> 6) & 0x03;
				subcolors = imageData[index++] & 0xFF;
				color[12] = subcolors & 0x03;
				color[13] = (subcolors >> 2) & 0x03;
				color[14] = (subcolors >> 4) & 0x03;
				color[15] = (subcolors >> 6) & 0x03;
				
				for(int k = 0; k < 4; k++) {					
					int row = w * 4 + h * 256 + k * 64;
									
					pixels[row] = colorLookup[color[4*k]] | alphaLookup[alpha[4*k]] << 24;
					pixels[row + 1] = colorLookup[color[4*k+1]] | alphaLookup[alpha[4*k+1]] << 24;
					pixels[row + 2] = colorLookup[color[4*k+2]] | alphaLookup[alpha[4*k+2]] << 24;
					pixels[row + 3] = colorLookup[color[4*k+3]] | alphaLookup[alpha[4*k+3]] << 24;
				}
			}
		}
		return pixels;
	}
	
	private int toRGB888(int rgb565) {
		int blue = rgb565 & 0x1F;
		int green = (rgb565 >> 5) & 0x3F;
		int red = (rgb565 >> 11) & 0x1F;
		
		int blue8 = blue * 255 / 31;
		int green8 = green * 255 / 63;
		int red8 = red * 255 / 31;
		
		return blue8 | green8 << 8 | red8 << 16;
	}
	
	private int getColor2(int color0, int color1) {
		int blue = (2 * (color0 & 0x1F) + (color1 & 0x1F)) / 3;
		int green = (2 * ((color0 >> 5) & 0x3F) + ((color1 >> 5) & 0x3F)) / 3;
		int red = (2 * ((color0 >> 11) & 0x1F) + ((color1 >> 11) & 0x1F)) / 3;
		
		return blue | green << 5 | red << 11;
	}
	
	private int getColor3(int color0, int color1) {
		int blue = ((color0 & 0x1F) + 2 * (color1 & 0x1F)) / 3;
		int green = (((color0 >> 5) & 0x3F) + 2 * ((color1 >> 5) & 0x3F)) / 3;
		int red = (((color0 >> 11) & 0x1F) + 2 * ((color1 >> 11) & 0x1F)) / 3;
		
		return blue | green << 5 | red << 11;
	}
	
	private byte[] openStream() throws FileNotFoundException, IOException {
		FileInputStream input = new FileInputStream(file);
		byte[] buffer = new byte[input.available()];
		input.read(buffer);
		input.close();
		
		return buffer;
	}
}
