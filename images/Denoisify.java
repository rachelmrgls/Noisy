import java.io.*;

public class Denoisify {
	public static void main(String[] args) throws IOException {
		Noisy.extractNoise(args[0], Integer.parseInt(args[1]));
	}
}