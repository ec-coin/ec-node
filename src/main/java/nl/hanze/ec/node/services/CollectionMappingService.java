package nl.hanze.ec.node.services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CollectionMappingService {
    public synchronized static List<String> mapToStringList(List<Object> objects) {
        return objects.stream()
                .map(string -> Objects.toString(string, null))
                .collect(Collectors.toList());
    }
}
