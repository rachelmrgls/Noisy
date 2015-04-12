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
	public static void quantize(String newFileName, String musicFileName, int level) throws IOException{
		BufferedImage img = ImageIO.read(new File(newFileName));
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int value = img.getRGB(i,j);
				Color c = new Color(value);
				int arr[] = new int[3];
				arr[0] = (int) Math.floor(c.getRed() * level / 256) * 256 / level;
				arr[1] = (int) Math.floor(c.getGreen() * level / 256) * 256 / level;
				arr[2] = (int) Math.floor(c.getBlue() * level / 256) * 256 / level;
				c = new Color(arr[0],arr[1],arr[2]);

				bi.setRGB(i,j, c.getRGB());
			}
		}
		File outputfile = new File("quantized.png");
    	ImageIO.write(bi, "png", outputfile);
    	addNoise(musicFileName, level);
    }
    public static void addNoise(String musicFileName, int level) throws IOException {
		BufferedImage img = ImageIO.read(new File("quantized.png"));
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		int interval = 256 / level;
		int imageBits = (int) (Math.log(level) / Math.log(2));
		File musicFile = new File(musicFileName);
		FileInputStream musicInputStream = new FileInputStream(musicFile);
		int metadata = (int) musicFile.length();
		if (musicFile.length() + 4 > (w * h * 3 * (8-imageBits) / 8)) {
			//System.out.println("music file will be cut short");
			metadata = (w * h * 3 * (8-imageBits) / 8);
		}
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
					for (int l = 0; l < (8-imageBits); l++) {
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

    public static void extractNoise(String noisedFileName, int level) throws IOException {
		BufferedImage img = ImageIO.read(new File(noisedFileName));
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		int interval = 256 / level;
		int imageBits = (int) (Math.log(level) / Math.log(2));


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
					arrQuant[k] = (int) Math.floor(arrOrig[k] * level / 256) * 256 / level;
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
    	byte[] unpaddedNoise = new byte[w * h * 3 * (8-imageBits) / 8];

    	StringBuilder bytes = new StringBuilder();
    	ByteArrayBitIterable musicDataIterable = new ByteArrayBitIterable(paddedNoise);
		Iterator<Boolean> itr = musicDataIterable.iterator();

    	int itrIndex = 0;
    	for (int i = 0; i < unpaddedNoise.length - 1; i++) {
    		StringBuilder noise = new StringBuilder();
    		for (int j = 0; j < 8; j++) {
    			while (itrIndex < imageBits) {
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

    	for (int i = 0; i <musicFileLength; i++)
    		musicFileBytes[i] = unpaddedNoise[i+4];

		FileOutputStream musicFile = new FileOutputStream("musicOut.mp3");
		musicFile.write(musicFileBytes);
    	musicFile.close();
	}

	public static void main(String[] args) {

	}
}