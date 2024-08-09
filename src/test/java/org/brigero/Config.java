package org.brigero;

public class Config {
    public KeybindsCustom X;

    public static class KeybindsCustom implements IEntree {
        public String functionName;

        public int[] numbers;
        public double[] doubles;

        @Override
        public Object[] getAdditionalOptions() {
            return new Object[] {numbers, doubles};
        }

        @Override
        public String getFunctionName() {
            return functionName;
        }
    }
}
