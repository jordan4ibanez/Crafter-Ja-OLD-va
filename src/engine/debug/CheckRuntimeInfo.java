package engine.debug;

import engine.graphics.Mesh;

import static engine.Time.getDelta;
import static engine.gui.TextHandling.createTextWithShadow;

public class CheckRuntimeInfo {
    private static final Mesh[] runtimeInfoText = new Mesh[6];
    private static long heapSize = 0;
    private static long heapMaxSize = 0;
    private static long heapFreeSize = 0;
    private static long availableProcessors = 0;
    private static int currentAmountOfThreads = 0;
    private static int cpuHealth = 100;

    public static Mesh[] getRuntimeInfoText(){
        return runtimeInfoText;
    }


    private static float timer = 0f;

    public static void doRuntimeInfoUpdate(){
        timer += getDelta();

        if (timer >= 0.5){
            timer = 0f;
            updateRuntimeInfoText();
            currentAmountOfThreads = 0;
        }

        //do THREAD and HEALTH calculation in real time

        if (Thread.activeCount() > currentAmountOfThreads) {
            // Get number of threads currently being utilized
            currentAmountOfThreads = Thread.activeCount();
            // Show how much effort the CPU is exerting (Higher is better/Less cpu resources being used)
            cpuHealth = Math.round((((float)availableProcessors-((float)currentAmountOfThreads-1f)) / (float)availableProcessors) * 100.f);
        }
    }

    public static void updateRuntimeInfoText(){
        //update the info
        getHeapInfo();

        //update the info text
        for (int i = 0; i < 6; i++) {
            if (runtimeInfoText[i] != null) {
                runtimeInfoText[i].cleanUp(false);
            }
        }
        runtimeInfoText[0] = createTextWithShadow("HEAP SIZE: " + formatSize(heapSize), 1,1,1);
        runtimeInfoText[1] = createTextWithShadow("HEAP MAX: "  + formatSize(heapMaxSize), 1,1,1);
        runtimeInfoText[2] = createTextWithShadow("HEAP FREE: " + formatSize(heapFreeSize), 1,1,1);
        runtimeInfoText[3] = createTextWithShadow("# CPU UNITS: " + availableProcessors, 1,1,1);
        runtimeInfoText[4] = createTextWithShadow("# OF THREADS: " + currentAmountOfThreads, 1,1,1);
        if (cpuHealth < 30){
            runtimeInfoText[5] = createTextWithShadow("CPU HEALTH: " + cpuHealth + "% (WARNING! CPU OVERLOAD!)", 1, 1, 1);
        } else {
            runtimeInfoText[5] = createTextWithShadow("CPU HEALTH: " + cpuHealth + "%", 1, 1, 1);
        }
    }

    //this is from:
    //https://stackoverflow.com/questions/2015463/how-to-view-the-current-heap-size-that-an-application-is-using
    public static void getHeapInfo() {
        heapSize = Runtime.getRuntime().totalMemory();
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        heapMaxSize = Runtime.getRuntime().maxMemory();
        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        heapFreeSize = Runtime.getRuntime().freeMemory();

        // Get number of cpu threads available
        availableProcessors = Runtime.getRuntime().availableProcessors();
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
