package gitlet;


import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
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
    public String message_;

    /** The reference of parent commit in SHA1 ID form. */
    public String parent_commit_;

    /** Possible reference of second parent commit. */
    public String second_parent_commit_;

    /** The date and time of this commit. */
    public Date date_and_time_;
/** The ID of this commit. */
    public String id_;

    /** The tracked content of this commit. */
    public HashMap<String, String> tracked_ = new HashMap<>();

    public Commit(String message, String parent_commit, String second_parent_commit) {
        this.message_ = message;
        this.parent_commit_ = parent_commit;
        this.second_parent_commit_ = second_parent_commit;
        this.date_and_time_ = new Date();
    }

    public void initialDateTime() {
        this.date_and_time_.setTime(0);
    }

    public void updateID() {
        id_ = Utils.sha1(this.date_and_time_.toString(), this.message_, this.parent_commit_, this.second_parent_commit_, this.tracked_.toString());
    }

}
