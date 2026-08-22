package gitlet;

import java.io.File;
import java.util.*;
import gitlet.Utils.*;
import gitlet.Stage.*;
import gitlet.CommitTree.*;
import gitlet.References.*;

class StuUtils {
    static String findCommit(String text, Context context) {
        String result = context.commit_tree.matchCommit(text);
        if (result.equals("")) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        return result;
    }

    static List<String> scanUntrackedFiles(Context context) {
        List<String> files = new ArrayList<>();
        Map<String, String> deletion_stage = context.stage.getRemovals();
        for (Map.Entry<String, String> entry : deletion_stage.entrySet()) {
            String filename = entry.getKey();
            File file = Utils.join(context.cwd, filename);
            if (file.exists()) {
                files.add(filename);
            }
        }
        
        List<String> dir_files = Utils.plainFilenamesIn(context.cwd);
        for (String filename : dir_files) {
            if (!context.headCommit().getTracked().containsKey(filename) && !context.stage.isStagedAddition(filename)) {
                files.add(filename);
            }
        }
        Collections.sort(files);

        return files;
    }

    static HashMap<String, Integer> findParentsDistance(String commit, Context context) {
        Queue<String> queue = new LinkedList<>();
        HashMap<String, Integer> distances = new HashMap<>();

        queue.add(commit);
        distances.put(commit, 0);

        while (!queue.isEmpty()) {
            String id = queue.remove();
            Integer parent_distance = distances.get(id) + 1;
            Commit curr_commit = context.commit_tree.getCommit(id);
            String first_parent = curr_commit.getParentID();
            String second_parent = curr_commit.getSecondParentID();

            if (!first_parent.isEmpty() && !distances.containsKey(first_parent)) {
                queue.add(first_parent);
                distances.put(first_parent, parent_distance);
            }
            if (!second_parent.isEmpty() && !distances.containsKey(second_parent)) {
                queue.add(second_parent);
                distances.put(second_parent, parent_distance);
            }
        }

        return distances;
    }


    static String findSplitPoint(String first_commit, String second_commit, Context context) {
        HashMap<String, Integer> first_parent_distances = findParentsDistance(first_commit, context);
        HashMap<String, Integer> second_parent_distances = findParentsDistance(second_commit, context);

        String best_parent = null;
        Integer best_distance = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : first_parent_distances.entrySet()) {
            String curr_parent = entry.getKey();
            if (second_parent_distances.containsKey(curr_parent)) {
                Integer sum_distance = entry.getValue() + second_parent_distances.get(curr_parent);
                if (sum_distance < best_distance) {
                    best_parent = curr_parent;
                    best_distance = sum_distance;
                }
            }
        }

        if (best_parent != null) {
            return best_parent;
        }
        
        throw Utils.error("There should be a split point between these two commits.");
    }
}
