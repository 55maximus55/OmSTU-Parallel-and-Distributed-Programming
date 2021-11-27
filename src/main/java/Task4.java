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

/* Обработка файлов исходного текста должна выполняться в отдельных потоках. Результат должен
собираться в единый индекс (использовать Map). Доступ к единому индексу должен быть защищен
объектом синхронизации. Основной поток должен ожидать завершения всех потоков и только потом
печатать в консоли единый индекс. Можно использовать ключевое слово synchronized, лучше
использовать ReenterantLock */

public class Task4 {
    public static void main(String[] args) throws IOException {
        Map<String, ArrayList<String>> entities = new HashMap<>();
        Pattern pattern = Pattern.compile("(class|interface) +([A-Za-z]\\w*) *(<\\w+>)? *" +
                "(extends +([A-Za-z]\\w*))? *(implements +([A-Za-z]\\w*))?", Pattern.MULTILINE);
        ArrayList<String> data = ReadSourceFiles("");
        CountDownLatch cdl = new CountDownLatch(data.size());
        data.forEach(file -> new Thread(() -> {
            try {
                Matcher matcher = pattern.matcher(file);
                while (matcher.find()) {
                    var name = matcher.group(2);
                    var parentClass = matcher.group(5);
                    var parentInterfaces = matcher.group(7);
                    synchronized (entities) {
                        entities.put(name, entities.getOrDefault(name, new ArrayList<>()));
                        addChildrenToParent(entities, name, parentClass);
                        addChildrenToParent(entities, name, parentInterfaces);
                    }
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

    public static void addChildrenToParent(Map<String, ArrayList<String>> entities, String entity, String parent) {
        if (parent != null) {
            entities.putIfAbsent(parent, new ArrayList<>());
            entities.get(parent).add(entity);
        }
    }

}