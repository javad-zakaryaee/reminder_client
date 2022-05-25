package com.example.finalproject.models;

import java.time.LocalTime;

public class Reminder {
        private Long id;
        private String title;
        private String desc;
        private LocalTime time;
        private Boolean done = false;
        private User user;

        public Reminder() {
        }
        public Reminder(Long id, String title, String desc, LocalTime time, Boolean done) {
            this.id = id;
            this.desc = desc;
            this.time = time;
            this.title = title;
            this.done = done;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }

        public Boolean getDone() {
            return done;
        }

        public void setDone(Boolean done) {
            this.done = done;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

    @Override
    public String toString() {
        return  "id=" + id +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", time=" + time +
                ", done=" + done +
                ", user=" + user +
                '}';
    }
}