package ru.hh.school.homework.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.school.homework.entity.PopularWords;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toList;
import static ru.hh.school.homework.logic.SimpleWordCounter.naiveCount;

public class ScanDirectory {
    private static Path dirPath;
    private static Logger LOGGER = LoggerFactory.getLogger(ScanDirectory.class);

    public ScanDirectory(Path path) {
        dirPath = path;
    }

    //метод получает все директории с учетом вложенных
    public List<Path> getDirectories() throws IOException {
        return Files.walk(dirPath)
                .filter(path -> Files.isDirectory(path))
                .collect(toList());
    }

    //метод возвращает объект PopularWords для каждой папки (топ 10 слов)
    public PopularWords getPopularWords(Path dirPath) {
        List<String> words = getListPopularWords(dirPath);
        return new PopularWords(dirPath, words);
    }

    //метод возвращает список популярных слов для переданной директории
    private List<String> getListPopularWords(Path dirPath) {
        Map<String, Long> counts = getWordCounts(dirPath);
        return counts.entrySet().stream()
                .sorted(comparingByValue(reverseOrder()))
                .limit(10)
                .map(Entry::getKey)
                .collect(toList());
    }

    //метод возвращает словарь популярных слов для папки
    private Map<String, Long> getWordCounts(Path dirPath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.java")) {
            return makeWordCounts(stream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return Collections.EMPTY_MAP;
        }
    }
    //счет популярных слов и создание словаря
    private Map<String, Long> makeWordCounts(DirectoryStream<Path> stream) {
        Map<String, Long> wordCounts = new HashMap<>();
        for (Path pathFile : stream) {
            for (Entry<String, Long> entry : naiveCount(pathFile).entrySet()) {
                wordCounts.merge(entry.getKey(), entry.getValue(), Long::sum);
            }
        }
        return wordCounts;
    }
}