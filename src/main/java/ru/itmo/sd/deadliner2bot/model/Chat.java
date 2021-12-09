package ru.itmo.sd.deadliner2bot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "chats")
public class Chat {

    @Id
    @Column(name = "chat_id")
    private long chatId;

    @OneToMany(mappedBy = "chat")
    private Set<Todo> todos = new TreeSet<>();

    @OneToMany(mappedBy = "chat")
    private Set<DailyNotification> dailyNotifications = new TreeSet<>();
}
