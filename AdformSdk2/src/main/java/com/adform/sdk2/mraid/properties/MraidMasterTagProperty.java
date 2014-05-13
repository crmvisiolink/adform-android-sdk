package com.adform.sdk2.mraid.properties;

/**
 * Created by mariusm on 08/05/14.
 * A property that holds master id that identifies which ad should be loaded.
 */
public class MraidMasterTagProperty extends MraidBaseProperty {
    private final String mMasterId;

    MraidMasterTagProperty(String masterTag) {
        mMasterId = masterTag;
    }

    public static MraidMasterTagProperty createWithMasterTag(String masterTag) {
        return new MraidMasterTagProperty(masterTag);
    }

    @Override
    public String toGetPair() {
        return "mid="+ mMasterId;
    }
}