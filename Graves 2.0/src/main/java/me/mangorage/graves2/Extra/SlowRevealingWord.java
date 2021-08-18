package me.mangorage.graves2.Extra;

import java.util.HashMap;

public class SlowRevealingWord {
    private String Word = "";
    private HashMap<Integer, RevealLetter> Letters = new HashMap<>();
    














    public static class RevealLetter {
        public String Letter = "";
        public boolean hidden = true;

        RevealLetter(String LetterB) {
            Letter = LetterB;
        }

        public void toggle() {
            hidden = !hidden;
        }
    }
}
