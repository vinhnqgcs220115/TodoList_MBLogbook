// Task.java (todoList)
package com.example.todolist;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String notes;
    private String createDate;
    private String priority;
    private boolean completed;

    public Task(String title, String notes, String createDate, boolean completed) {
        this.title = title;
        this.notes = notes;
        this.createDate = createDate;
        this.completed = completed;
        this.priority = "Medium";
    }

    protected Task(Parcel in) {
        id = in.readInt();
        title = in.readString();
        notes = in.readString();
        createDate = in.readString();
        priority = in.readString();
        completed = in.readByte() != 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(notes);
        dest.writeString(createDate);
        dest.writeString(priority);
        dest.writeByte((byte) (completed ? 1 : 0));
    }
}