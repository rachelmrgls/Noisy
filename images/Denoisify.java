import java.io.*;

public class Denoisify {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			throw new IOException("\n \nProgram requires two parameters: a \"noisy\" image file " +
									"and an integer that specifies how many bits per byte are " +
									"allocated to the original image (from 0-7).\n\n" +
									"Run as follows: \n" +
								    ">java Denoisify [image file path] [image bits (0-7)]\n\n");
		}
		Noisy.extractNoise(args[0], Integer.parseInt(args[1]));
	}
}