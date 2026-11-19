package com.nurkiewicz.download;

import java.io.File;
import java.net.URL;
import java.util.UUID;

public class FileExamples {

	public static final UUID TXT_FILE_UUID = UUID.fromString("6b5b1a0e-3c1f-4d2a-9e4b-1f0c2d3e4a5b");
	public static final FilePointer TXT_FILE = txtFile();
	public static final UUID NOT_FOUND_UUID = UUID.fromString("0f9e8d7c-6b5a-4f3e-8d2c-1b0a9f8e7d6c");

	private static FileSystemPointer txtFile() {
		final URL resource = FileStorageStub.class.getResource("/download.txt");
		final File file = new File(resource.getFile());
		return new FileSystemPointer(file);
	}


}
