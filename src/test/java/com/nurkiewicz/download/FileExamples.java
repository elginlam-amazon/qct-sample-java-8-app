package com.nurkiewicz.download;

import java.io.File;
import java.net.URL;
import java.util.UUID;

public class FileExamples {

	public static final UUID TXT_FILE_UUID = UUID.fromString("8b5d2a2e-2b3a-4a1e-9a7f-6c1d9f0f1a01");
	public static final FilePointer TXT_FILE = txtFile();
	public static final UUID NOT_FOUND_UUID = UUID.fromString("3f1c7b64-5c6e-4d2b-8e1a-9b0c2d3e4f02");

	private static FileSystemPointer txtFile() {
		final URL resource = FileStorageStub.class.getResource("/download.txt");
		final File file = new File(resource.getFile());
		return new FileSystemPointer(file);
	}


}
