package gitlet;

import java.io.File;
import java.util.*;
class StuUtils {
    static String findCommit(String text, Context context) {
        String result = context.commitTree.matchCommit(text);
        if (result.equals("")) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        return result;
    }

    static List<String> scanUntrackedFiles(Context context) {
        List<String> files = new ArrayList<>();
        Map<String, String> deletionStage = context.stage.getRemovals();
        for (Map.Entry<String, String> entry : deletionStage.entrySet()) {
            String filename = entry.getKey();
            File file = Utils.join(context.cwd, filename);
            if (file.exists()) {
                files.add(filename);
            }
        }
        
        List<String> dirFiles = Utils.plainFilenamesIn(context.cwd);
        for (String filename : dirFiles) {
            boolean tracked = context.headCommit()
                    .getTracked().containsKey(filename);
            boolean staged = context.stage.isStagedAddition(filename);
            if (!tracked && !staged) {
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
            Integer parentDistance = distances.get(id) + 1;
            Commit currCommit = context.commitTree.getCommit(id);
            String firstParent = currCommit.getParentID();
            String secondParent = currCommit.getSecondParentID();

            if (!firstParent.isEmpty() && !distances.containsKey(firstParent)) {
                queue.add(firstParent);
                distances.put(firstParent, parentDistance);
            }
            if (!secondParent.isEmpty() && !distances.containsKey(secondParent)) {
                queue.add(secondParent);
                distances.put(secondParent, parentDistance);
            }
        }

        return distances;
    }


    static String findSplitPoint(String firstCommit, String secondCommit, Context context) {
        HashMap<String, Integer> firstParentDistances = findParentsDistance(firstCommit, context);
        HashMap<String, Integer> secondParentDistances = findParentsDistance(secondCommit, context);

        String bestParent = null;
        Integer bestDistance = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : firstParentDistances.entrySet()) {
            String currParent = entry.getKey();
            if (secondParentDistances.containsKey(currParent)) {
                Integer sumDistance = entry.getValue() + secondParentDistances.get(currParent);
                if (sumDistance < bestDistance) {
                    bestParent = currParent;
                    bestDistance = sumDistance;
                }
            }
        }

        if (bestParent != null) {
            return bestParent;
        }
        
        throw Utils.error("There should be a split point between these two commits.");
    }

    static void transfer(Context src, Context dest, String start) {
        Queue<String> queue = new LinkedList<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            String curr = queue.remove();
            if (curr.isEmpty() || dest.commitTree.hasCommit(curr)) {
                continue;
            }

            Commit currCommit = src.commitTree.getCommit(curr);
            dest.commitTree.addCommit(curr, currCommit);

            Map<String, String> tracked = currCommit.getTracked();
            for (Map.Entry<String, String> entry : tracked.entrySet()) {
                String hash = entry.getValue();
                if (dest.blobStore.contains(hash)) {
                    continue;
                }
                dest.blobStore.store(hash, src.blobStore.load(hash));
            }

            if (!currCommit.getParentID().isEmpty()) {
                queue.add(currCommit.getParentID());
            }
            if (currCommit.isMergeCommit()) {
                queue.add(currCommit.getSecondParentID());
            }
        }
    }
}
