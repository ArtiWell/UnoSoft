package com.example.unosoft;


import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class UnoSoft {
    static List<Line> lines = new ArrayList<>(1000000);

    static Map<Float, Map<Integer, List<Line>>> valueToRowToLines = new HashMap<>(1000000);
    static Map<Integer, ResultGroup> result = new HashMap<>(1000000);

    public static void main(String[] args) throws InterruptedException {
        var start = System.currentTimeMillis();
        readFile();
        makeRows();
        List<ResultGroup> answer = result.values().stream()
                .filter(e -> e.lines.size() != 1)
                .sorted(Comparator.comparingInt(e -> -e.lines.size()))
                .collect(Collectors.toList());

        writeFile(answer);

        var end = System.currentTimeMillis();
        System.out.println(answer.size());
        System.out.println((end - start));
    }


    private static void makeRows() {
        lines.forEach(line -> {
                    int currentLineGroup = line.currentGroup;
                    for (int row = 0; row < line.content.length; row++) {
                        float value = line.content[row];
                        if (value == 0) continue;

                        Map<Integer, List<Line>> rowToLines;

                        if (valueToRowToLines.containsKey(value)) { //если такое число есть то

                            rowToLines = valueToRowToLines.get(value);
                            if (rowToLines.containsKey(row)) { // если такая колонка есть то

                                List<Line> groupedLines = rowToLines.get(row);
                                int groupOfGroupedLines = groupedLines.get(0).currentGroup;

                                if (groupOfGroupedLines == currentLineGroup) {
                                    continue;
                                }

                                ResultGroup oldGroup = result.remove(currentLineGroup);// возможно линия была раньше добавлена в группу
                                if (oldGroup == null) {
                                    result.get(groupOfGroupedLines).add(line);
                                } else {
                                    result.get(groupOfGroupedLines).addAll(oldGroup);
                                }
                                groupedLines.add(line);

                            } else {// если такой  колонки нет то

                                rowToLines.computeIfAbsent(row, k -> new ArrayList<>())
                                        .add(line);

                                result.computeIfAbsent(currentLineGroup, k -> new ResultGroup(line.currentGroup))
                                        .add(line); // может добавить саму себя, если ранее была уже добавлена
                            }


                        } else { // если такого числа еще нет
                            valueToRowToLines.computeIfAbsent(value, k -> new HashMap<>())
                                    .computeIfAbsent(row, k -> new ArrayList<>())
                                    .add(line);

                            result.computeIfAbsent(currentLineGroup, k -> new ResultGroup(line.currentGroup))
                                    .add(line);
                        }

                        currentLineGroup = line.currentGroup;
                    }
                }
        );
    }

    private static void readFile() {
        try (BufferedReader br = new BufferedReader(new FileReader("lng-big.csv"))) {
            String rawLine;
            while ((rawLine = br.readLine()) != null) {
                String[] line = rawLine.split(";");
                float[] split = new float[line.length];
                try {
                    for (int i = 0; i < split.length; i++) {
                        split[i] = convertString(line[i]);
                    }
                    lines.add(new Line(split, rawLine.hashCode()));
                } catch (Exception i) {
                    i.printStackTrace();
                }
            }
        } catch (Exception i) {
            i.printStackTrace();
        }
    }

    private static void writeFile(List<ResultGroup> resultGroups) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("answer.txt"))) {
            writer.write("Number of groups with more than one element: " + resultGroups.size() + "\n\n");
            for (int i = 0; i < resultGroups.size(); i++) {
                writer.write("Group " + (i + 1) + "\n");
                ResultGroup group = resultGroups.get(i);
                for (Line line : group.lines) {
                    writer.write(Arrays.toString(line.content) + "\n");
                }
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Float convertString(String value) {
        return value.isBlank() ? 0 : Float.parseFloat(value.substring(1, value.length() - 1));
    }

}

class ResultGroup implements Comparable<ResultGroup> {

    final int group;
    Set<Line> lines = new HashSet<>(1);

    ResultGroup(int group) {
        this.group = group;
    }


    void addAll(ResultGroup resultGroup) {
        if (group == resultGroup.group) return;  //чтобы не удалить самого себя
        resultGroup.lines.forEach(this::add);
        resultGroup.lines.clear();
    }


    void add(Line line) {
        line.moveTo(group);
        lines.add(line);
    }

    @Override
    public int compareTo(ResultGroup o) {
        return Integer.compare(this.lines.size(), o.lines.size());
    }
}


class Line {
    static int lastGroup = 1;
    final float[] content;
    private final int hashCode;
    int currentGroup = lastGroup++;

    public Line(float[] split, int hashCode) {
        content = split;
        this.hashCode = hashCode;
    }

    void moveTo(int group) {
        currentGroup = group;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Line)) return false;
        Line line = (Line) o;
        return hashCode == line.hashCode;
    }
}


