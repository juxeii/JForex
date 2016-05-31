package com.jforex.programming.test.common;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.programming.builder.OrderParams;
import com.dukascopy.api.Instrument;

public class OrderParamsForTest extends CommonUtilForTest {

    public static OrderParams paramsBuyEURUSD() {
        return OrderParams.forInstrument(Instrument.EURUSD)
                .withOrderCommand(OrderCommand.BUY)
                .withAmount(0.1)
                .withLabel("TestBuyLabelEURUSD")
                .price(userSettings.defaultOpenPrice())
                .slippage(userSettings.defaultSlippage())
                .stopLossPrice(1.32456)
                .takeProfitPrice(1.32556)
                .goodTillTime(userSettings.defaultGTT())
                .comment("Test comment for buy EURUSD")
                .build();
    }

    public static OrderParams paramsSellEURUSD() {
        return OrderParams.forInstrument(Instrument.EURUSD)
                .withOrderCommand(OrderCommand.SELL)
                .withAmount(0.12)
                .withLabel("TestSellLabelEURUSD")
                .price(userSettings.defaultOpenPrice())
                .slippage(userSettings.defaultSlippage())
                .stopLossPrice(1.32456)
                .takeProfitPrice(1.32556)
                .goodTillTime(userSettings.defaultGTT())
                .comment("Test comment for sell EURUSD")
                .build();
    }

    public static OrderParams paramsUSDJPY() {
        return OrderParams.forInstrument(Instrument.USDJPY)
                .withOrderCommand(OrderCommand.SELL)
                .withAmount(0.1)
                .withLabel("TestLabelUSDJPY")
                .price(userSettings.defaultOpenPrice())
                .slippage(userSettings.defaultSlippage())
                .stopLossPrice(132.456)
                .takeProfitPrice(132.556)
                .goodTillTime(userSettings.defaultGTT())
                .comment("Test comment for USDJPY")
                .build();
    }
}
