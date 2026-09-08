package com.burmaldabot.command.impl;

import com.burmaldabot.command.TelegramCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("defaultCommand")
@RequiredArgsConstructor
public class DefaultCommand implements TelegramCommand {
    @Override
    public String command() {
        return "";
    }

    @Override
    public String message() {
        return null;
    }

    @Override
    public String description() {
        return "Default command";
    }
}
