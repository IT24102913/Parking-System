package org.PG196VehicleParking;

import java.util.List;

public class QuickSort {

    public static void sort(List<ParkingSlot> list) {
        quicksort(list, 0, list.size() - 1);
    }

    private static void quicksort(List<ParkingSlot> list, int low, int high) {
        if (low < high) {
            int pi = partition(list, low, high);
            quicksort(list, low, pi - 1);
            quicksort(list, pi + 1, high);
        }
    }

    private static int partition(List<ParkingSlot> list, int low, int high) {
        ParkingSlot pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (list.get(j).isOccupied() && !pivot.isOccupied()) {
                i++;
                swap(list, i, j);
            } else if (list.get(j).isOccupied() == pivot.isOccupied()) {
                if (list.get(j).getSlotId() < pivot.getSlotId()) {
                    i++;
                    swap(list, i, j);
                }
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static void swap(List<ParkingSlot> list, int i, int j) {
        ParkingSlot temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
