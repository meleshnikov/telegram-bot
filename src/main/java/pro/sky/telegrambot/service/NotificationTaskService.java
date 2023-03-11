package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.regex.Matcher;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    private NotificationTask createTask(Matcher matcher, long chatId) {
        NotificationTask task = new NotificationTask();
        String dateTime = matcher.group(1);
        String notification = matcher.group(3);
        task.setDateTime(LocalDateTime.parse(dateTime,
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        task.setNotification(notification);
        task.setChatId(chatId);
        return task;
    }

    public void saveTask(Matcher matcher, long chatId) {
        notificationTaskRepository.save(createTask(matcher, chatId));
    }

    public Collection<NotificationTask> findAllByDateTime(LocalDateTime dateTime) {
        return notificationTaskRepository.findAllByDateTime(dateTime);
    }

}
