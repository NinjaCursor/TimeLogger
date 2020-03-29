package TimeSheet.Main;

import java.util.ArrayList;

public class PlayerTimePackage {
    private ArrayList<LogData> logs;
    private SummaryData summaryData;

    public PlayerTimePackage(ArrayList<LogData> logs, SummaryData summaryData) {
        this.logs = logs;
        this.summaryData = summaryData;
    }

    public ArrayList<LogData> getLogs() {
        return logs;
    }

    public SummaryData getSummaryData() {
        return summaryData;
    }
}
