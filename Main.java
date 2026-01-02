import java.time.*;
import java.util.*;
class Task {
    String id, title;
    LocalDate dueDate;
    double estimatedHours;
    int priority;
    double score;
    int remainingMinutes;

    Task(String id, String title, LocalDate dueDate, double hours, int priority) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.estimatedHours = hours;
        this.priority = priority;
    }
}

class StudyWindow {
    LocalDate day;
    LocalTime start, end;

    StudyWindow(LocalDate d, LocalTime s, LocalTime e) {
        day = d; start = s; end = e;
    }
}

class NoStudyEvent {
    LocalDate day;
    LocalTime start, end;

    NoStudyEvent(LocalDate d, LocalTime s, LocalTime e) {
        day = d; start = s; end = e;
    }
}

class FreeSlot {
    LocalDate day;
    LocalTime start, end;
    int duration;

    public String toString() {
        return day + " " + start + "-" + end + " (" + duration + "m)";
    }
}

class Chunk {
    String id, taskId;
    LocalDate dueDate;
    int duration;
    double score;

    public String toString() {
        return taskId + " chunk " + duration + "m (due " + dueDate + ")";
    }
}

class ScheduledBlock {
    String taskId, chunkId;
    LocalDate day;
    LocalTime start, end;

    public String toString() {
        return taskId + " on " + day + " " + start + "-" + end;
    }
}

class SchedulingResult {
    List<ScheduledBlock> scheduledBlocks = new ArrayList<>();
    List<Chunk> unscheduledChunks = new ArrayList<>();
}

class SchedulingConfig {
    int minBlock = 30;
    int maxBlock = 60;
    int horizon = 7;
}
class SchedulerEngine {

    public SchedulingResult schedule(
            List<Task> tasks,
            List<StudyWindow> windows,
            List<NoStudyEvent> noEvents,
            SchedulingConfig config,
            LocalDate today
    ) {
        SchedulingResult result = new SchedulingResult();

        // Convert study windows into free slots
        List<FreeSlot> freeSlots = new ArrayList<>();
        for (StudyWindow w : windows) {
            FreeSlot fs = new FreeSlot();
            fs.day = w.day;
            fs.start = w.start;
            fs.end = w.end;
            fs.duration = (int) Duration.between(w.start, w.end).toMinutes();
            freeSlots.add(fs);
        }

        // Score tasks
        for (Task t : tasks) {
            long days = Math.max(1, Duration.between(today.atStartOfDay(), t.dueDate.atStartOfDay()).toDays());
            double urgency = 1.0 / days;
            double priorityMult = 1 + (t.priority - 3) * 0.25;
            t.score = urgency * priorityMult;
            t.remainingMinutes = (int)(t.estimatedHours * 60);
        }

        // Break into chunks
        List<Chunk> chunks = new ArrayList<>();
        for (Task t : tasks) {
            int rem = t.remainingMinutes;
            while (rem > 0) {
                int size = Math.min(config.maxBlock, rem);
                Chunk c = new Chunk();
                c.id = t.id + "_c" + chunks.size();
                c.taskId = t.id;
                c.dueDate = t.dueDate;
                c.duration = size;
                c.score = t.score;
                chunks.add(c);
                rem -= size;
            }
        }

        // Sort chunks
        chunks.sort((a, b) -> {
            int cmp = a.dueDate.compareTo(b.dueDate);
            if (cmp != 0) return cmp;
            return Double.compare(b.score, a.score);
        });

        // Place chunks
        for (Chunk c : chunks) {
            boolean placed = false;

            for (FreeSlot fs : freeSlots) {
                if (fs.duration >= c.duration && !fs.day.isAfter(c.dueDate)) {
                    ScheduledBlock sb = new ScheduledBlock();
                    sb.taskId = c.taskId;
                    sb.chunkId = c.id;
                    sb.day = fs.day;
                    sb.start = fs.start;
                    sb.end = fs.start.plusMinutes(c.duration);

                    result.scheduledBlocks.add(sb);

                    fs.start = fs.start.plusMinutes(c.duration);
                    fs.duration -= c.duration;

                    placed = true;
                    break;
                }
            }

            if (!placed) result.unscheduledChunks.add(c);
        }

        return result;
    }
}

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
