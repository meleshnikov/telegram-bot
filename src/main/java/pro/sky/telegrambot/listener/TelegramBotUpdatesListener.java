package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Value("${telegram.bot.command.start}")
    private String startCommand;

    @Value("${telegram.bot.greeting}")
    private String greeting;

    @Value("${telegram.bot.pattern}")
    private String dateTimeTextPattern;

    private final TelegramBot telegramBot;

    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            // Process your updates here
            String message = update.message().text();
            long chatId = update.message().chat().id();

            if (message.equals(startCommand)) {
                telegramBot.execute(new SendMessage(chatId, greeting));
            } else {
                Matcher matcher = match(dateTimeTextPattern, message);
                if (matcher.matches()) {
                    notificationTaskService.saveTask(matcher, chatId);
                    telegramBot.execute(new SendMessage(chatId, "Task has been saved!"));
                } else {
                    telegramBot.execute(new SendMessage(chatId, "Invalid message!"));
                }
            }

        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private Matcher match(String template, String message) {
        return Pattern.compile(template).matcher(message);
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        Collection<NotificationTask> tasks = notificationTaskService
                .findAllByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        sendNotification(tasks);
    }

    private void sendNotification(Collection<NotificationTask> tasks) {
        tasks.forEach(task ->
                telegramBot.execute(new SendMessage(task.getChatId(), task.toString())));
    }

}
