import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.lang.StringBuilder;

public class Noisy {
	public static void quantize(String newFileName, String musicFileName, int imgBits) throws IOException{
		//check for valid parameters
		if (imgBits < 0 || imgBits > 7) {
			throw new IOException("\n\n[image bits] represents the number of bits (per 8 bits) assigned to the image," +
								  " \nand must be an integer between 0 (only noise) and 7 (very little noise)");
		}

		int levels = (int) Math.pow(2.0,imgBits);
		BufferedImage img = ImageIO.read(new File(newFileName));
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int value = img.getRGB(i,j);
				Color c = new Color(value);
				int arr[] = new int[3];
				arr[0] = (int) Math.floor(c.getRed() * levels / 256) * 256 / levels;
				arr[1] = (int) Math.floor(c.getGreen() * levels / 256) * 256 / levels;
				arr[2] = (int) Math.floor(c.getBlue() * levels / 256) * 256 / levels;
				c = new Color(arr[0],arr[1],arr[2]);

				bi.setRGB(i,j, c.getRGB());
			}
		}
		File outputfile = new File("quantized.png");
    	ImageIO.write(bi, "png", outputfile);
    	addNoise(musicFileName, imgBits);
    }
    public static void addNoise(String musicFileName, int imgBits) throws IOException {
		//check for valid parameters
		File musicFile = new File(musicFileName);
		BufferedImage img = ImageIO.read(new File("quantized.png"));
		int w = img.getWidth(null);
		int h = img.getHeight(null);

		if (musicFile.length() + 4 > (w * h * 3 * (8-imgBits) / 8)) {
			throw new IOException("Not enough space to store the provided music file." +
								  " To remedy this issue you can: select a lower value for [image bits]," +
								  " pick a smaller music file, and/or pick a larger image file");
		}
		int levels = (int) Math.pow(2.0,imgBits);
		int interval = 256 / levels;
		FileInputStream musicInputStream = new FileInputStream(musicFile);
		int metadata = (int) musicFile.length();
		byte[] metaArray = ByteBuffer.allocate(4).putInt(metadata).array();
		byte[] musicArray = new byte[(int) musicFile.length()];
		
		musicInputStream.read(musicArray);
		byte[] noiseArray = new byte[8 + (int) musicFile.length()];
		
		for (int i = 0; i < 4; i++)
			noiseArray[i] = metaArray[i];

		for (int i = 0; i < musicArray.length; i++)
			noiseArray[i + 8] = musicArray[i];

		ByteArrayBitIterable noiseDataIterable = new ByteArrayBitIterable(noiseArray);
		Iterator<Boolean> itr = noiseDataIterable.iterator();

		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int value = img.getRGB(i,j);
				Color c = new Color(value);
				int arr[] = new int[3];
				arr[0] = c.getRed();
				arr[1] = c.getGreen();
				arr[2] = c.getBlue();
				for (int k = 0; k < 3; k++) {
					StringBuilder noise = new StringBuilder();
					for (int l = 0; l < (8-imgBits); l++) {
						if (itr.hasNext()) {
							if (itr.next())
								noise.append("1");
							else
								noise.append("0");
						}
						else
							noise.append(Math.round(Math.random()));
					}
					arr[k] += Integer.parseInt(noise.toString(), 2);
				}
				c = new Color(arr[0],arr[1],arr[2]);

				bi.setRGB(i,j, c.getRGB());
			}
		}
		File outputfile = new File("noised.png");
    	ImageIO.write(bi, "png", outputfile);
    }

    public static void extractNoise(String noisedFileName, int imgBits) throws IOException {
    	//check for valid parameters
		if (imgBits < 0 || imgBits > 7) {
			throw new IOException("\n\n[image bits] represents the number of bits (per 8 bits) assigned to the image," +
								  " \nand must be an integer between 0 (only noise) and 7 (very little noise)");
		}

		BufferedImage img = ImageIO.read(new File(noisedFileName));
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		int levels = (int) Math.pow(2.0, imgBits);
		int interval = 256 / levels;


		// step 1 : separate out noise and image
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		byte[] paddedNoise = new byte[w * h * 3];
		int padIndex = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int value = img.getRGB(i,j);
				Color c = new Color(value);
				int arrOrig[] = new int[3];
				int arrQuant[] = new int[3];
				arrOrig[0] = c.getRed();
				arrOrig[1] = c.getGreen();
				arrOrig[2] = c.getBlue();

				for (int k = 0; k < 3; k++) {
					arrQuant[k] = (int) Math.floor(arrOrig[k] * levels / 256) * 256 / levels;
					byte[] temp = ByteBuffer.allocate(4).putInt(arrOrig[k]-arrQuant[k]).array();
					paddedNoise[padIndex] = temp[3];
					padIndex++;
				}
				c = new Color(arrQuant[0], arrQuant[1], arrQuant[2]);

				bi.setRGB(i, j, c.getRGB());
			}
		}
		File outputfile = new File("denoised.png");
    	ImageIO.write(bi, "png", outputfile);

		// step 2: read metadata to find how long the music file should be
    	byte[] unpaddedNoise = new byte[w * h * 3 * (8-imgBits) / 8];

    	StringBuilder bytes = new StringBuilder();
    	ByteArrayBitIterable musicDataIterable = new ByteArrayBitIterable(paddedNoise);
		Iterator<Boolean> itr = musicDataIterable.iterator();

    	int itrIndex = 0;
    	for (int i = 0; i < unpaddedNoise.length - 1; i++) {
    		StringBuilder noise = new StringBuilder();
    		for (int j = 0; j < 8; j++) {
    			while (itrIndex < imgBits) {
    				itr.next();
    				itrIndex = (itrIndex + 1) % 8;
    			}
    			
				if (itr.hasNext() && itr.next())
					noise.append("1");
				else
					noise.append("0");
				itrIndex = (itrIndex + 1) % 8;
			}
			int noiseInt = Integer.parseInt(noise.toString(), 2);
			byte[] temp = ByteBuffer.allocate(4).putInt(noiseInt).array();
			unpaddedNoise[i] = temp[3];
    	}

    	byte[] metadata = new byte[4];
    	for (int i = 0; i < metadata.length; i++)
    		metadata[i] = unpaddedNoise[i];

    	int musicFileLength = ByteBuffer.wrap(metadata).getInt();
    	byte[] musicFileBytes = new byte[musicFileLength];

    	for (int i = 0; i < musicFileLength; i++)
    		musicFileBytes[i] = unpaddedNoise[i+4];

		FileOutputStream musicFile = new FileOutputStream("musicOut.mp3");
		musicFile.write(musicFileBytes);
    	musicFile.close();
	}

	public static void main(String[] args) {

	}
}