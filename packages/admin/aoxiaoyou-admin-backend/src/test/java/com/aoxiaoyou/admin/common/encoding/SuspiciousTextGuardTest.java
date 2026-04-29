package com.aoxiaoyou.admin.common.encoding;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuspiciousTextGuardTest {

    @Test
    void ignoresNormalMultilingualContent() {
        SamplePayload payload = new SamplePayload();
        payload.nameZht = "澳門葡京人";
        payload.descriptionPt = "Rua da Patinagem, Cotai, Macau";

        assertTrue(SuspiciousTextGuard.findFirstIssue(payload).isEmpty());
    }

    @Test
    void detectsQuestionMarkReplacement() {
        SamplePayload payload = new SamplePayload();
        payload.nameZht = "????";

        Optional<SuspiciousTextIssue> issue = SuspiciousTextGuard.findFirstIssue(payload);

        assertTrue(issue.isPresent());
        assertEquals("nameZht", issue.get().path());
    }

    @Test
    void detectsUtf8Mojibake() {
        SamplePayload payload = new SamplePayload();
        payload.nameZht = "æ¾³éè¡äº¬äºº";

        Optional<SuspiciousTextIssue> issue = SuspiciousTextGuard.findFirstIssue(payload);

        assertTrue(issue.isPresent());
        assertEquals("nameZht", issue.get().path());
    }

    @Test
    void skipsQuestionMarksInsideJsonPayloadField() {
        Optional<SuspiciousTextIssue> issue = SuspiciousTextGuard.findFirstIssue(
                Map.of("displayConfigJson", "{\"help\":\"What???\"}")
        );

        assertFalse(issue.isPresent());
    }

    private static final class SamplePayload {
        private String nameZht;
        private String descriptionPt;
    }
}
