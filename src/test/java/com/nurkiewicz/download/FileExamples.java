package com.nurkiewicz.download;

import java.io.File;
import java.net.URL;
import java.util.UUID;

public class FileExamples {

	// Fixed UUIDs so the Groovy compiler inlines the same value it sees at runtime.
	// UUID.randomUUID() was baked at Groovy 4.x compile-time into the spec bytecode
	// but regenerated at test runtime, causing a UUID mismatch in FileStorageStub.
	public static final UUID TXT_FILE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
	public static final FilePointer TXT_FILE = txtFile();
	public static final UUID NOT_FOUND_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");

	private static FileSystemPointer txtFile() {
		final URL resource = FileStorageStub.class.getResource("/download.txt");
		final File file = new File(resource.getFile());
		return new FileSystemPointer(file);
	}


}
