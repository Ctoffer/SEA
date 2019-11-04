package de.ctoffer.meta;

import org.apache.poi.ss.usermodel.Row;

public class Student {
    public final int muesliId;
    public final int moodleId;
    public final int groupId;
    public final String name;
    public final String mail;

    public Student(final int muesliId, final int moodleId, final int groupId, final String name, final String mail) {
        this.muesliId = muesliId;
        this.moodleId = moodleId;
        this.groupId = groupId;
        this.name = name;
        this.mail = mail;
    }

    public String getFirstName() {
        return name.split(" ")[0];
    }

    public String getName() {
        return name;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getMoodleId() {
        return moodleId;
    }

    public int getMuesliId() {
        return muesliId;
    }

    public String getMail() {
        return mail;
    }

    @Override
    public String toString() {
        return String.format("'%s'#(%s, %s, %s)", name, muesliId, moodleId, groupId);
    }

    public static Student fromRow(Row row) {
        int muesliId = getIntFromCell(row, 0);
        int moodleId =  getIntFromCell(row, 1);
        int groupId =  getIntFromCell(row, 2);
        String name = row.getCell(3).getStringCellValue();
        String mail = row.getCell(4).getStringCellValue();

        return new Student(muesliId, moodleId, groupId, name, mail);
    }

    private static int getIntFromCell(Row row, int index) {
        return (int) row.getCell(index).getNumericCellValue();
    }
}
