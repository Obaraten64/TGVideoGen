package com.burmaldabotmanager.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Contract for telegram commands
 */
public interface TelegramCommand {
    /**
     * @return Command itself. EXMPL: /vpn
     */
    String command();
    /**
     * @return Result of the command
     */
    SendMessage message();

    /**
     * @return Command description
     */
    String description();
}
