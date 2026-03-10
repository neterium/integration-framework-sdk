package com.neterium.client.sdk.binding;

import com.neterium.client.sdk.model.ScreenableTransaction;
import com.neterium.sdk.model.*;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@link Binder} implementation for JetFlow.
 * It handles the conversion of {@link ScreenableTransaction} objects to the input structure
 * claimed by JetFlow API (ie {@link JetFlowRequestBody})
 *
 * @author Bernard Ligny
 */
@Component
public class JetFlowBinder extends BaseBinder<ScreenableTransaction<?>, JetFlowRequestBody> {

    /**
     * Constructor
     */
    public JetFlowBinder() {
        super();
    }


    /**
     * @see BaseBinder#bind(Collection)
     */
    @Override
    public JetFlowRequestBody bind(Collection<? extends ScreenableTransaction<?>> items) {
        var jetFlowRequestBody = new JetFlowRequestBody();
        for (ScreenableTransaction<?> transaction : items) {
            var destCreditor = bindParty(transaction.getCreditor());
            var destDebtor = bindParty(transaction.getDebtor());
            var item = new JetFlowRequestBodyRecords()
                    .type(JetFlowRequestBodyRecords.TypeEnum.TRANSACTION)
                    .reference(transaction.getPaymentRef())
                    ._object(new JetFlowTransaction()
                            .transactionId(transaction.getPaymentRef())
                            .transactionType(transaction.getType())
                            .currency(transaction.getCurrency())
                            .amount(Optional.ofNullable(transaction.getAmount())
                                    .map(BigDecimal::doubleValue)
                                    .orElse(null)
                            )
                            .purpose(transaction.getPurpose())
                            .creditor(destCreditor)
                            .debtor(destDebtor)
                    );
            transaction.getIntermediaries()
                    .stream()
                    .map(this::bindParty)
                    .filter(this::hasSomeId)
                    .forEach(p -> item.getObject().addIntermediariesItem(p)
                    );
            jetFlowRequestBody.addRecordsItem(item);
        }
        return jetFlowRequestBody;
    }


    private JetFlowParty bindParty(ScreenableTransaction.Party srcParty) {
        if (srcParty == null) {
            return null;
        }
        var destParty = new JetFlowParty()
                .name(srcParty.getFullName())
                .label(srcParty.getLabel())
                .text(srcParty.getText());
        if (isNotEmpty(srcParty.getAccountNumber())) {
            destParty.account(new JetFlowAccount()
                    .id(srcParty.getAccountNumber())
                    .type(srcParty.getAccountType())
            );
        }
        if (isNotEmpty(srcParty)) {
            var addr = new JetFlowAddress()
                    .country(srcParty.getCountryCode())
                    .postalCode(srcParty.getAddressPostalCode())
                    .city(srcParty.getAddressCity());
            if (isNotEmpty(srcParty.getAddressLine())) {
                addr.addAddressLinesItem(srcParty.getAddressLine());
            }
            destParty.addPostalAddressItem(addr);
        }
        if (hasAnyOf(srcParty.getIdType(), srcParty.getIdValue())) {
            destParty.addIdsItem(new JetFlowId()
                    .value(srcParty.getIdValue())
                    .type(srcParty.getIdType().equalsIgnoreCase("BIC") ? JetFlowId.TypeEnum.BIC :
                            JetFlowId.TypeEnum.OTHER)
            );
        }
        return destParty;

    }


    private boolean isNotEmpty(ScreenableTransaction.Party party) {
        return hasAnyOf(party.getCountryCode(),
                party.getAddressPostalCode(),
                party.getAddressCity(),
                party.getAddressLine());
    }

    private boolean hasAnyOf(Object... elements) {
        return Stream.of(elements).anyMatch(this::isNotEmpty);
    }

    private boolean isNotEmpty(Object obj) {
        return !ObjectUtils.isEmpty(obj);
    }


    private boolean hasSomeId(JetFlowParty party) {
        return party.getIds()
                .stream()
                .anyMatch(p -> p.getValue() != null);
    }

}
