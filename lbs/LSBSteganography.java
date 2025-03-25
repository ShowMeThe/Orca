import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LSBSteganography {

    // 嵌入文件到图片
    public static void embedFiles(String imagePath, List<String> filePaths, String outputImagePath) throws IOException {
        BufferedImage img = ImageIO.read(new File(imagePath));
        int width = img.getWidth();
        int height = img.getHeight();
        int totalPixels = width * height;

        // 合并文件数据为二进制流（含元数据）
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        for (String filePath : filePaths) {
            File file = new File(filePath);
            byte[] fileData = Files.readAllBytes(file.toPath());
            String fileName = file.getName();
            byte[] fileNameBytes = fileName.getBytes("UTF-8");

            // 写入文件名长度（4字节）、文件名、文件大小（8字节）
            dataStream.write(ByteBuffer.allocate(4).putInt(fileNameBytes.length).array());
            dataStream.write(fileNameBytes);
            dataStream.write(ByteBuffer.allocate(8).putLong(fileData.length).array());
            dataStream.write(fileData);
        }

        byte[] binaryData = dataStream.toByteArray();
        int requiredBits = binaryData.length * 8;
        int availableBits = totalPixels * 3; // 每个像素3位（RGB）
        if (requiredBits > availableBits) {
            throw new IllegalArgumentException("图片容量不足，需至少 " + requiredBits / 3 + " 像素，当前为 " + totalPixels);
        }

        // 将字节数据转换为比特流
        int[] bits = new int[requiredBits];
        int bitIndex = 0;
        for (byte b : binaryData) {
            for (int i = 7; i >= 0; i--) {
                bits[bitIndex++] = (b >> i) & 1;
            }
        }

        // 嵌入数据到LSB
        bitIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // 修改每个通道的最低位
                if (bitIndex < bits.length) {
                    r = (r & 0xFE) | bits[bitIndex++];
                }
                if (bitIndex < bits.length) {
                    g = (g & 0xFE) | bits[bitIndex++];
                }
                if (bitIndex < bits.length) {
                    b = (b & 0xFE) | bits[bitIndex++];
                }

                int newRgb = (r << 16) | (g << 8) | b;
                img.setRGB(x, y, newRgb);
            }
        }

        // 保存为PNG格式（无损）
        ImageIO.write(img, "PNG", new File(outputImagePath));
        System.out.println("文件已嵌入至: " + outputImagePath);
    }

    // 从图片提取文件
    public static void extractFiles(String imagePath, String outputDir) throws IOException {
        BufferedImage img = ImageIO.read(new File(imagePath));
        int width = img.getWidth();
        int height = img.getHeight();

        // 提取所有LSB位
        List<Integer> bits = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                bits.add((rgb >> 16) & 1); // R通道的LSB
                bits.add((rgb >> 8) & 1);  // G通道的LSB
                bits.add(rgb & 1);         // B通道的LSB
            }
        }

        // 将比特流转换为字节
        byte[] data = new byte[bits.size() / 8];
        for (int i = 0; i < data.length; i++) {
            int byteValue = 0;
            for (int j = 0; j < 8; j++) {
                int bitIndex = i * 8 + j;
                if (bitIndex < bits.size()) {
                    byteValue = (byteValue << 1) | bits.get(bitIndex);
                }
            }
            data[i] = (byte) byteValue;
        }

        // 解析元数据并提取文件
        int ptr = 0;
        while (ptr < data.length) {
            // 读取文件名长度（4字节）
            if (ptr + 4 > data.length) break;
            int nameLen = ByteBuffer.wrap(data, ptr, 4).getInt();
            ptr += 4;

            // 读取文件名
            if (ptr + nameLen > data.length) break;
            String fileName = new String(data, ptr, nameLen, "UTF-8");
            ptr += nameLen;

            // 读取文件大小（8字节）
            if (ptr + 8 > data.length) break;
            long fileSize = ByteBuffer.wrap(data, ptr, 8).getLong();
            ptr += 8;

            // 读取文件内容
            if (ptr + fileSize > data.length) break;
            byte[] fileData = new byte[(int) fileSize];
            System.arraycopy(data, ptr, fileData, 0, (int) fileSize);
            ptr += fileSize;

            // 保存文件
            File outputFile = new File(outputDir, fileName);
            if(!outputFile.exists()){
                outputFile.getParentFile().mkdirs();
                outputFile.createNewFile();
            }
            Files.write(outputFile.toPath(), fileData);
            System.out.println("提取文件: " + outputFile.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        try {
            // 嵌入示例
            List<String> filesToEmbed = new ArrayList<>();
            filesToEmbed.add("C:\\Users\\anbian\\Desktop\\monster");
            embedFiles("C:\\Users\\anbian\\Desktop\\monster.jpg", filesToEmbed, "C:\\Users\\anbian\\Desktop\\output.png");

            // 提取示例
            extractFiles("C:\\Users\\anbian\\Desktop\\output.png", "C:\\Users\\anbian\\Desktop\\extracted_files");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}