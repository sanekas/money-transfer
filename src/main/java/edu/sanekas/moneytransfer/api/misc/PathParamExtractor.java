package edu.sanekas.moneytransfer.api.misc;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;

import java.util.Optional;

public class PathParamExtractor {
    private PathParamExtractor() {}

    public static Optional<String> extractPathParam(HttpServerExchange httpServerExchange, String paramName) {
        return Optional.ofNullable(httpServerExchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY))
                .map(PathTemplateMatch::getParameters)
                .map(params -> params.get(paramName));
    }
}
