package Task_3;

public class Journal {
    private final int[][][] data;

    public Journal(int[] studentsPerGroup, int weeksCount){
        int groupsCount = studentsPerGroup.length;
        data = new int[groupsCount][][];
        for(int i = 0; i < groupsCount; i++){
            data[i] = new int[studentsPerGroup[i]][weeksCount];
        }
    }

    public int getGroupsCount() { return data.length; }
    public int getStudentsCount(int groupNumber) { return data[groupNumber].length; }
    public int getWeeksCount() { return data[0][0].length; }

    public synchronized void assignGrade(int group, int student,
                                         int date, int grade){
        data[group][student][date] = grade;
    }

    public synchronized void print(){
        System.out.println("============= GRADES ==============");
        System.out.println("I |\tGroup |\tStudent | \tWeek");
        System.out.print("\t\t\t\t\t\t");
        for (int i = 0; i < data[0][0].length; i++){
            System.out.print(i + "\t");
        }
        System.out.print("\n");
        System.out.println("-----------------------------------");
        int index = 1;
        for (int group = 0; group < data.length; group++){
            for (int student = 0; student < data[group].length; student++){
                System.out.print(index + "\t" + group + "\t|\t\t" + student + "\t|\t");
                for (int date = 0; date < data[group][student].length; date++){
                    System.out.print(data[group][student][date] + "\t");
                }
                System.out.println();
                index++;
            }
        }
        System.out.println("-----------------------------------");
    }
}
