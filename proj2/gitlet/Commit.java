package gitlet;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import static gitlet.Utils.sha1;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author D
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message_;

    /** The reference of parent commit in SHA1 ID form. Empty string if none. */
    private String parent_;

    /** Possible reference of second parent commit. Empty string if none. */
    private String second_parent_;

    /** The date and time of this commit. */
    private Date timestamp_;

    /** The ID of this commit. */
    private String id_;

    /** The tracked content of this commit. */
    private Map<String, String> tracked_;

    public Commit(String message, String parent, String second_parent, Map<String, String> tracked) {
        this.message_ = message;
        this.parent_ = parent;
        this.second_parent_ = second_parent;
        this.timestamp_ = new Date();
        this.tracked_ = tracked;
    }

    public void initialDateTime() {
        this.timestamp_.setTime(0);
    }

    public void updateID() {
        id_ = Utils.sha1(this.timestamp_.toString(), this.message_, this.parent_, this.second_parent_, this.tracked_.toString());
    }

    public String getParentID() {
        return parent_;
    }

    public String getSecondParentID() {
        return second_parent_;
    }

    public Map<String, String> getTracked() {
        return Collections.unmodifiableMap(tracked_);
    }

    public String getID() {
        return id_;
    }

    public String getMessage() {
        return message_;
    }

    public Date getTimestamp() {
        return timestamp_;
    }

    public boolean isInitialCommit() {
        return parent_.isEmpty();
    }

    public boolean isMergeCommit() {
        return !second_parent_.isEmpty();
    }

    public String toString() {
        if (!second_parent_.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            return String.format("===\ncommit %s\nMerge: %.7s %.7s\nDate: %s\n%s\n", id_, parent_, second_parent_, sdf.format(timestamp_), message_);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return String.format("===\ncommit %s\nDate: %s\n%s\n", id_, sdf.format(timestamp_), message_);
    }
}
