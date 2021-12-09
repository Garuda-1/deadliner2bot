package ru.itmo.sd.deadliner2bot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "todos")
public class Todo implements Comparable<Todo> {

    @Id
    @GeneratedValue
    @Column(name = "todo_id")
    private long todoId;

    @Column(name = "name")
    private String name;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "completed")
    private boolean completed;

    @Column(name = "daily_notifications_enabled")
    private boolean dailyNotificationsEnabled;

    @OneToMany(mappedBy = "todo")
    private Set<TodoNotification> todoNotifications = new TreeSet<>();

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Override
    public int compareTo(Todo other) {
        return endTime.compareTo(other.endTime);
    }
}
