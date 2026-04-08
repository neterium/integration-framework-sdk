package com.neterium.client.sdk.privatelist;

import com.neterium.sdk.ofac.ObjectFactory;
import com.neterium.sdk.ofac.SdnList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Collection;
import java.util.Optional;

/**
 * A wrapper around a {@link SdnList.SdnEntry} JAXB bean
 * for easy population of its properties via reflection
 *
 * @author Bernard Ligny
 */
@Slf4j
public class SdnEntryWrapper {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private final BeanWrapper bean;

    /**
     * Constructor
     */
    public SdnEntryWrapper() {
        var pojo = OBJECT_FACTORY.createSdnListSdnEntry();
        bean = new BeanWrapperImpl(pojo);
        bean.setAutoGrowNestedPaths(true);
    }


    /**
     * Set the value of a (simple, nested or indexed) property
     *
     * @param path  full path of bean property
     * @param value value of property
     */
    public void set(String path, String value) {
        log.trace("- {} = \"{}\" ", path, value);
        bean.setPropertyValue(path, value);
    }


    /**
     * Get the current size of the collection owning an indexed property
     *
     * @param path full path of bean property
     * @return the size of collection
     */
    public Optional<Integer> getSizeIfCollection(String path) {
        var prefix = StringUtils.substringBeforeLast(path, "[");
        var list = bean.getPropertyValue(prefix);
        if (list instanceof Collection<?>) {
            var size = ((Collection<?>) list).size();
            return Optional.of(size);
        } else {
            return Optional.empty();
        }
    }


    /**
     * Unwrap nested SdnEntry bean
     *
     * @return populated SdnEntry instance
     */
    public SdnList.SdnEntry unwrap() {
        return (SdnList.SdnEntry) bean.getWrappedInstance();
    }

}
