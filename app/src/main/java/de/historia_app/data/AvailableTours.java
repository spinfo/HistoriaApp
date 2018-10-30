package de.historia_app.data;


import android.content.Context;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    public ArrayList<TourRecord> getAllRecords() {
        ArrayList<TourRecord> result = new ArrayList<>();
        for (Collection<TourRecord> records : recordsByAreaId.values()) {
            result.addAll(records);
        }
        return result;
    }

    public List<TourRecord> getRecordsIn(long areaId) {
        final ArrayList<TourRecord> records = recordsByAreaId.get(areaId);
        return records == null ? Collections.<TourRecord>emptyList() : records;
    }

    public List<AreaDownloadStatus> getAreaDownloadStatus(Context context) {
        List<AreaDownloadStatus> result = new ArrayList<>(areaNamesById.size());
        for (long areaId : getAreaIds()) {
            result.add(buildAreaDownloadStatus(context, areaId));
        }
        sortAreaDownloadStatus(result);
        return result;
    }

    private void sortAreaDownloadStatus(List<AreaDownloadStatus> list) {
        Collections.sort(list, new Comparator<AreaDownloadStatus>() {
            final Collator collator = Collator.getInstance(Locale.getDefault());
            @Override
            public int compare(AreaDownloadStatus o1, AreaDownloadStatus o2) {
                String s1 = o1.getName() == null ? "" : o1.getName();
                String s2 = o2.getName() == null ? "" : o2.getName();
                return collator.compare(s1, s2);
            }
        });
    }

    private String getName(long areaId) {
        String result = areaNamesById.get(areaId);
        return result == null ? "" : result;
    }

    private AreaDownloadStatus buildAreaDownloadStatus(Context context, long areaId) {
        AreaDownloadStatus result = new AreaDownloadStatus(areaId);

        Set<Long> installedIds = (new DataFacade(context)).getTourIdsInArea(areaId);
        List<TourRecord> records = getRecordsIn(areaId);

        int tours = 0;
        int size = 0;
        for (TourRecord record : records) {
            if (installedIds.contains(record.getTourId())) {
                tours += 1;
                size += record.getDownloadSize();
            }
        }
        result.setDownloadedToursAmount(tours);
        result.setDownloadedToursSize(size);
        result.setName(getName(areaId));
        result.setAvailableToursAmount(availableToursAmount(areaId));

        return result;
    }

    public int availableToursAmount(long areaId) {
        return getRecordsIn(areaId).size();
    }
}
