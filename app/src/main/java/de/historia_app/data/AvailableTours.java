package de.historia_app.data;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AvailableTours {

    private Map<Long, ArrayList<TourRecord>> recordsByAreaId;

    private Map<Long, String> areaNamesById;

    public AvailableTours() {
        this.recordsByAreaId = new HashMap<>();
        this.areaNamesById = new HashMap<>();
    }

    public void addRecord(TourRecord record) {
        ArrayList<TourRecord> records = recordsByAreaId.get(record.getAreaId());
        if(records == null) {
            records = new ArrayList<>();
        }
        records.add(record);
        recordsByAreaId.put(record.getAreaId(), records);
        areaNamesById.put(record.getAreaId(), record.getAreaName());
    }

    public Collection<String> getAreaNames() {
        return areaNamesById.values();
    }

    public Set<Long> getAreaIds() {
        return areaNamesById.keySet();
    }

    public ArrayList<TourRecord> getRecordsForArea(long areaId) {
        return recordsByAreaId.get(areaId);
    }

    // TODO: Temporary workaround, remove after reworking tour records list view
    public ArrayList<TourRecord> getAllRecords() {
        ArrayList<TourRecord> result = new ArrayList<>();
        for (Collection<TourRecord> records : recordsByAreaId.values()) {
            result.addAll(records);
        }
        return result;
    }


}
