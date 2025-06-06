package Task_3;

import java.util.Random;

public class Teacher implements Runnable {
    private final Journal journal;
    private static final Random random = new Random();

    public Teacher(Journal journal){
        this.journal = journal;
    }

    public void run() {
        int groupsCount = journal.getGroupsCount();
        int weeksCount = journal.getWeeksCount();
        int randomWeek = random.nextInt(weeksCount);

        for (int group = 0; group < groupsCount; group++){
            for (int student = 0;
                 student < journal.getStudentsCount(group);
                 student++)
            {
                int MAX_GRADE = 100;
                int MIN_GRADE = 60;
                int grade = random.nextInt(MIN_GRADE,MAX_GRADE + 1);
                journal.assignGrade(group, student, randomWeek, grade);
            }
        }
    }
}
