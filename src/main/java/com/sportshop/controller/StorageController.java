package com.sportshop.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/storage")
public class StorageController {
	private String imageFolder = "D:\\Programing\\images";
	@GetMapping("/images/products/{folder}/{fileName:.+}")
	public byte[] getImageProduct(@PathVariable String folder
			, @PathVariable String fileName ) throws IOException
	{
		String pathImage = imageFolder + "\\products\\"
						  + folder + "\\" + fileName;
		byte[] images = Files.readAllBytes(new File(pathImage).toPath());
		return images;
	}
	@GetMapping("/images/categories/{fileName:.+}")
	public byte[] getImageCategory(@PathVariable String fileName ) throws IOException
	{
		String pathImage = imageFolder + "\\categories\\"
						   + "\\" + fileName;
		byte[] images = Files.readAllBytes(new File(pathImage).toPath());
		return images;
	}
}
