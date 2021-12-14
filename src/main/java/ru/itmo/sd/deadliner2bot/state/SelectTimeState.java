package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.DailyNotification;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.service.DailyNotificationService;
import ru.itmo.sd.deadliner2bot.ui.commands.CommandInfo;
import ru.itmo.sd.deadliner2bot.ui.commands.Commands;
import ru.itmo.sd.deadliner2bot.ui.messages.StateMessages;
import ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils;
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils.timeFormat;

@Component
@RequiredArgsConstructor
public class SelectTimeState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.SELECT_TIME_STATE;
    private final MessageUtils messageUtils;
    private final DateTimeUtils dateTimeUtils;
    private final DailyNotificationService dailyNotificationService;
    private final Commands commands;
    private final StateMessages stateMessages;
    private Map<String, CommandInfo> commandsInfo;

    @PostConstruct
    public void postConstruct() {
        commandsInfo = commands.loadAllCommandsInfo(getChatStateEnum(), List.of(
                "cancel"
        ));
    }

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        if (commandsInfo.get("cancel").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "cancel")));
        } else if (message.startsWith("/")) {
            return null;
        }

        LocalTime time = dateTimeUtils.parseTime(message);
        if (time == null) {
            return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "invalid-time-format", timeFormat)));
        }

        Set<DailyNotification> toUpdate = dailyNotificationService
                .findDailyNotificationsByChatStartingWithDate(chat.getChatId(), dateTimeUtils.getStagingWeekAuxStartDateTime());
        toUpdate.forEach(
                notification -> {
                    LocalDateTime date = notification.getNotificationTime();
                    date = dateTimeUtils.setTimeAndConfirmed(date, time);
                    notification.setNotificationTime(date);
                    dailyNotificationService.save(notification);
                }
        );
        chat.setState(ChatStateEnum.BASE_STATE);
        chatRepository.save(chat);
        return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "time-set", time)));
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
