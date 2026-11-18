package org.springframework.web.filter

import com.google.common.hash.Hashing
import jakarta.servlet.http.HttpServletResponse
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest

import java.nio.charset.StandardCharsets

class Sha512ShallowEtagHeaderFilterSpec extends Specification {

	private static final byte[] BODY = 'foobar'.getBytes(StandardCharsets.UTF_8)
	private static final String EXPECTED_ETAG = '"' + Hashing.sha512().hashBytes(BODY) + '"'

	private final Sha512ShallowEtagHeaderFilter filter = new Sha512ShallowEtagHeaderFilter()

	def 'should set SHA-512 based ETag header'() {
		given:
			MockHttpServletRequest request = new MockHttpServletRequest('GET', '/file')
			MockHttpServletResponse response = new MockHttpServletResponse()
		when:
			filter.doFilter(request, response, writingChain())
		then:
			response.status == HttpServletResponse.SC_OK
			response.getHeader('ETag') == EXPECTED_ETAG
			response.contentAsByteArray == BODY
	}

	def 'should return 304 when If-None-Match matches SHA-512 ETag'() {
		given:
			MockHttpServletRequest request = new MockHttpServletRequest('GET', '/file')
			request.addHeader('If-None-Match', EXPECTED_ETAG)
			MockHttpServletResponse response = new MockHttpServletResponse()
		when:
			filter.doFilter(request, response, writingChain())
		then:
			response.status == HttpServletResponse.SC_NOT_MODIFIED
			response.getHeader('ETag') == EXPECTED_ETAG
			response.contentAsByteArray.length == 0
	}

	private static FilterChain writingChain() {
		return new MockFilterChain(new HttpServlet() {
			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
				resp.outputStream.write(BODY)
			}
		})
	}
}
