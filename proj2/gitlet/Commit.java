package gitlet;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/** Represents a gitlet commit object.
 *  FIXME: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author D
 */
public class Commit implements Serializable {
    /**
     * FIXME: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** The reference of parent commit in SHA1 ID form. Empty string if none. */
    private String parent;

    /** Possible reference of second parent commit. Empty string if none. */
    private String secondParent;

    /** The date and time of this commit. */
    private Date timestamp;

    /** The ID of this commit. */
    private String id;

    /** The tracked content of this commit. */
    private Map<String, String> tracked;

    public Commit(String message, String parent, String secondParent, Map<String, String> tracked) {
        this.message = message;
        this.parent = parent;
        this.secondParent = secondParent;
        this.timestamp = new Date();
        this.tracked = tracked;
    }

    public void initialDateTime() {
        this.timestamp.setTime(0);
    }

    public void updateID() {
        id = Utils.sha1(String.valueOf(this.timestamp.getTime()),
                this.message, this.parent, this.secondParent,
                this.tracked.toString());
    }

    public String getParentID() {
        return parent;
    }

    public String getSecondParentID() {
        return secondParent;
    }

    public Map<String, String> getTracked() {
        return Collections.unmodifiableMap(tracked);
    }

    public String getID() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isInitialCommit() {
        return parent.isEmpty();
    }

    public boolean isMergeCommit() {
        return !secondParent.isEmpty();
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        if (!secondParent.equals("")) {
            return String.format("===\ncommit %s\nMerge: %.7s %.7s\n"
                    + "Date: %s\n%s\n",
                    id, parent, secondParent, sdf.format(timestamp), message);
        }
        return String.format("===\ncommit %s\nDate: %s\n%s\n",
                id, sdf.format(timestamp), message);
    }
}
