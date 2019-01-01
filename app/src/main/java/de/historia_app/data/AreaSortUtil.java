package de.historia_app.data;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AreaSortUtil {

    public interface ObjectWithName {
        String getName();
    }

    private static final Collator collator = Collator.getInstance(Locale.getDefault());

    public static void sortAreasByName(List<? extends ObjectWithName> list) {
        Collections.sort(list, new Comparator<ObjectWithName>() {
            @Override
            public int compare(ObjectWithName o1, ObjectWithName o2) {
                String s1 = o1.getName() == null ? "" : o1.getName();
                String s2 = o2.getName() == null ? "" : o2.getName();
                return collator.compare(s1, s2);
            }
        });
        prependDownloadStatusWithName(list, "Anleitung");
    }

    private static <T extends  ObjectWithName> void prependDownloadStatusWithName(List<T> list, String name) {
        T objectWithName = null;
        for (int i = 0; i < list.size(); i++) {
            if (name.equals(list.get(i).getName())) {
                objectWithName = list.remove(i);
                break;
            }
        }
        if (objectWithName != null) {
            list.add(0, objectWithName);
        }
    }

}
