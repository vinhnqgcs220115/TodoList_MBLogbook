// Plan.java
package com.example.todolist;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plans")
public class Plan implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private String targetGoal;
    private String createDate;
    private String deadlineDate;
    private String orientation;
    private int progress;

    public Plan(String title, String description, String targetGoal, String createDate, String deadlineDate, String orientation) {
        this.title = title;
        this.description = description;
        this.targetGoal = targetGoal;
        this.createDate = createDate;
        this.deadlineDate = deadlineDate;
        this.orientation = orientation;
        this.progress = 0;
    }

    protected Plan(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        targetGoal = in.readString();
        createDate = in.readString();
        deadlineDate = in.readString();
        orientation = in.readString();
        progress = in.readInt();
    }

    public static final Creator<Plan> CREATOR = new Creator<Plan>() {
        @Override
        public Plan createFromParcel(Parcel in) {
            return new Plan(in);
        }

        @Override
        public Plan[] newArray(int size) {
            return new Plan[size];
        }
    };

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTargetGoal() { return targetGoal; }
    public void setTargetGoal(String targetGoal) { this.targetGoal = targetGoal; }
    public String getCreateDate() { return createDate; }
    public String getDeadlineDate() { return deadlineDate; }
    public void setDeadlineDate(String deadlineDate) { this.deadlineDate = deadlineDate; }
    public String getOrientation() { return orientation; }
    public void setOrientation(String orientation) { this.orientation = orientation; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(targetGoal);
        dest.writeString(createDate);
        dest.writeString(deadlineDate);
        dest.writeString(orientation);
        dest.writeInt(progress);
    }
}