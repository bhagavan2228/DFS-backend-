package com.fourm.discussion_forum.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class ToxicityService {

    // Mocked list of toxic words (banned terms)
    private static final List<String> BANNED_WORDS = Arrays.asList(
        "spam", "hate", "kill", "attack", "scam", "offensive_word_placeholder"
    );

    /**
     * Checks if the provided text contains any toxic or banned content.
     * In a real AI implementation, this would call an external API (e.g., Perspective API).
     */
    public boolean isToxic(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String lowercaseText = text.toLowerCase();
        for (String word : BANNED_WORDS) {
            if (lowercaseText.contains(word)) {
                return true;
            }
        }
        
        return false;
    }
}
