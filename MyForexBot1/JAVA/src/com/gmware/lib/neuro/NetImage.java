package com.gmware.lib.neuro;

import java.io.*;
import java.util.Formatter;
import java.util.Locale;
import java.util.zip.DataFormatException;

/**
 * Created by IntelliJ IDEA.
 * Date: 29.05.13
 * Time: 16:04
 */
public class NetImage {

    /**
     * Максимальная длина информации об образе.
     */
    public static final byte IMAGE_INFO_MAX_LENGTH = Byte.MAX_VALUE;

    public int numIn;
    public int numOut;
    public double[] in;
    public double[] out;
    private byte imageInfoLength;
    private byte[] imageInfoBytes;
    boolean notUsed = true;

    public NetImage() {
        numIn = 0;
        numOut = 0;
        in = null;
        out = null;
        imageInfoLength = 0;
        imageInfoBytes = null;
        notUsed = true;
    }

    public NetImage(final DataInputStream dis) throws IOException {
        numIn = 0;
        numOut = 0;
        in = null;
        out = null;
        imageInfoLength = 0;
        imageInfoBytes = null;
        readImage(dis);
    }

    public NetImage(final int ni, final int no) {
        numIn = ni;
        numOut = no;
        if (ni > 0) {
            in = new double[ni];
        } else {
            in = null;
        }
        if (no > 0) {
            out = new double[no];
        } else {
            out = null;
        }
        imageInfoLength = 0;
        imageInfoBytes = null;
    }

    public NetImage(final int ni, final int no, final double[] vi, final double[] vo) {
        numIn = ni;
        numOut = no;
        if (ni > 0) {
            in = new double[ni];
            System.arraycopy(vi, 0, in, 0, ni);
        } else {
            in = null;
        }
        if (no > 0) {
            out = new double[no];
            System.arraycopy(vo, 0, out, 0, no);
        } else {
            out = null;
        }
        imageInfoLength = 0;
        imageInfoBytes = null;
    }

    public NetImage(final int ni, final int no, final double[] vi, final double[] vo, final byte iil, final byte[] iib) {
        numIn = ni;
        numOut = no;
        if (ni > 0) {
            in = new double[ni];
            System.arraycopy(vi, 0, in, 0, ni);
        } else {
            in = null;
        }
        if (no > 0) {
            out = new double[no];
            System.arraycopy(vo, 0, out, 0, ni);
        } else {
            out = null;
        }
        imageInfoLength = iil;
        if (iil > 0) {
            imageInfoBytes = new byte[iil];
            System.arraycopy(iib, 0, imageInfoBytes, 0, iil);
        } else {
            imageInfoBytes = null;
        }
    }

    public int getNumIn() {
        return numIn;
    }

    public int getNumOut() {
        return numOut;
    }

    public NetImage getCopy() {
        return new NetImage(numIn, numOut, in, out, imageInfoLength, imageInfoBytes);
    }

    public void addIn(final double v) {
        in[numIn++] = v;
    }

    private void readNewImage(final DataInputStream dis) throws IOException {
        numIn = dis.readInt();
        numOut = dis.readInt();
        in = new double[numIn];
        for (int i = 0; i < numIn; ++i) {
            in[i] = dis.readDouble();
        }
        out = new double[numOut];
        for (int i = 0; i < numOut; ++i) {
            out[i] = dis.readDouble();
        }
        imageInfoLength = dis.readByte();
        if (imageInfoLength > 0) {
            imageInfoBytes = new byte[imageInfoLength];
            dis.read(imageInfoBytes);
        } else {
            imageInfoBytes = null;
        }
    }

    private static ThreadLocal<byte[]> writeBufferTL = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[10000];
        }
    };

    public final void readImage(final DataInputStream dis) throws IOException {
        tag = dis.readLong();
        if (notUsed) {
            readNewImage(dis);
            notUsed = false;
        } else {
            if (numIn != dis.readInt()) {
                throw new IOException("Изменилось число входов образа.");
            }
            if (numOut != dis.readInt()) {
                throw new IOException("Изменилось число выходов образа.");
            }
            for (int i = 0; i < numIn; ++i) {
                in[i] = dis.readDouble();
            }
            for (int i = 0; i < numOut; ++i) {
                out[i] = dis.readDouble();
            }
            final byte iil = dis.readByte();
            if (iil < 0) {
                throw new IOException("Длина информации об образе < 0.");
            }
            if (imageInfoLength != iil) {
                imageInfoLength = iil;
                if (imageInfoLength > 0) {
                    imageInfoBytes = new byte[imageInfoLength];
                }
            }
            if (imageInfoLength > 0) {
                dis.read(imageInfoBytes);
            } else {
                imageInfoBytes = null;
            }
        }
        if ((numIn + numOut) % 2 != 0) {
            dis.readLong();
        }
    }

    final void readImages(final String nameIn, final String nameOut) throws FileNotFoundException {
        final DataInputStream dis = new DataInputStream(new FileInputStream(nameIn));
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(nameOut));
        try {
            readNewImage(dis);
            dos.writeBytes(toString() + "\n");
            while (true) {
                readImage(dis);
                dos.writeBytes(toString() + "\n");
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public final void skipImage(final DataInputStream dis) throws IOException {
        tag = dis.readLong();
        if (notUsed) {
            readNewImage(dis);
            notUsed = false;
        } else {
            if (numIn != dis.readInt()) {
                throw new IOException("Изменилось число входов образа.");
            }
            if (numOut != dis.readInt()) {
                throw new IOException("Изменилось число выходов образа.");
            }
            dis.skipBytes((numIn + numOut) * 8);
            final byte iil = dis.readByte();
            if (iil < 0) {
                throw new IOException("Длина информации об образе < 0.");
            }
            if (iil > 0) {
                dis.skipBytes(iil);
            }
            if (imageInfoLength != iil) {
                imageInfoLength = iil;
                if (imageInfoLength > 0) {
                    imageInfoBytes = new byte[imageInfoLength];
                }
            }
            if (imageInfoLength > 0) {
                dis.read(imageInfoBytes);
            } else {
                imageInfoBytes = null;
            }
        }
        if ((numIn + numOut) % 2 != 0) {
            dis.skipBytes(8);
        }
    }

    public long tag = 0;

    public synchronized final void writeImage(final DataOutputStream dos) throws IOException {
        final byte[] writeBuffer = writeBufferTL.get();
        int j = 0;

        long v = tag;
        writeBuffer[j++] = (byte) (v >>> 56);
        writeBuffer[j++] = (byte) (v >>> 48);
        writeBuffer[j++] = (byte) (v >>> 40);
        writeBuffer[j++] = (byte) (v >>> 32);
        writeBuffer[j++] = (byte) (v >>> 24);
        writeBuffer[j++] = (byte) (v >>> 16);
        writeBuffer[j++] = (byte) (v >>> 8);
        writeBuffer[j++] = (byte) (v);

        writeBuffer[j++] = (byte) (numIn >>> 24);
        writeBuffer[j++] = (byte) (numIn >>> 16);
        writeBuffer[j++] = (byte) (numIn >>> 8);
        writeBuffer[j++] = (byte) (numIn);

        writeBuffer[j++] = (byte) (numOut >>> 24);
        writeBuffer[j++] = (byte) (numOut >>> 16);
        writeBuffer[j++] = (byte) (numOut >>> 8);
        writeBuffer[j++] = (byte) (numOut);

        for (int i = 0; i < numIn; ++i) {
            v = Double.doubleToLongBits(in[i]);
            writeBuffer[j++] = (byte) (v >>> 56);
            writeBuffer[j++] = (byte) (v >>> 48);
            writeBuffer[j++] = (byte) (v >>> 40);
            writeBuffer[j++] = (byte) (v >>> 32);
            writeBuffer[j++] = (byte) (v >>> 24);
            writeBuffer[j++] = (byte) (v >>> 16);
            writeBuffer[j++] = (byte) (v >>> 8);
            writeBuffer[j++] = (byte) (v);
        }
        for (int i = 0; i < numOut; ++i) {
            v = Double.doubleToLongBits(out[i]);
            writeBuffer[j++] = (byte) (v >>> 56);
            writeBuffer[j++] = (byte) (v >>> 48);
            writeBuffer[j++] = (byte) (v >>> 40);
            writeBuffer[j++] = (byte) (v >>> 32);
            writeBuffer[j++] = (byte) (v >>> 24);
            writeBuffer[j++] = (byte) (v >>> 16);
            writeBuffer[j++] = (byte) (v >>> 8);
            writeBuffer[j++] = (byte) (v);
        }
        writeBuffer[j++] = imageInfoLength;
        if (imageInfoLength > 0) {
            System.arraycopy(imageInfoBytes, 0, writeBuffer, j, imageInfoLength);
            j += imageInfoLength;
        }
        if (imageInfoBytes != null && imageInfoBytes.length != imageInfoLength) {
            try {
                throw new DataFormatException("NetImage: imageInfoBytes.length != imageInfoLength");
            } catch (DataFormatException e) {
                e.printStackTrace();
            }
        }
        if ((numIn + numOut) % 2 != 0) {
            for (int i = 0; i < 8; ++i) {
                writeBuffer[j++] = 0;
            }
        }
        dos.write(writeBuffer, 0, j);
    }

    public void setOuts(final double v) {
        numOut = 1;
        out = new double[numOut];
        out[0] = v;
    }

    public void addOuts(final double v1, final double v2, final double v3) {
        numOut = 3;
        out = new double[numOut];
        out[0] = v1;
        out[1] = v2;
        out[2] = v3;
    }

    public void addOuts(final double v1, final double v2, final double v3, final double v4) {
        numOut = 4;
        out = new double[numOut];
        out[0] = v1;
        out[1] = v2;
        out[2] = v3;
        out[3] = v4;
    }

    public void addOuts(final double[] v) {
        numOut = v.length;
        out = new double[v.length];
        System.arraycopy(v, 0, out, 0, out.length);
    }

    /**
     * Получить информацию об образе в виде строки.
     *
     * @return информация об образе в виде строки или {@code null}, если информация об образе отсутствует.
     */
    public String getImageInfo() {
        if (imageInfoBytes == null) {
            return null;
        } else {
            return new String(imageInfoBytes);
        }
    }

    /**
     * Установить значение для информации об образе.
     * Поскольку производится преобразование в массив байтов,
     * входная информация об образе в виде строки должна содержать только символы с кодировкой
     * от 0 до 127 (цифры, латинские буквы, знаки препинания, пробел).
     *
     * @param imageInfo информация об образе в виде строки.
     */
    public void setImageInfo(final String imageInfo) {
        imageInfoLength = imageInfo == null || imageInfo.isEmpty() ? 0 : (imageInfo.length() <= IMAGE_INFO_MAX_LENGTH ? (byte) imageInfo.length() : IMAGE_INFO_MAX_LENGTH);
        if (imageInfoLength > 0) {
            imageInfoBytes = new byte[imageInfoLength];
            for (int i = 0; i < imageInfoLength; ++i) {
                imageInfoBytes[i] = (byte) imageInfo.charAt(i);
            }
        } else {
            imageInfoBytes = null;
        }
    }

    private StringBuilder s = null;
    private StringBuilder sb = null;
    private Formatter f = null;

    public String toString() {
        if (s == null) {
            s = new StringBuilder(2000);
        }
        if (sb == null) {
            sb = new StringBuilder(20);
        }
        if (f == null) {
            f = new Formatter(sb);
        }
        s.setLength(0);
        for (int i = 0; i < numIn; i++) {
            s.append(fmt(in[i])).append(' ');
        }
        s.append('|');
        for (int i = 0; i < numOut; i++) {
            if (out[i] > 0) {
                s.append(" +0.5");
            } else {
                s.append(" -0.5");
            }
        }
        if (imageInfoLength > 0) {
            s.append(" | ");
            for (int i = 0; i < imageInfoLength; i++) {
                s.append((char) imageInfoBytes[i]);
            }
        }
        return s.substring(0);
    }

    public String toStringAsAlpha() {
        if (s == null) {
            s = new StringBuilder(2000);
        }
        if (sb == null) {
            sb = new StringBuilder(20);
        }
        if (f == null) {
            f = new Formatter(sb);
        }
        s.setLength(0);
        for (int i = 0; i < numIn; i++) {
            s.append(fmt(in[i])).append(' ');
        }
        s.append('|').append(' ').append(fmt(out[0]));
        if (imageInfoLength > 0) {
            s.append(" | ");
            for (int i = 0; i < imageInfoLength; i++) {
                s.append((char) imageInfoBytes[i]);
            }
        }
        return s.substring(0);
    }

    public String toString1() {
        if (s == null) {
            s = new StringBuilder(2000);
        }
        if (sb == null) {
            sb = new StringBuilder(20);
        }
        if (f == null) {
            f = new Formatter(sb);
        }
        s.setLength(0);
        for (int i = 0; i < numIn; i++) {
            s.append(fmt(in[i])).append(' ');
        }
        for (int i = 0; i < numOut; i++) {
            s.append('|').append(' ').append(fmt(out[i]));
        }
        if (imageInfoLength > 0) {
            s.append(" | ");
            for (int i = 0; i < imageInfoLength; i++) {
                s.append((char) imageInfoBytes[i]);
            }
        }
        return s.substring(0);
    }

    private String fmt(final double x) {
        sb.setLength(0);
        f.format(Locale.US, "%.7f", x);
        while (sb.length() < 9) {
            sb.append(' ');
        }
        return sb.substring(0, 9);
    }

}
