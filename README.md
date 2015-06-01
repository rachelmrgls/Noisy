# Noisy
A tool for encoding binary data from music files into images as visual noise, while maintaining image content.


# To Encode Music into Image
- Download source code
- Navigate to the [images directory] (./images) from the command-line
- Add image and music file to this directory (example:  kale.jpeg, Uptown Funk.mp3)
- Select the value of "image bits" (an integer from 0-7) for this encoding.
	- The image bits value is an integer that specifies how many bits per byte are allocated to the image (from 0-7).
	- A lower value allows for storing more music, but lowers image quality.
	- A higher value allows for less music storage, but gives better image quality.
	- A value around 2 - 4 is suggested. 
- From the command line, run

	> java Noisify \[image file path\] \[music file path\] \[image bits\]

- For example:
	
	> java Noisify "kale.jpeg" "Uptown Funk.mp3" 2
- If all parameters are sufficient, the resultant image will be called "noised.png" and will be located in the same directory

# To Extract Music from "Noisy" Image
- Download source code
- Navigate to the [images directory] (./images) from the command-line
- Make sure the noised image is located in this same directory
- NOTE: to extract music from an image, you must know the image bits value with which it was encoded.
- From the command line, run
	> java Denoisify \[image file path\] \[image bits\]
- For example:
	> java Denoisify "noised.png" 2
- If all parameters are sufficient, the resultant image and music file will be called "denoised.png" and "musicOut.mp3", respectively. They will be located in the same images directory. 