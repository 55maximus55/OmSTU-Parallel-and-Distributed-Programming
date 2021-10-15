import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/* В произвольном текстовом документе посчитать и напечатать в консоли сколько раз встречается
каждое  слово.  Текст  можно  сформировать  генератором  lorem  ipsum.  Необходимо  использовать
регулярные  выражения  (regexp),  должны  корректно  обрабатываться  любые  знаки  препинания  в
любом количестве. */

public class Task1 {

    public static void main(String[] args) {
        File file = new File("text.txt");
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Scanner in = new Scanner(file);
            while (in.hasNextLine()) {
                stringBuilder.append(in.nextLine()).append("\n");
            }
            String text = stringBuilder.toString();

            Pattern pattern = Pattern.compile("[\\W]+", Pattern.CASE_INSENSITIVE);
            String[] words = pattern.split(text);
            HashMap<String, Integer> dict = new HashMap<>();
            Stream<String> stream = Arrays.stream(words);
            stream.forEach(x -> dict.put(x, dict.getOrDefault(x, 0) + 1));
            dict.forEach((x, y) -> System.out.println("" + x + " " + y));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
