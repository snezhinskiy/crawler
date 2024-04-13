package com.snezhinskiy.crawler.processing.parser.utils;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UrlUtils {
    private static String[] allowedExtensions = {"htm", "html", "php", "asp", "aspx", "shtml"};

    static {
        Arrays.sort(allowedExtensions);
    }

    /**
     * Check is url ends with htm | html | php | asp | aspx | shtml | or no ext at all
     */
    public static boolean isMediaFileUrl(String url) {
        String path = toAbsolutePath(url);
        String ext = path.contains(".") ? path.substring(path.lastIndexOf(".") + 1) : null;

        return StringUtils.hasText(ext)
            && Arrays.binarySearch(allowedExtensions, ext.toLowerCase()) < 0;
    }

    public static String getHostWithSchema(String url) {
        String base = "";

        try {
            URI uri = new URI(url);

            if (StringUtils.hasText(uri.getScheme())) {
                base = uri.getScheme() + "://" + uri.getHost();
            } else {
                base = "http://" + uri.getHost();
            }
        } catch (URISyntaxException e) {}

        return base;
    }

    public static String getHost(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {}

        return "";
    }

    public static String toAbsolutePath(String url) {
        try {
            URI uri = new URI(url);
            return uri.getPath();
        } catch (URISyntaxException e) {}

        return "";
    }

    public static String toAbsoluteUrl(String domainUrl, String url) {
        if (!StringUtils.hasText(domainUrl))
            return url;

        String absUrl = toAbsolutePath(url);

        if (domainUrl.endsWith("/") && absUrl.startsWith("/"))
            return domainUrl + absUrl.substring(1, url.length());
        else
            return domainUrl + absUrl;
    }

    public static String removeTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static boolean isSameSchemaAndHost(String testUrl, String parentUrl) {
        if (!StringUtils.hasText(testUrl) || !StringUtils.hasText(parentUrl)) {
            return false;
        }

        if (testUrl.startsWith("/")) {
            return true;
        }

        final String hostWithSchema = getHostWithSchema(parentUrl);

        if (testUrl.startsWith(hostWithSchema)) {
            return true;
        }

        final String domain = getHost(parentUrl);

        if (testUrl.startsWith(domain)) {
            return true;
        }

        return false;
    }

    public static boolean isDescendantOf(String testUrl, String parentUrl) {
        if (!StringUtils.hasText(testUrl) || !StringUtils.hasText(parentUrl)) {
            return false;
        }

        if (testUrl.startsWith(parentUrl)) {
            return true;
        }

        if (testUrl.startsWith("http://") || testUrl.startsWith("https://")) {
            /**
             * So, it's full url with host & schema, and it wasn't matched on prev step... then we may
             * conclude it's not descendant
             */
            return false;
        }

        final String hostWithSchema = getHostWithSchema(parentUrl);

        if (testUrl.startsWith("/")) {
            String fullUrl = hostWithSchema + testUrl;
            return fullUrl.startsWith(parentUrl);
        }

        final String domain = getHost(parentUrl);

        if (testUrl.startsWith(domain)) {
            String fullUrl = hostWithSchema + testUrl.substring(domain.length(), testUrl.length());
            return fullUrl.startsWith(parentUrl);
        }

        /**
         * Return false for relative urls too
         */

        return false;
    }

    public static int calcAbsolutePathAndQueryHash(String url) {
        try {
            final URI uri = new URI(url);
            String path = uri.getPath();

            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            final String query = uri.getQuery();

            if (StringUtils.hasText(query)) {
                String params = Arrays.stream(query.split("&")).sorted().collect(Collectors.joining("&"));
                path = path + "?" + params;
            }

            return path.hashCode();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
