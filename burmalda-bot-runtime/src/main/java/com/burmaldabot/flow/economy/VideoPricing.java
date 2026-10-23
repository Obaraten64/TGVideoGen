package com.burmaldabot.flow.economy;

public class VideoPricing {

    public static final int RATE_480P = 14;
    public static final int RATE_720P = 28;
    public static final int RATE_1080P = 56;

    public static int getRatePerSecond(String resolution) {
        if (resolution == null) {
            return RATE_480P;
        }
        return switch (resolution.toLowerCase().trim()) {
            case "720p" -> RATE_720P;
            case "1080p" -> RATE_1080P;
            default -> RATE_480P;
        };
    }

    public static long calculateTotalCost(String resolution, int durationSeconds, boolean isFreeTrial) {
        if (isFreeTrial) {
            return 0L;
        }
        return (long) getRatePerSecond(resolution) * durationSeconds;
    }
}
