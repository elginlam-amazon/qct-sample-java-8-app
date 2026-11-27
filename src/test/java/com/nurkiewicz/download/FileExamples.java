package com.nurkiewicz.download;

import java.io.File;
import java.net.URL;
import java.util.UUID;

public class FileExamples {

	// Fixed, not random: the Groovy compiler constant-folds the specs' `static final String`
	// URLs at build time, so a random UUID here would never match the one seen at runtime.
	public static final UUID TXT_FILE_UUID = UUID.fromString("6f1b2e4a-9c3d-4f5b-8a7e-1d2c3b4a5968");
	public static final FilePointer TXT_FILE = txtFile();
	public static final UUID NOT_FOUND_UUID = UUID.fromString("a2e9f0c1-5b84-4d27-9e63-7c8f1a0b5d34");

	private static FileSystemPointer txtFile() {
		final URL resource = FileStorageStub.class.getResource("/download.txt");
		final File file = new File(resource.getFile());
		return new FileSystemPointer(file);
	}


}
