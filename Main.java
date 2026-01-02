import java.time.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        // --- 1. Create tasks ---
        Task t1 = new Task("math_hw", "Math Homework", LocalDate.now().plusDays(2), 3, 3);
        Task t2 = new Task("history_read", "History Reading", LocalDate.now().plusDays(4), 2, 2);

        List<Task> tasks = Arrays.asList(t1, t2);

        // --- 2. Create study windows ---
        StudyWindow w1 = new StudyWindow(LocalDate.now(), LocalTime.of(17, 0), LocalTime.of(20, 0));
        StudyWindow w2 = new StudyWindow(LocalDate.now().plusDays(1), LocalTime.of(18, 0), LocalTime.of(21, 0));

        List<StudyWindow> windows = Arrays.asList(w1, w2);

        // --- 3. No no-study events for now ---
        List<NoStudyEvent> noEvents = new ArrayList<>();

        // --- 4. Config ---
        SchedulingConfig config = new SchedulingConfig();

        // --- 5. Run scheduler ---
        SchedulerEngine engine = new SchedulerEngine();
        SchedulingResult result = engine.schedule(tasks, windows, noEvents, config, LocalDate.now());

        // --- 6. Print results ---
        System.out.println("Scheduled Blocks:");
        for (ScheduledBlock b : result.scheduledBlocks) {
            System.out.println(b);
        }

        System.out.println("\nUnscheduled Chunks:");
        for (Chunk c : result.unscheduledChunks) {
            System.out.println(c);
        }
    }
}
