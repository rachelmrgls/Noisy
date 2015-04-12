import java.io.*;

public class Noisify {
	public static void main(String[] args) throws IOException {
		Noisy.quantize(args[0], args[1], Integer.parseInt(args[2]));
    }
}