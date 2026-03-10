package com.neterium.client.sdk.screening;

import com.neterium.client.sdk.model.ScreenableParty;
import com.neterium.client.sdk.model.ScreenableTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is the central delegate in the screening package.
 * This class simplifies screening operations on counterparts as well as transactions,
 * by eliminating boilerplate code that is needed to invoke Neterium screening API.
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class ScreeningTemplate {

    private final CounterpartScreener counterpartScreener;
    private final TransactionScreener transactionScreener;


    /**
     * Constructor
     *
     * @param counterpartScreener a {@link CounterpartScreener} instance
     * @param transactionScreener a {@link TransactionScreener} instance
     */
    public ScreeningTemplate(CounterpartScreener counterpartScreener,
                             TransactionScreener transactionScreener) {
        this.counterpartScreener = counterpartScreener;
        this.transactionScreener = transactionScreener;
    }


    // === Name screening ===

    /**
     * Screen multiple counterparts
     *
     * @param counterparts the counterparts to screen
     * @param collection   sanction to screen against
     * @param threshold    the screening threshold
     * @param <T>          a {@link  ScreenableParty} implementation
     * @return a map of (screened counterpart, screening-response)
     */
    public <T extends ScreenableParty> Map<T, ScreeningResponseItem> screenNames(Collection<T> counterparts,
                                                                                 String collection,
                                                                                 int threshold) {
        return this.screenNames(counterparts, collection, threshold, null);
    }


    /**
     * Screen multiple counterparts inside an existing session
     *
     * @param counterparts the counterparts to screen
     * @param collection   sanction to screen against
     * @param threshold    the screening threshold
     * @param sessionId    id of screening session
     * @param <T>          a {@link  ScreenableParty} implementation
     * @return a map of (screened counterpart, screening-response)
     */
    public <T extends ScreenableParty> Map<T, ScreeningResponseItem> screenNames(Collection<T> counterparts,
                                                                                 String collection,
                                                                                 int threshold,
                                                                                 String sessionId) {
        var request = new ScreeningRequest<ScreenableParty>(collection, counterparts);
        var response = counterpartScreener.doScreen(request, threshold, sessionId);
        return counterparts
                .stream()
                .collect(
                        Collectors.toMap(Function.identity(),
                                c -> response.getByReference(c.getId())
                        ));
    }


    // === Transaction screening ===

    /**
     * Screen a single transaction
     *
     * @param transaction the transaction to screen
     * @param collection  sanction to screen against
     * @param threshold   the screening threshold
     * @param <T>         a {@link  ScreenableTransaction} implementation
     * @return the screening result
     */
    public <T extends ScreenableTransaction<?>> ScreeningResponseItem screenTransaction(T transaction,
                                                                                        String collection,
                                                                                        int threshold) {
        return this.screenTransaction(transaction, collection, threshold, null);
    }


    /**
     * Screen a single transaction inside an existing session
     *
     * @param transaction the transaction to screen
     * @param collection  sanction to screen against
     * @param threshold   the screening threshold
     * @param sessionId   id of screening session
     * @param <T>         a {@link  ScreenableTransaction} implementation
     * @return the screening result
     */
    public <T extends ScreenableTransaction<?>> ScreeningResponseItem screenTransaction(T transaction,
                                                                                        String collection,
                                                                                        int threshold,
                                                                                        String sessionId) {
        return this.screenTransactions(List.of(transaction), collection, threshold, sessionId)
                .get(transaction);
    }


    /**
     * Screen multiple transactions
     *
     * @param transactions the transactions to screen
     * @param collection   sanction to screen against
     * @param threshold    the screening threshold
     * @param <T>          a {@link  ScreenableTransaction} implementation
     * @return a map of (screened transaction, screening-response)
     */
    public <T extends ScreenableTransaction<?>> Map<T, ScreeningResponseItem> screenTransactions(Collection<T> transactions,
                                                                                                 String collection,
                                                                                                 int threshold) {
        return this.screenTransactions(transactions, collection, threshold, null);
    }


    /**
     * Screen multiple transactions inside an existing session
     *
     * @param transactions the transactions to screen
     * @param collection   sanction to screen against
     * @param threshold    the screening threshold
     * @param sessionId    id of screening session
     * @param <T>          a {@link  ScreenableTransaction} implementation
     * @return a map of (screened transaction, screening-response)
     */
    public <T extends ScreenableTransaction<?>> Map<T, ScreeningResponseItem> screenTransactions(Collection<T> transactions,
                                                                                                 String collection,
                                                                                                 int threshold,
                                                                                                 String sessionId) {
        var request = new ScreeningRequest<ScreenableTransaction<?>>(collection, transactions);
        var response = transactionScreener.doScreen(request, threshold, sessionId);
        return transactions
                .stream()
                .collect(
                        Collectors.toMap(Function.identity(),
                                c -> response.getByReference(c.getPaymentRef())
                        ));
    }

}
