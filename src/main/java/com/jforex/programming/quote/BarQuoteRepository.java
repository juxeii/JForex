package com.jforex.programming.quote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.keyvalue.MultiKey;

import com.dukascopy.api.IBar;
import com.jforex.programming.misc.HistoryUtil;

import rx.Observable;

public class BarQuoteRepository {

    private final HistoryUtil historyUtil;
    private final Map<MultiKey<Object>, BarQuote> barQuotes = new ConcurrentHashMap<>();

    public BarQuoteRepository(final Observable<BarQuote> barQuoteObservable,
                              final HistoryUtil historyUtil) {
        this.historyUtil = historyUtil;

        barQuoteObservable.subscribe(this::onBarQuote);
    }

    private void onBarQuote(final BarQuote barQuote) {
        final MultiKey<Object> quoteKey = quoteKey(barQuote.barParams());
        barQuotes.put(quoteKey, barQuote);
    }

    private MultiKey<Object> quoteKey(final BarParams barParams) {
        return new MultiKey<Object>(barParams.instrument(),
                                    barParams.period(),
                                    barParams.offerSide());
    }

    public BarQuote get(final BarParams barParams) {
        final MultiKey<Object> quoteKey = quoteKey(barParams);
        return barQuotes.containsKey(quoteKey)
                ? barQuotes.get(quoteKey)
                : quoteFromHistory(barParams);
    }

    private BarQuote quoteFromHistory(final BarParams barParams) {
        final IBar historyBar = historyUtil.barQuote(barParams);
        final BarQuote barQuote = new BarQuote(historyBar, barParams);

        onBarQuote(barQuote);

        return barQuote;
    }
}
