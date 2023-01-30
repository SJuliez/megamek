package megamek.common;

public enum ERAS {
    AOW("Age of War", 0),
    SL("Star League", 2571),
    ESW("Early Succession Wars", 2781),
    LSW("Late Succession Wars", 2901),
    CLI("Clan Invasion", 3050),
    CW("Civil War", 3062),
    JHD("Jihad", 3068),
    EREP("Early Republic", 3081),
    LREP("Late Republic", 3101),
    DARK("Dark Age", 3131),
    ILC("Star League", 3151);

    private final String name;
    private final int startYear;

    ERAS(String name, int begin) {
        this.name = name;
        this.startYear = begin;
    }

    public String getName() {
        return name;
    }

    public int getStartYear() {
        return startYear;
    }

    public static ERAS getEra(int year) {
        if (year <= 2570) {
            return AOW;
        } else if (year <= 2780) {
            return SL;
        } else if (year <= 2900) {
            return ESW;
        } else if (year <= 3049) {
            return LSW;
        } else if (year <= 3061) {
            return CLI;
        } else if (year <= 3067) {
            return CW;
        } else if (year <= 3080) {
            return JHD;
        } else if (year <= 3100) {
            return EREP;
        } else if (year <= 3130) {
            return LREP;
        } else if (year <= 3150) {
            return DARK;
        } else {
            return ILC;
        }
    }
}
