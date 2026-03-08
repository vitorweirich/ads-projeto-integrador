package com.github.fileshare.constants;

import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class PublicPaths {
	
	private PublicPaths() {}

	public static final RequestMatcher[] PUBLIC_PATHS_MATCHERS = new RequestMatcher[] {
			PathPatternRequestMatcher.withDefaults().matcher("/v1/share-url/{shortUrl}"),
			PathPatternRequestMatcher.withDefaults().matcher("/health")
	};
}
