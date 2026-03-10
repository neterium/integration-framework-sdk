package com.neterium.client.sdk.binding;

import com.neterium.client.sdk.model.ScreenableParty;
import com.neterium.sdk.model.JetScanRequestBody;
import com.neterium.sdk.model.JetScanScreenRecord;
import com.neterium.sdk.model.JetScanScreenRecordMetadata;
import com.neterium.sdk.model.JetScanScreenRecordMetadataPlacesInner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

/**
 * {@link Binder} implementation for JetScan.
 * It handles the conversion of {@link ScreenableParty} objects to the input structure
 * claimed by JetScan API (ie {@link JetScanRequestBody})
 *
 * @author Bernard Ligny
 */
@Component
public class JetScanBinder extends BaseBinder<ScreenableParty, JetScanRequestBody> {

    /**
     * Constructor
     */
    public JetScanBinder() {
        super();
    }


    /**
     * @see BaseBinder#bind(Collection)
     */
    @Override
    public JetScanRequestBody bind(Collection<? extends ScreenableParty> items) {
        var jetScanRequestBody = new JetScanRequestBody();
        for (ScreenableParty counterpart : items) {

            var metaData = new JetScanScreenRecordMetadata()
                    .gender(mapEnum(counterpart.getGender(), JetScanScreenRecordMetadata.GenderEnum::fromValue))
                    .dob(counterpart.getDateOfBirth());

            Optional.ofNullable(counterpart.getRegistrationNumber())
                    .ifPresent(metaData::addIdsItem);

            Optional.ofNullable(counterpart.getRegistrationCountryCode())
                    .ifPresent(country -> metaData.addPlacesItem(
                            new JetScanScreenRecordMetadataPlacesInner()
                                    .country(country)
                                    .type(JetScanScreenRecordMetadataPlacesInner.TypeEnum.NATIONALITY)
                    ));

            Optional.ofNullable(counterpart.getAddressCountryCode())
                    .ifPresent(country -> metaData.addPlacesItem(
                            new JetScanScreenRecordMetadataPlacesInner()
                                    .country(country)
                                    .city(counterpart.getAddressCityName())
                                    .type(JetScanScreenRecordMetadataPlacesInner.TypeEnum.ADDRESS)
                    ));

            var fullName = nvl(counterpart.getLastName()) + ", " + nvl(counterpart.getFirstName())
                    + nvl(counterpart.getMiddleNames());

            jetScanRequestBody.addRecordsItem(
                    new JetScanScreenRecord()
                            .reference(counterpart.getId())
                            .type(mapEnum(counterpart.getType(), JetScanScreenRecord.TypeEnum::fromValue))
                            ._object(fullName)
                            .metadata(metaData)
            );
        }
        return jetScanRequestBody;
    }

}
