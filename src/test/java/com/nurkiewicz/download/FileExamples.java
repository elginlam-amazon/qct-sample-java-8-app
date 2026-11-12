package com.nurkiewicz.download;

import java.io.File;
import java.net.URL;
import java.util.UUID;

public class FileExamples {

	public static final UUID TXT_FILE_UUID = UUID.fromString("4f1a5c7e-0b52-4c4a-9c9b-2f0a3b6d1e11");
	public static final FilePointer TXT_FILE = txtFile();
	public static final UUID NOT_FOUND_UUID = UUID.fromString("8b2d6f31-9a44-4e58-bd77-5c1e0a9f3c22");

	private static FileSystemPointer txtFile() {
		final URL resource = FileStorageStub.class.getResource("/download.txt");
		final File file = new File(resource.getFile());
		return new FileSystemPointer(file);
	}


}
