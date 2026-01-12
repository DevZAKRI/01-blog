package com.zerooneblog.blog.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for sanitizing user input to prevent XSS attacks.
 */
@Component
public class HtmlSanitizer {
    
    /**
     * Policy that strips ALL HTML tags - for plain text fields like usernames, titles
     */
    private static final PolicyFactory STRICT_POLICY = new HtmlPolicyBuilder().toFactory();
    
    /**
     * Policy that allows basic formatting tags - for rich content like post descriptions
     */
    private static final PolicyFactory RICH_TEXT_POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "b", "i", "u", "strong", "em", "ul", "ol", "li", "blockquote", "pre", "code", "h1", "h2", "h3", "h4", "h5", "h6")
            .allowElements("a")
            .allowUrlProtocols("http", "https")
            .allowAttributes("href").onElements("a")
            .requireRelNofollowOnLinks()
            .toFactory();

    /**
     * Sanitize plain text - strips ALL HTML tags.
     * Use for: usernames, post titles, comment text, report reasons
     */
    public String sanitizePlainText(String input) {
        if (input == null) {
            return null;
        }
        // First sanitize HTML, then unescape basic entities for plain text display
        String sanitized = STRICT_POLICY.sanitize(input);
        // The sanitizer may encode some characters, decode them for plain text
        return sanitized
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }
    
    /**
     * Sanitize rich text content - allows safe formatting tags.
     * Use for: post descriptions/content that may contain HTML
     */
    public String sanitizeRichText(String input) {
        if (input == null) {
            return null;
        }
        return RICH_TEXT_POLICY.sanitize(input);
    }
    
    /**
     * Check if text contains potentially dangerous content
     */
    public boolean containsDangerousContent(String input) {
        if (input == null) {
            return false;
        }
        String lower = input.toLowerCase();
        return lower.contains("<script") || 
               lower.contains("javascript:") || 
               lower.contains("onerror=") || 
               lower.contains("onload=") ||
               lower.contains("onclick=") ||
               lower.contains("<iframe") ||
               lower.contains("<object") ||
               lower.contains("<embed");
    }
}
