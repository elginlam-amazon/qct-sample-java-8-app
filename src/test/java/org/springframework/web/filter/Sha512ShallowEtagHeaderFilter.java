package org.springframework.web.filter;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

public class Sha512ShallowEtagHeaderFilter extends ShallowEtagHeaderFilter {

	/**
	 * Custom implementation to use SHA-512 instead of MD5 for ETag generation.
	 * This method is used internally by the filter.
	 * 
	 * @param bytes The response body bytes to hash
	 * @return An ETag value using SHA-512 hash
	 */
	protected String generateSha512ETag(byte[] bytes) {
		final HashCode hash = Hashing.sha512().hashBytes(bytes);
		return "\"" + hash + "\"";
	}
}
