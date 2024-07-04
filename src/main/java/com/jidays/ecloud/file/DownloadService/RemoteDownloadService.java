package com.jidays.ecloud.file.DownloadService;

import com.jidays.ecloud.DTO.DownloadTaskDTO;
import com.jidays.ecloud.Entity.DownloadTask;
import com.jidays.ecloud.Entity.EFile;
import com.jidays.ecloud.file.FileService.FileSystemService;
import com.jidays.ecloud.file.fileMapper.DownloadTaskMapper;
import com.jidays.ecloud.file.fileMapper.EFileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RemoteDownloadService {
    private final ConcurrentHashMap<String, DownloadTask> taskPool = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Thread> threadPool = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Autowired
    EFileMapper eFileMapper;
    @Autowired
    DownloadTaskMapper downloadTaskMapper;
    @Autowired
    FileSystemService fileSystemService;

    private static final Logger logger = LoggerFactory.getLogger(FileSystemService.class);

    private final ConcurrentHashMap<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void addEmitter(SseEmitter emitter, Integer userID) {
        emitters.put(userID, emitter);
    }

    @Scheduled(fixedRate = 1000)
    public void notifyProgress() {
        emitters.forEach((userID, emitter) -> {
            if (emitter != null) {
                try {
                    emitter.send(Objects.requireNonNull(getProgress(userID)), MediaType.APPLICATION_JSON);
                } catch (Exception e) {
                    emitters.remove(userID);
                }
            }
        });
    }

    private Object getProgress(Integer userID) {
        Map<String, Long> progress = new ConcurrentHashMap<>();
        taskPool.forEach((taskID, task) -> {
            if (!task.getStatus().equals("processing")) return;
            progress.put(taskID, task.getDownloaded_bytes());
        });
        return progress;
    }

    public boolean createDownloadTask(DownloadTaskDTO downloadTaskDTO, Integer userID) {
        /* 初始化任务 */
        DownloadTask task = null;
        try {
            task = initDownloadTask(downloadTaskDTO, userID);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        /*
         * 更新数据库
         * 创建本地文件
         * */
        try {
            String fileTrueName = fileSystemService.createFile(task.getFile_save_name(), userID);
            task.setFile_true_name(fileTrueName);
            task.setFilePath(fileSystemService.constructPath(userID).resolve(fileTrueName));
            eFileMapper.addFile(
                    task.getFile_save_name(),
                    task.getFile_true_name(),
                    task.getFile_parent_id(),
                    task.getUser_id());
            downloadTaskMapper.addDownloadTask(task);
        } catch (IOException e) {
            e.printStackTrace();
            downloadTaskMapper.addDownloadTask(task);
            throw new RuntimeException(e);
        }

        /*
         * 添加到任务池
         * 启动任务
         * */
        taskPool.put(task.getTask_uuid(), task);
        task.setStatus("processing");
        downloadTaskMapper.updateTask(task);

        DownloadTask finalTask = task;
        threadPool.put(task.getTask_uuid(), new Thread(() -> {
            try {
                logger.info("Download task " + finalTask.getTask_uuid() + " created: " + finalTask.getFile_save_name());
                taskExecute(finalTask);
                threadPool.remove(finalTask.getTask_uuid());
            } catch (IOException e) {
                finalTask.setStatus("error");
                downloadTaskMapper.updateTask(finalTask);
                threadPool.remove(finalTask.getTask_uuid());
                throw new RuntimeException(e);
            }
        }));
        threadPool.get(task.getTask_uuid()).start();

        return true;
    }

    public DownloadTask initDownloadTask(DownloadTaskDTO downloadTaskDTO, Integer userID) throws IOException, URISyntaxException {
        EFile parentFolder = eFileMapper.getFileById(downloadTaskDTO.getParentID());
        /*
         * 检查父目录
         * */
        if (parentFolder == null) {
            return null;
        }

        long total_size;
        String fileName = downloadTaskDTO.getFileName();
        /*
         * 文件名为空->从URL获取->如果为空，则url无法使用->返回false
         * 文件名不为空->检查是否正确->修正
         * 修正后的文件名->检查是否重复->修正
         * */
        URL url = new URI(downloadTaskDTO.getUrl()).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.getInputStream();
        total_size = connection.getContentLengthLong();
        if (fileName == null || fileName.isEmpty()) {
            // 尝试从 URL 中提取文件名
            fileName = Paths.get(url.getPath()).getFileName().toString();
        }
        if (fileName.isEmpty()) {
            // 尝试从响应头中获取文件名
            String contentDisposition = connection.getHeaderField("Content-Disposition");
            if (contentDisposition != null && contentDisposition.contains("filename=")) {
                int index = contentDisposition.indexOf("filename=") + 9;
                fileName = contentDisposition.substring(index).replace("\"", "");
            } else {
                // 如果无法从 URL 和响应头中获取文件名，使用默认文件名
                fileName = "remoteDownload.noname";
            }
        }
        if (fileName.lastIndexOf(".") == -1) {
            fileName += ".noname";
        }
        connection.disconnect();

        for (int i = 0; eFileMapper.checkExistence(fileName, parentFolder.getFile_id(), userID) > 0; i++) {
            fileName = "%s(%d)%s".formatted(
                    fileName.substring(0, fileName.lastIndexOf(".")),
                    i,
                    fileName.substring(fileName.lastIndexOf("."))
            );
        }

        /*
         * 初始化任务
         * 更新数据库
         * 创建本地文件
         * 启动线程
         * */
        DownloadTask result = new DownloadTask(
                fileName,
                url.toString(),
                parentFolder.getFile_id(),
                userID);
        result.setTotal_bytes(total_size);
        return result;
    }


    public boolean resumeDownloadTask(String taskID, Integer userID) {
        DownloadTask task = taskPool.get(taskID);
        if (task == null) {
            task = downloadTaskMapper.getTaskByUUID(taskID);
            if (task == null) return false;
        }
        if ((task.getUser_id() != userID && userID != 0)  || task.getStatus().equals("completed")) {
            return false;
        }
        if (task.getFilePath() == null) {
            try {
                task.setFilePath(fileSystemService.constructPath(userID).resolve(task.getFile_true_name()));
                task.setJurl(new URI(task.getUrl()).toURL());
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        taskPool.put(taskID, task);
        task.setStatus("processing");
        downloadTaskMapper.updateTask(task);

//        executorService.execute(() -> {
//            try {
//                logger.info("Download task(" + taskID + ") resumed");
//                taskExecute(taskPool.get(taskID));
//            } catch (IOException e) {
//                taskPool.get(taskID).setStatus("error");
//                downloadTaskMapper.updateTask(taskPool.get(taskID));
//                logger.info("Download task(" + taskID + ") paused");
//                throw new RuntimeException(e);
//            }
//        });
        DownloadTask finalTask = taskPool.get(taskID);
        threadPool.put(taskID, new Thread(() -> {
            try {
                logger.info("Download task(" + taskID + ") resumed");
                taskExecute(finalTask);
                threadPool.remove(finalTask.getTask_uuid());
            } catch (IOException e) {
                taskPool.get(taskID).setStatus("error");
                downloadTaskMapper.updateTask(finalTask);
                threadPool.remove(finalTask.getTask_uuid());
                logger.info("Download task(" + taskID + ") paused");
                throw new RuntimeException(e);
            }
        }));
        threadPool.get(taskID).start();

        return true;
    }

    public boolean pauseDownloadTask(String taskID, Integer userID) {
        DownloadTask task = taskPool.get(taskID);
        if (task == null) {
            task = downloadTaskMapper.getTaskByUUID(taskID);
            if (task == null) return false;
        }
        if ((task.getUser_id() != userID && userID != 0) || task.getStatus().equals("completed")) {
            return false;
        }
        task.setStatus("paused");
        Thread thread = threadPool.get(taskID);
        if (thread != null) {
            try {
                thread.join();
                threadPool.remove(taskID);
                logger.info("Download task(" + taskID + ") stopped");
            } catch (InterruptedException e) {
                e.printStackTrace();
                threadPool.remove(taskID);
                throw new RuntimeException(e);
            }
        }
        downloadTaskMapper.updateTask(task);
        return true;
    }

    public boolean cancelDownloadTask(String taskID) {
        return true;
    }

    public boolean deleteDownloadTask(String taskID) {
        DownloadTask task = taskPool.get(taskID);
        if (task == null) {
            task = downloadTaskMapper.getTaskByUUID(taskID);
            if (task == null) return false;
        } else if (threadPool.containsKey(taskID) && task.getStatus().equals("processing")) {
            task.setStatus("paused");
            try {
                threadPool.get(taskID).join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            threadPool.remove(taskID);
        }

        taskPool.remove(task.getTask_uuid());
        downloadTaskMapper.deleteTask(task);

        return true;
    }

    @Async
    public void taskExecute(DownloadTask task) throws IOException {
        URL url = task.getJurl();
        long downloadedBytes = task.getDownloaded_bytes();
        String filename = task.getFile_save_name();
        Path filePath = task.getFilePath();

        /*
         * 检查本地文件是否创建
         * */
        if (!Files.exists(filePath)) {
            task.setStatus("error");
            downloadTaskMapper.updateTask(task);
            return;
        }

        HttpURLConnection connection = null;
        ReadableByteChannel readableByteChannel = null;
        FileChannel fileChannel = null;
        try {
            logger.info(task.getTask_uuid() + " connecting...");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Range", "bytes=" + downloadedBytes + "-");
            connection.setConnectTimeout(30000); // 连接超时设置为30秒
            connection.setReadTimeout(30000); // 读取超时设置为30秒
            logger.info(task.getTask_uuid() + " connected:: " + connection.getResponseCode());
            readableByteChannel = Channels.newChannel(connection.getInputStream());
            fileChannel = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);

            fileChannel.position(downloadedBytes);
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
            while (task.getStatus().equals("processing") && readableByteChannel.read(buffer) > 0) {
                buffer.flip();
                fileChannel.write(buffer);
                buffer.clear();
                task.setDownloaded_bytes(fileChannel.position());
            }
            task.setDownloaded_bytes(fileChannel.position());
        } catch (IOException e) {
            task.setStatus("error");
            downloadTaskMapper.updateTask(task);
            if (fileChannel != null) fileChannel.close();
            if (readableByteChannel != null) readableByteChannel.close();
            if (connection != null) connection.disconnect();
            throw new RuntimeException(e);
        }

        /*
         * 如果是任务完成->更新task->文件数据库新建文件
         * 如果任务被暂停或者被停止->更新task
         * 如果任务失败->更新task
         * */
        if (Files.size(filePath) == connection.getContentLengthLong()) {
            task.setStatus("completed");
            task.setTotal_bytes(Files.size(filePath));
            emitters.forEach((userID, emitter) -> {
                if (emitter != null) {
                    try {
                        emitter.send(Objects.requireNonNull(getProgress(userID)), MediaType.APPLICATION_JSON);
                    } catch (Exception e) {
                        emitters.remove(userID);
                    }
                }
            });
        } else if (!task.getStatus().equals("paused") && !task.getStatus().equals("processing")) {
            task.setStatus("failed");
        }
        downloadTaskMapper.updateTask(task);
        fileChannel.close();
        readableByteChannel.close();
    }

    public DownloadTask getTaskByUUID(String taskID) {
        return downloadTaskMapper.getTaskByUUID(taskID);
    }

    public List<DownloadTask> getTaskList(Integer userID) {
        return downloadTaskMapper.getTaskList(userID);
    }


    public List<DownloadTask> getAllTaskList() {
        return downloadTaskMapper.getAllTaskList();
    }
}
