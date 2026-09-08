package com.burmaldabot.command;

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
    String message();

    /**
     * @return Command description
     */
    String description();
}
