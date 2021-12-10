import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/* В отличии от программы из задания 4 количество потоков должно быть ограниченным. Следует
использовать один из вариантов Java Executor Service. Формирование единого индекса должно
осуществляться с использованием Future. Все объекты Future необходимо собрать в список и только
когда последнее “задание” на обработку будет выдано начинать считывать результаты (метод get()). */

public class Task5 {
    public static void main(String[] args) throws IOException {
        Map<String, ArrayList<String>> entities = new HashMap<>();
        Pattern pattern = Pattern.compile("(class|interface) +([A-Za-z]\\w*) *(<\\w+>)? *" +
                "(extends +([A-Za-z]\\w*))? *(implements +([A-Za-z]\\w*))?", Pattern.MULTILINE);
        ArrayList<String> data = readSourceFiles("");
        CountDownLatch cdl = new CountDownLatch(data.size());
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        ArrayList<Callable<List<Entity>>> executorTasks = new ArrayList<>();
        data.forEach(file -> {
            executorTasks.add(() -> {
                List<Entity> entityList = new ArrayList<>();
                try {
                    Matcher matcher = pattern.matcher(file);
                    while (matcher.find()) {
                        Entity entity = new Entity();
                        entity.name = matcher.group(2);
                        entity.parentClass = matcher.group(5);
                        entity.interfaces = matcher.group(7);
                        entityList.add(entity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cdl.countDown();
                return entityList;
            });
        });
        List<Future<List<Entity>>> futures = null;
        try {
            futures = executorService.invokeAll(executorTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();


        if (futures != null) {
            futures.forEach(future -> {
                try {
                    List<Entity> result = future.get();
                    for (Entity entity : result) {
                        entities.put(entity.name, entities.getOrDefault(entity.name, new ArrayList<>()));
                        addChildrenToParent(entities, entity.name, entity.parentClass);
                        addChildrenToParent(entities, entity.name, entity.interfaces);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println(entities);

    }

    public static ArrayList<String> readSourceFiles(String path) throws IOException {
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

    static class Entity {
        public String name = null;
        public String parentClass = null;
        public String interfaces = null;
    }
}