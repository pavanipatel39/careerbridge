package com.careerbridge.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class JsonTest {
    @Test void readsStringValue() { assertEquals("Riya", Json.get("{\"name\":\"Riya\"}", "name")); }
    @Test void missingKeyGivesEmptyString() { assertEquals("", Json.get("{\"a\":\"b\"}", "x")); }
    @Test void ignoresSpacesAroundColon() { assertEquals("v", Json.get("{ \"k\" :  \"v\" }", "k")); }
    @Test void readsEscapedQuote() { assertEquals("say \"hi\"", Json.get("{\"m\":\"say \\\"hi\\\"\"}", "m")); }
    @Test void quoteEscapesSpecialCharacters() { assertEquals("\"a\\\"b\"", Json.quote("a\"b")); }
    @Test void quoteTurnsNullIntoJsonNull() { assertEquals("null", Json.quote(null)); }
    @Test void quoteEscapesTabAndControlCharacters() { assertEquals("\"a\\tb\\u0001\"", Json.quote("a\tb\u0001")); }
    @Test void getDecodesNewlineTabAndUnicodeEscapes() { assertEquals("a\nb\tc\u00e9", Json.get("{\"k\":\"a\\nb\\tc\\u00e9\"}", "k")); }
    @Test void getReturnsEmptyForNonStringValue() { assertEquals("", Json.get("{\"n\":42}", "n")); }
    @Test void objectBuildsJsonFromMap() { assertEquals("{\"a\":\"b\"}", Json.object(java.util.Map.of("a", "b"))); }
    @Test void emptyBodyGivesEmptyString() { assertEquals("", Json.get("", "k")); }
}
