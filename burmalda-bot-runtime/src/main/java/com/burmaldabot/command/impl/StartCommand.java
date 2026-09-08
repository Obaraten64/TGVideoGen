package com.burmaldabot.command.impl;

import com.burmaldabot.command.TelegramCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartCommand implements TelegramCommand {
    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String message() {
        return "Send a photo of your girl and we will strip her naked";
    }

    @Override
    public String description() {
        return "Start command";
    }
}
