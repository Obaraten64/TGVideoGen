package com.burmaldabot.telegram.keyboard.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyboardDefinition {
    private KeyboardType type;
    @Builder.Default
    private Boolean resizeKeyboard = true;
    @Builder.Default
    private Boolean oneTimeKeyboard = false;
    @Builder.Default
    private List<List<ButtonConfig>> rows = new ArrayList<>();
}
