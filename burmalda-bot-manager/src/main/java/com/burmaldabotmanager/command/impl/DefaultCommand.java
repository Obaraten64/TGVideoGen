package com.burmaldabotmanager.command.impl;

import com.burmaldabotmanager.command.TelegramCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component("defaultCommand")
@RequiredArgsConstructor
public class DefaultCommand implements TelegramCommand {
    @Override
    public String command() {
        return "";
    }

    @Override
    public SendMessage message() {
        return null;
    }

    @Override
    public String description() {
        return "Default command";
    }
}
