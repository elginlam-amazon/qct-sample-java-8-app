package org.springframework.web.filter;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Sha512ShallowEtagHeaderFilter extends ShallowEtagHeaderFilter {

	@Override
	protected String generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException {
		HashingInputStream hashingInputStream = new HashingInputStream(Hashing.sha512(), inputStream);
		hashingInputStream.transferTo(OutputStream.nullOutputStream());
		HashCode hash = hashingInputStream.hash();
		return (isWeak ? "W/" : "") + "\"" + hash + "\"";
	}
}
