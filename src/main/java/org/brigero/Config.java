package org.brigero;

public class Config {
    public Keybinds keybinds;
    public KeybindsWithCustomOptions CustomOptionsKeybinds;

    public static class Keybinds {
        public String X;
    }

    public static class KeybindsWithCustomOptions implements IEntree<int[]> {
        public String functionName;
        public int[] customOptions;

        @Override
        public int[] getAdditionalOptions() {
            return customOptions;
        }

        @Override
        public String getFunctionName() {
            return functionName;
        }
    }
}




