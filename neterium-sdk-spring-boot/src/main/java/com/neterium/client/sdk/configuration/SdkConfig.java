package com.neterium.client.sdk.configuration;

import com.neterium.client.sdk.binding.JetFlowBinder;
import com.neterium.client.sdk.binding.JetScanBinder;
import com.neterium.client.sdk.converters.Pacs008Converter;
import com.neterium.client.sdk.converters.SwiftFinMT103Converter;
import com.neterium.client.sdk.converters.SwiftFinMT541Converter;
import com.neterium.client.sdk.files.FileService;
import com.neterium.client.sdk.matching.MatchVerifierClientImpl;
import com.neterium.client.sdk.matching.MatchVerifierPassThroughImpl;
import com.neterium.client.sdk.privatelist.PrivateListBuilder;
import com.neterium.client.sdk.privatelist.PrivateListTemplate;
import com.neterium.client.sdk.properties.SdkProperties;
import com.neterium.client.sdk.screening.CounterpartScreener;
import com.neterium.client.sdk.screening.ExceptionTemplate;
import com.neterium.client.sdk.screening.ScreeningTemplate;
import com.neterium.client.sdk.screening.TransactionScreener;
import com.neterium.client.sdk.security.OAuth2Client;
import com.neterium.client.sdk.security.TokenServiceEagerImpl;
import com.neterium.client.sdk.security.TokenServiceLazyImpl;
import com.neterium.client.sdk.session.SessionManager;
import com.neterium.client.sdk.throttling.ThrottlerImpl;
import com.neterium.client.sdk.utils.JsonHelper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * All components of the SDK that are eligible for Spring container
 *
 * @author Bernard Ligny
 */
@Configuration(proxyBeanMethods = false)
@Import({
        FileService.class,
        CounterpartScreener.class,
        TransactionScreener.class,
        JetScanBinder.class,
        JetFlowBinder.class,
        JsonHelper.class,
        Pacs008Converter.class,
        SwiftFinMT103Converter.class,
        SwiftFinMT541Converter.class,
        ScreeningTemplate.class,
        SessionManager.class,
        OAuth2Client.class,
        TokenServiceLazyImpl.class,
        TokenServiceEagerImpl.class,
        ThrottlerImpl.class,
        MatchVerifierClientImpl.class,
        MatchVerifierPassThroughImpl.class,
        ExceptionTemplate.class,
        PrivateListBuilder.class,
        PrivateListTemplate.class
})
@EnableConfigurationProperties(SdkProperties.class)
public class SdkConfig {

    /**
     * Constructor
     */
    public SdkConfig() {
    }

}
