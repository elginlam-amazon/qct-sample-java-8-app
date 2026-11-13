package com.nurkiewicz.download;

import java.io.File;
import java.net.URL;
import java.util.UUID;

public class FileExamples {

	public static final UUID TXT_FILE_UUID = UUID.fromString("f2b2e0ca-8c21-4f0e-8b5e-0a9f3c5f4a11");
	public static final FilePointer TXT_FILE = txtFile();
	public static final UUID NOT_FOUND_UUID = UUID.fromString("9c4b1d2e-5a63-4f77-9ab1-7d0c2e8f6b22");

	private static FileSystemPointer txtFile() {
		final URL resource = FileStorageStub.class.getResource("/download.txt");
		final File file = new File(resource.getFile());
		return new FileSystemPointer(file);
	}


}
