package engine.debug;

import engine.graph.Mesh;

import static engine.Time.getDelta;
import static engine.gui.TextHandling.createTextWithShadow;

public class CheckHeapSize {
    private static final Mesh[] heapInfoText = new Mesh[3];
    private static long heapSize = 0;
    private static long heapMaxSize = 0;
    private static long heapFreeSize = 0;

    public static Mesh[] getHeapInfoText(){
        return heapInfoText;
    }


    private static float timer = 0f;

    public static void doHeapInfoUpdate(){
        timer += getDelta();

        if (timer >= 0.25){
            timer = 0f;
            updateHeapInfoText();
        }
    }

    public static void updateHeapInfoText(){
        //update the info
        getHeapInfo();

        //update the info text
        for (int i = 0; i < 3; i++) {
            if (heapInfoText[i] != null) {
                heapInfoText[i].cleanUp(false);
            }
        }
        heapInfoText[0] = createTextWithShadow("HEAP SIZE: " + formatSize(heapSize), 1,1,1);
        heapInfoText[1] = createTextWithShadow("HEAP MAX: "  + formatSize(heapMaxSize), 1,1,1);
        heapInfoText[2] = createTextWithShadow("HEAP FREE: " + formatSize(heapFreeSize), 1,1,1);
    }

    //this is from:
    //https://stackoverflow.com/questions/2015463/how-to-view-the-current-heap-size-that-an-application-is-using
    public static void getHeapInfo() {
        heapSize = Runtime.getRuntime().totalMemory();
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        heapMaxSize = Runtime.getRuntime().maxMemory();
        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        heapFreeSize = Runtime.getRuntime().freeMemory();
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
