package io.codelabs.digitutor.core.util;

/**
 * Contains all constant values required for database references and more
 */
public final class Constants {
    // User database references
    public static final String TUTORS = "tutors";
    public static final String PARENTS = "parents";
    public static final String WARDS = PARENTS + "/%s/wards";   // String.format(WARDS,parent.key);

    // Misc database references
    public static final String SUBJECTS = "subjects";
    public static final String COMPLAINTS = "complaints";
    public static final String TIMETABLES = WARDS + "/%s/timetables"; // String.format(WARDS,parent.key, ward.key);
    public static final String CLIENTS = TUTORS + "/%s/clients"; // String.format(CLIENTS,tutor.key);
    public static final String REPORTS = "reports";
    public static final String REQUESTS = "requests";
    public static final String ASSIGNMENTS = TUTORS + "/%s/assignments";    // String.format(ASSIGNMENTS,tutor.key);
    public static final String STUDENTS = TUTORS + "/%s/students";    // String.format(STUDENTS,tutor.key);
    public static final String TUTOR_SUBJECTS = TUTORS + "/%s/" + SUBJECTS;    // String.format(ASSIGNMENTS,tutor.key);
    public static final String SCHEDULES = "schedules";
    public static final String FEEDBACK = "feedback";

    // Shared Preferences
    public static final String SHARED_PREFS = "TUTOR_SHARED_PREFS";
    public static final String USER_KEY = "USER_KEY";
    public static final String USER_TYPE = "USER_TYPE";

    // Others
    public static final String DEFAULT_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/house-teaching.appspot.com/o/default-avatar.png?alt=media&token=b1ab3898-f830-42b6-aa08-18c70627bb87";
}
