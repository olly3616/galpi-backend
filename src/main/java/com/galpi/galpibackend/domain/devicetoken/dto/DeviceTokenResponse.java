package com.galpi.galpibackend.domain.devicetoken.dto;

public record DeviceTokenResponse(
        boolean success
) {

    public static DeviceTokenResponse ok() {
        return new DeviceTokenResponse(true);
    }
}
