package com.jmorla.tradingboot.strategies;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.WilliamsRIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;


/**
 * Williams%R strategy
 *
 * if w%r goes down to -85 and then after goes up to 40 then create a signal
 *
 * @author jmorla
 * */
public class WilliamsRStrategyFactory {

    private final ClosePriceIndicator closePriceIndicator;
    private final HighPriceIndicator highPriceIndicator;
    private final LowPriceIndicator lowPriceIndicator;
    private final WilliamsRIndicator williamsRIndicator;
    private final LowestValueIndicator lowestWRValue;

    public WilliamsRStrategyFactory(BarSeries series) {
        this.closePriceIndicator = new ClosePriceIndicator(series);
        this.highPriceIndicator = new HighPriceIndicator(series);
        this.lowPriceIndicator = new LowPriceIndicator(series);
        this.williamsRIndicator = new WilliamsRIndicator(closePriceIndicator, 14, highPriceIndicator, lowPriceIndicator);
        this.lowestWRValue = new LowestValueIndicator(williamsRIndicator, 14);
    }

    private Rule defineEntryRule() {
        Rule wrOverMinus40Rule = new OverIndicatorRule(williamsRIndicator, -42);
        Rule wrLowestUnderMinus80Rule = new UnderIndicatorRule(lowestWRValue, -85);
        return  wrOverMinus40Rule.and(wrLowestUnderMinus80Rule);
    }

    private Rule defineExitRule() {
        Rule wrUnderMinus40Rule = new UnderIndicatorRule(williamsRIndicator, -43)
                .or(new StopLossRule(closePriceIndicator, 5));

        return wrUnderMinus40Rule;
    }

    public static Strategy getDefault(BarSeries series) {
        WilliamsRStrategyFactory williamsRStrategy = new WilliamsRStrategyFactory(series);
        Rule entryRule = williamsRStrategy.defineEntryRule();
        Rule exitRule = williamsRStrategy.defineExitRule();

        return new BaseStrategy(entryRule, exitRule);
    }
}
