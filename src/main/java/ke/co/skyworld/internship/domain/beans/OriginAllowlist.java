package ke.co.skyworld.internship.domain.beans;


import ke.co.skyworld.internship.config.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


public final class OriginAllowlist {

    private final Set<String> exactOrigins;
    private final List<Pattern> wildcardPortPatterns;

    private OriginAllowlist(Set<String> exactOrigins, List<Pattern> wildcardPortPatterns) {
        this.exactOrigins = exactOrigins;
        this.wildcardPortPatterns = wildcardPortPatterns;
    }

    public static OriginAllowlist fromConfig() {
        Set<String> exact = new HashSet<>();
        List<Pattern> wildcards = new ArrayList<>();

        for (String configured : Constants.getCorsAllowedOrigins()) {
            String origin = configured.trim();
            if (origin.isEmpty()) continue;
            if (origin.endsWith(":*")) {
                String schemeAndHost = origin.substring(0, origin.length() - 2);
                wildcards.add(Pattern.compile(Pattern.quote(schemeAndHost) + ":\\d+"));
            } else {
                exact.add(origin);
            }
        }
        return new OriginAllowlist(Set.copyOf(exact), List.copyOf(wildcards));
    }

    /**
     * @return {@code requestOrigin} unchanged if it is allowed (to echo back as
     * Access-Control-Allow-Origin), or {@code null} if it is not.
     */
    public String match(String requestOrigin) {
        if (requestOrigin == null) return null;
        if (exactOrigins.contains(requestOrigin)) return requestOrigin;
        for (Pattern pattern : wildcardPortPatterns) {
            if (pattern.matcher(requestOrigin).matches()) return requestOrigin;
        }
        return null;
    }
}
