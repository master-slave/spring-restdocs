/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.restdocs.hypermedia;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Parameterized tests for {@link LinkExtractors} with various payloads.
 *
 * @author Andy Wilkinson
 */
@RunWith(Parameterized.class)
public class LinkExtractorsPayloadTests {

	private final LinkExtractor linkExtractor;

	private final String linkType;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[] { LinkExtractors.halLinks(), "hal" },
				new Object[] { LinkExtractors.atomLinks(), "atom" });
	}

	public LinkExtractorsPayloadTests(LinkExtractor linkExtractor, String linkType) {
		this.linkExtractor = linkExtractor;
		this.linkType = linkType;
	}

	@Test
	public void singleLink() throws IOException {
		Map<String, List<Link>> links = this.linkExtractor
				.extractLinks(createResponse("single-link"));
		assertLinks(Arrays.asList(new Link("alpha", "http://alpha.example.com")), links);
	}

	@Test
	public void multipleLinksWithDifferentRels() throws IOException {
		Map<String, List<Link>> links = this.linkExtractor
				.extractLinks(createResponse("multiple-links-different-rels"));
		assertLinks(Arrays.asList(new Link("alpha", "http://alpha.example.com"),
				new Link("bravo", "http://bravo.example.com")), links);
	}

	@Test
	public void multipleLinksWithSameRels() throws IOException {
		Map<String, List<Link>> links = this.linkExtractor
				.extractLinks(createResponse("multiple-links-same-rels"));
		assertLinks(Arrays.asList(new Link("alpha", "http://alpha.example.com/one"),
				new Link("alpha", "http://alpha.example.com/two")), links);
	}

	@Test
	public void noLinks() throws IOException {
		Map<String, List<Link>> links = this.linkExtractor
				.extractLinks(createResponse("no-links"));
		assertLinks(Collections.<Link> emptyList(), links);
	}

	@Test
	public void linksInTheWrongFormat() throws IOException {
		Map<String, List<Link>> links = this.linkExtractor
				.extractLinks(createResponse("wrong-format"));
		assertLinks(Collections.<Link> emptyList(), links);
	}

	private void assertLinks(List<Link> expectedLinks, Map<String, List<Link>> actualLinks) {
		MultiValueMap<String, Link> expectedLinksByRel = new LinkedMultiValueMap<>();
		for (Link expectedLink : expectedLinks) {
			expectedLinksByRel.add(expectedLink.getRel(), expectedLink);
		}
		assertEquals(expectedLinksByRel, actualLinks);
	}

	private MockHttpServletResponse createResponse(String contentName) throws IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		FileCopyUtils.copy(new FileReader(getPayloadFile(contentName)),
				response.getWriter());
		return response;
	}

	private File getPayloadFile(String name) {
		return new File("src/test/resources/link-payloads/" + this.linkType + "/" + name
				+ ".json");
	}
}
