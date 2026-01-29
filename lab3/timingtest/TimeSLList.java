package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> ops = new AList<>();
        SLList<Integer> subject = new SLList<>();

        for (int i = 1000, j = 0; j < 8; j++, i *= 2) {
            Ns.addLast(i);
            ops.addLast(100000);
        }

        for (int i = 0; i < Ns.size(); i++) {
            int lfs = (i == 0) ? 0 : Ns.get(i - 1);
            int rfs = Ns.get(i);
            for (int j = 0; j < rfs - lfs; j++) {
                subject.addLast(j);
            }
            Stopwatch sw = new Stopwatch();
            for (int j = 0; j < ops.get(i); j++) {
                subject.getLast();
            }
            double timeInSeconds = sw.elapsedTime();
            times.addLast(timeInSeconds);
        }

        printTimingTable(Ns, times, ops);
    }

}
