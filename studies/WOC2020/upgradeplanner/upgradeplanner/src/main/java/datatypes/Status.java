package datatypes;

import org.jose4j.json.internal.json_simple.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class Status {
    private static Status status;
    private List<String> update = new ArrayList<>();

    public static Status getInstance() {
        if (status == null) {
            status = new Status();
        }
        return status;
    }

    public void addStatus(String text) {
        update.add(text);
    }

    public List<String> getStatus() {
        return update;
    }
}
