import java.io.*;

public class Noisify {
	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			throw new IOException("\n \nProgram requires three parameters: an image file, a music file, " +
									"and an integer (image bits)that specifies how many bits per byte are " +
									"allocated to the image (from 0-7).\n" +
									"A lower value for image bits allows for storing more music, but lowers image quality.\n" +
									"A higher value for image bits allows for less music storage, but gives better image quality.\n"+
									"A image bits value around 2 - 4 is suggested. \n \n"+
									"Run as follows: \n" +
								    ">java Noisify [image file path] [music file path] [image bits (0-7)]\n\n");
		}
		Noisy.quantize(args[0], args[1], Integer.parseInt(args[2]));
    }
}