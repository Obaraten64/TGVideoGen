package com.burmaldabot.model.files;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AllowedExtensions {
    MP4("mp4"), JPG("jpg");

    private final String extension;


}
