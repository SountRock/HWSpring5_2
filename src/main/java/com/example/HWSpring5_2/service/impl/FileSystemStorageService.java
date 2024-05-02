package com.example.HWSpring5_2.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import com.example.HWSpring5_2.configuration.StorageProperties;
import com.example.HWSpring5_2.exceptions.StorageException;
import com.example.HWSpring5_2.exceptions.StorageFileNotFoundException;
import com.example.HWSpring5_2.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    /**
     * Получаем из StorageProperties рабочую директорию и заносим ее в rootLocation
     * @param properties
     */
    @Autowired
    public FileSystemStorageService(StorageProperties properties) {

        //Если директории нет
        if(properties.getLocation().trim().length() == 0){
            throw new StorageException("File upload location can not be Empty.");
        }

        //Если есть заносим в rootLocation
        this.rootLocation = Paths.get(properties.getLocation());
    }

    /**
     * Загрузить файл (MultipartFile - представление загруженного файла)
     * @param file
     */
    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                //Если файл пуст
                throw new StorageException("Failed to store empty file.");
            }

            //Создаем обсолютную директорию для нового файла
            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();

            //Если родительский каталог destinationFile совпадает с полной дректорией файла, что не должно быть
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }

            //Если все в прорядке копируем файлы в новую директорию
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    /**
     * Выгрузить все файлы
     * @return
     */
    @Override
    public Stream<Path> loadAll() {
        try {
            //"лениво" собирает файлы
            return Files.walk(this.rootLocation, 1)
                    //собирать файлы только находящиеся в rootLocation
                    .filter(path -> !path.equals(this.rootLocation))
                    //this.rootLocation::relativize - перемещение файлов в эту переменную?
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        //Принимает путь к файлу и выбадет абсолютный путь
        return rootLocation.resolve(filename);
    }

    /**
     * Выгрузить файлы как ресурс
     * @return
     */
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            //Получить ресурс через url
            Resource resource = new UrlResource(file.toUri());
            //Если ресурс существует или его возможно прочитать
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    /**
     * Рекурсивное удаление всех файлов
     */
    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    /**
     * Иничциализация (создает рабочую директорию в реале)
     */
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
