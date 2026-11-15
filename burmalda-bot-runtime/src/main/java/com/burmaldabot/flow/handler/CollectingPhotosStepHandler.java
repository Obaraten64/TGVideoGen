package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.UserFlowStepHandler;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.flow.session.UserSessionService;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.service.MessageService;
import com.burmaldabot.telegram.TelegramSender;
import com.burmaldabot.telegram.keyboard.TelegramKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollectingPhotosStepHandler implements UserFlowStepHandler {

    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final FlowResponseService responseService;
    private final MessageService messageService;

    @Override
    public UserFlowState getSupportedState() {
        return UserFlowState.COLLECTING_PHOTOS;
    }

    @Override
    public void handleMessage(BotContext context, BotUserSession session, Message message) {
        // Handle cancellation
        if (message.hasText() && message.getText().contains("Отмена")) {
            session.resetToMenu();
            sessionService.save(session);
            telegramSender.sendMessage(
                    context,
                    messageService.getMessage("flow.generate.photos.cancelled"),
                    keyboardFactory.createRemoveReplyKeyboard()
            );
            telegramSender.sendMessage(
                    context,
                    messageService.getMessage("menu.action.choose"),
                    keyboardFactory.createMainMenu()
            );
            return;
        }

        // Handle photo confirmation
        if (message.hasText() && message.getText().contains("Подтвердить")) {
            List<String> photos = session.getPhotoFileIds();
            if (photos == null || photos.isEmpty()) {
                responseService.sendValidationError(
                        context,
                        messageService.getMessage("flow.generate.photos.none_error"),
                        keyboardFactory.createPhotoCollectionReplyKeyboard(0)
                );
                return;
            }

            session.setState(UserFlowState.INPUTTING_PROMPT);
            sessionService.save(session);

            // Remove bottom reply keyboard first
            telegramSender.sendMessage(
                    context,
                    messageService.getMessage("flow.generate.photos.accepted", photos.size()),
                    keyboardFactory.createRemoveReplyKeyboard()
            );

            // Send prompt request with inline templates
            telegramSender.sendMessage(
                    context,
                    messageService.getMessage("flow.generate.step4.prompt", photos.size()),
                    keyboardFactory.createPromptTemplatesKeyboard()
            );
            return;
        }

        // Handle photo reception
        if (message.hasPhoto()) {
            List<PhotoSize> photoSizes = message.getPhoto();
            PhotoSize largest = photoSizes.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(photoSizes.get(photoSizes.size() - 1));

            if (session.getPhotoFileIds().size() >= 10) {
                responseService.sendValidationError(
                        context,
                        messageService.getMessage("flow.generate.photos.max_reached"),
                        keyboardFactory.createPhotoCollectionReplyKeyboard(session.getPhotoFileIds().size())
                );
                return;
            }

            session.addPhoto(largest.getFileId());
            sessionService.save(session);

            int currentCount = session.getPhotoFileIds().size();
            telegramSender.sendMessage(
                    context,
                    messageService.getMessage("flow.generate.photos.received_count", currentCount),
                    keyboardFactory.createPhotoCollectionReplyKeyboard(currentCount)
            );
            return;
        }

        // Unsupported message type in this state: always preserve the reply keyboard
        responseService.sendValidationError(
                context,
                messageService.getMessage("flow.generate.photos.invalid_input"),
                keyboardFactory.createPhotoCollectionReplyKeyboard(session.getPhotoFileIds().size())
        );
    }
}
