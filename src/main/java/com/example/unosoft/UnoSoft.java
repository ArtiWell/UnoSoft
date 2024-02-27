package com.example.unosoft;


import java.io.*;
import java.util.*;

public class UnoSoft {

    private static final Map<String, Integer> stringLineNumber = new HashMap<>();
    private static final List<String> intersectingLines = new ArrayList<>();

    public static void main(String[] args) {
        long start = System.nanoTime();

        readFile();

        Set<Integer> numbersOfIntersectingLines = getNumbersOfIntersectingLines();

        for (Map.Entry<String, Integer> entry : stringLineNumber.entrySet()) {
            if (numbersOfIntersectingLines.contains(entry.getValue())) {
                intersectingLines.add(entry.getKey());
            }
        }

        writeFile(getResultGroups());

        long finish = System.nanoTime();
        double time = (finish - start) / 1e9;
        System.out.printf("Time : %.3f sec", time);

    }

    private static void readFile() {
        int lineCount = 1;
        try (BufferedReader br = new BufferedReader(new FileReader("lng.txt"))) {
            String readLine;
            while ((readLine = br.readLine()) != null) {
                stringLineNumber.put(readLine, lineCount++);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Set<Integer> getNumbersOfIntersectingLines() {
        Set<OneSlotAndLine> oneSlotAndLines = new HashSet<>();
        Map<OneSlot, Integer> oneSlotToRepeats = new HashMap<>();

        for (Map.Entry<String, Integer> entry : stringLineNumber.entrySet()) {
            if (!isValidLine(entry.getKey())) {
                continue;
            }

            String[] array = entry.getKey().split(";");
            for (int j = 0; j < array.length; j++) {
                String numberAsString = array[j].substring(1, array[j].length() - 1);
                long number = numberAsString.isEmpty() ? 0 : Long.parseLong(numberAsString);

                OneSlot oneSlot = new OneSlot(number, j);
                OneSlotAndLine oneSlotAndLine = new OneSlotAndLine(oneSlot, entry.getValue());

                oneSlotToRepeats.put(oneSlot, oneSlotToRepeats.getOrDefault(oneSlot, 0) + 1);
                oneSlotAndLines.add(oneSlotAndLine);
            }
        }
        Set<Integer> returnSet = new HashSet<>();
        oneSlotAndLines.forEach(OneSlotAndLine -> {
            OneSlot key = OneSlotAndLine.oneSlot();
            Integer orDefault = oneSlotToRepeats.getOrDefault(key, 0);
            if (orDefault > 1 && OneSlotAndLine.oneSlot().number() != 0) {
                returnSet.add(OneSlotAndLine.line());
            }

        });
        return returnSet;
    }

    private static List<List<String>> getResultGroups() {

        List<List<String>> resultGroups = new ArrayList<>();

        while (intersectingLines.size() != 0) {
            List<String> listElements = new ArrayList<>();
            listElements.add(intersectingLines.get(0));
            for (int j = 1; j < intersectingLines.size(); j++) {
                if (isCoincidedLineAndSet(intersectingLines.get(j), listElements)) {
                    listElements.add(intersectingLines.get(j));
                    intersectingLines.remove(j);
                    j = 0;
                }
            }
            intersectingLines.remove(0);

            resultGroups.add(listElements);
        }

        resultGroups.sort((l1, l2) -> Integer.compare(l2.size(), l1.size()));
        return resultGroups;
    }

    private static void writeFile(List<List<String>> resultGroups) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("answer.txt"))) {
            writer.write("Number of groups with more than one element: " + resultGroups.size() + "\n\n");
            for (int i = 0; i < resultGroups.size(); i++) {
                writer.write("Group " + (i + 1) + "\n");
                List<String> group = resultGroups.get(i);
                for (String line : group) {
                    writer.write(line+"\n");
                }
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValidLine(String str) {
        boolean inside = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '"') {
                inside = !inside;
            } else if (!inside && c != ';' && c != ' ') {
                if (!Character.isDigit(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isCoincidedLineAndSet(String str, List<String> list) {
        for (String string : list) {
            if (isCoincidedLines(str, string)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCoincidedLines(String str1, String str2) {
        String[] array1 = str1.split(";");
        String[] array2 = str2.split(";");

        for (int i = 0; i < array1.length && i < array2.length; i++) {
            if (!array1[i].equals("\"\"") && array1[i].equals(array2[i])) {
                return true;
            }
        }
        return false;
    }

}


