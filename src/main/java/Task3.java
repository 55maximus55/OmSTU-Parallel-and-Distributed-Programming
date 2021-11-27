import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/* Усовершенствовать программу из задания 2, чтобы для обработки каждого файла исходного текста
создавался отдельный поток (Thread). Взаимодействия потоков не требуется! Для ожидания
завершения потоков можно использовать метод join(), желательно CountDownLatch.
Работоспособность программы должна быть продемонстрирована на большом проекте с GitHub,
например, Spring Framework. */

public class Task3 {
    public static void main(String[] args) throws IOException {
        Map<String, ArrayList<String>> entities = new HashMap<>();
        Pattern pattern = Pattern.compile("(class|interface) +([A-Za-z]\\w*) *(<\\w+>)? *" +
                "(extends +([A-Za-z]\\w*))? *(implements +([A-Za-z]\\w*))?", Pattern.MULTILINE);
        ArrayList<String> data = ReadSourceFiles("");
        Parent action = (entity, parent) -> {
            if (parent != null) {
                entities.put(parent, entities.getOrDefault(parent, new ArrayList<>()));
                var children = entities.getOrDefault(parent, new ArrayList<>());
                children.add(entity);
            }
        };
        CountDownLatch cdl = new CountDownLatch(data.size());
        data.forEach(file -> new Thread(() -> {
            try {
                Matcher matcher = pattern.matcher(file);
                while (matcher.find()) {
                    var name = matcher.group(2);
                    var parentClass = matcher.group(5);
                    var parentInterface = matcher.group(7);
                    entities.put(name, entities.getOrDefault(name, new ArrayList<>()));
                    action.addChildren(name, parentClass);
                    action.addChildren(name, parentInterface);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            cdl.countDown();
        }).start());
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(entities);
    }

    public static ArrayList<String> ReadSourceFiles(String path) throws IOException {
        ArrayList<String> data = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(f -> f.endsWith(".java"))
                    .forEach(f -> {
                        StringBuilder fileData = new StringBuilder();
                        try (Scanner in = new Scanner(new File(f))) {
                            while (in.hasNextLine()) {
                                fileData.append(in.nextLine()).append(" ");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        data.add(fileData.toString());
                    });
        }
        return data;
    }

    interface Parent {
        void addChildren(String entity, String parent);
    }
}