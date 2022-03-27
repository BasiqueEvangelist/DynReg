package me.basiqueevangelist.dynreg.round;

@FunctionalInterface
public interface DynamicRoundTask {
    void perform(RoundContext ctx);
}
