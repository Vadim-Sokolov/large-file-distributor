package large.file.distributor;

import java.util.Random;

public class TextGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "abcdefghijklmnopqrstuvwxyz"
            + "0123456789"
            + "!@#$%^&*()-_=+[]{}|;:'\",.<>?/\\`~";

    private static final Random random = new Random();

    public static void main(String[] args) {
        int textSize = 12288;
        String randomText = generateRandomText(textSize + (textSize / 50));
        System.out.println(randomText);
    }

    private static String generateRandomText(int textSize) {
        var stringBuilder = new StringBuilder(textSize);

        for (int i = 0; i < textSize; i++) {
            stringBuilder.append(CHARS.charAt(random.nextInt(CHARS.length())));
            if ((i + 1) % 50 == 0) {
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
