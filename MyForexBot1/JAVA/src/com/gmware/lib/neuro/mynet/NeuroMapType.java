package com.gmware.lib.neuro.mynet;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.Maps.*;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Тип нейросети.
 * Created by Gauss on 09.12.2015.
 */
public enum NeuroMapType {
    Cells("CLS", false, 20160527, NeuroTrainerType.CellsDefinition) {
        @Override
        public String getDescription() {
            return "Cells";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure, final int trainSize, final int testSize, final NetImage[] images, final Rnd rnd) {
            return null;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return null;
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 0;
        }

        @Override
        public NeuroMap load(final DataInputStream dis) throws IOException {
            return NeuroMapCells.loadNewMap(dis);
        }
    },
    Partition("PRT", false, 20160411, NeuroTrainerType.PartitionByDetermination) {
        @Override
        public String getDescription() {
            return "Partition";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure, final int trainSize, final int testSize, final NetImage[] images, final Rnd rnd) {
            return null;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return null;
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 0;
        }

        @Override
        public NeuroMap load(final DataInputStream dis) throws IOException {
            return NeuroMapPartition.loadNewMap(dis);
        }
    },
    PartitionByCorrelation3("PBC3", false, 20160315, NeuroTrainerType.PartitionByCorrelation3) {
        @Override
        public String getDescription() {
            return "PartitionByCorrelation3";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure, final int trainSize, final int testSize, final NetImage[] images, final Rnd rnd) {
            return null;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return null;
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 0;
        }

        @Override
        public NeuroMap load(final DataInputStream dis) throws IOException {
            return NeuroMapPartitionByCorrelation3.loadNewMap(dis);
        }
    },
    PartitionByCorrelation2("PBC2", false, 20160225, NeuroTrainerType.PartitionByCorrelation2) {
        @Override
        public String getDescription() {
            return "PartitionByCorrelation2";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure, final int trainSize, final int testSize, final NetImage[] images, final Rnd rnd) {
            return null;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return null;
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 0;
        }

        @Override
        public NeuroMap load(final DataInputStream dis) throws IOException {
            return NeuroMapPartitionByCorrelation2.loadNewMap(dis);
        }
    },
    Unknown("UNKNOWN!!!", false, 20160224, null) {
        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure,
                                           final int trainSize,
                                           final int testSize,
                                           final NetImage[] images,
                                           final Rnd rnd) {
            return null;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return null;
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 0;
        }

        @Override
        public NeuroMap load(final DataInputStream dis) {
            return NeuroMapUnknown.loadNewMap(dis);
        }
    },
    Empty("EMPTY", false, 20160223, null) {
        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure,
                                           final int trainSize,
                                           final int testSize,
                                           final NetImage[] images,
                                           final Rnd rnd) {
            return null;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return null;
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 0;
        }

        @Override
        public NeuroMap load(final DataInputStream dis) {
            return NeuroMapEmpty.loadNewMap(dis);
        }
    },
    PartitionByCorrelation("PBC", false, 20160219, NeuroTrainerType.PartitionByCorrelation) {
        @Override
        public String getDescription() {
            return "PartitionByCorrelation";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure, final int trainSize, final int testSize, final NetImage[] images, final Rnd rnd) {
            return null;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return null;
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 0;
        }

        @Override
        public NeuroMap load(final DataInputStream dis) throws IOException {
            return NeuroMapPartitionByCorrelation.loadNewMap(dis);
        }
    },
    LinearSegments("LS", false, 20160218, NeuroTrainerType.LinearSegments) {
        @Override
        public String getDescription() {
            return "LinearSegments";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure, final int trainSize, final int testSize, final NetImage[] images, final Rnd rnd) {
            return null;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return null;
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 0;
        }

        @Override
        public NeuroMap load(final DataInputStream dis) throws IOException {
            return NeuroMapLinearSegments.loadNewMap(dis);
        }
    },
    Spline("S", false, 20160216, NeuroTrainerType.Spline) {
        @Override
        public String getDescription() {
            return "Spline";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure, final int trainSize, final int testSize, final NetImage[] images, final Rnd rnd) {
            return null;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return null;
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 0;
        }

        @Override
        public NeuroMap load(final DataInputStream dis) throws IOException {
            return NeuroMapSpline.loadNewMap(dis);
        }

    },
    Polinom("P", false, 20160211, NeuroTrainerType.Polinom) {
        @Override
        public String getDescription() {
            return "Polinom";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure, final int trainSize, final int testSize, final NetImage[] images, final Rnd rnd) {
            return null;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return null;
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 0;
        }

        @Override
        public NeuroMap load(final DataInputStream dis) throws IOException {
            return NeuroMapPolinom.loadNewMap(dis);
        }

    },
    Normalizer("N", false, 20160125, NeuroTrainerType.Standart) {
        @Override
        public String getDescription() {
            return "Функция-нормализатор. Обычно применяется с одним входом и одним выходом.";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure,
                                           final int trainSize,
                                           final int testSize,
                                           final NetImage[] images,
                                           final Rnd rnd) {
            final NeuroNet net = new NeuroNet1p5N(structure);
            net.trainImages = new NetImage[trainSize];
            net.testImages = new NetImage[testSize];
            NeuroTrainer.selectTrainTestImages(images, net);
            NeuroNet.randomize(net, rnd);
            return net;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return new NeuroNetStructure(new int[][]{
                    {image.numIn},
                    {(int) (4 * strength + 1)},
                    {image.numOut}});
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 100;//TODO !neuro!test
        }

        @Override
        public NeuroNet load(final DataInputStream dis) throws IOException {
            return NeuroNet1p5N.loadNewNet(dis);
        }

    },
    SumOfSigmoids("SOS", false, 20160126, NeuroTrainerType.Standart) {
        @Override
        public String getDescription() {
            return "Сумма нескольких сигмоидов.";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure,
                                           final int trainSize,
                                           final int testSize,
                                           final NetImage[] images,
                                           final Rnd rnd) {
            final NeuroNet net = new NeuroNet1p5N(structure);
            net.trainImages = new NetImage[trainSize];
            net.testImages = new NetImage[testSize];
            NeuroTrainer.selectTrainTestImages(images, net);
            NeuroNet.randomize(net, rnd);
            return net;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return new NeuroNetStructure(new int[][]{
                    {image.numIn},
                    {3},
                    {image.numOut}});
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 20;//TODO !neuro!test
        }

        @Override
        public NeuroNet load(final DataInputStream dis) throws IOException {
            return NeuroNet1p5N.loadNewNet(dis);
        }

    },
    TwoNodeLayers("TNL", false, 20160121, NeuroTrainerType.Standart) {
        @Override
        public String getDescription() {
            return "Старые нейросети с одним внутренним слоем";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure,
                                           final int trainSize,
                                           final int testSize,
                                           final NetImage[] images,
                                           final Rnd rnd) {
            final NeuroNet net = new NeuroNet2N(structure);
            net.trainImages = new NetImage[trainSize];
            net.testImages = new NetImage[testSize];
            NeuroTrainer.selectTrainTestImages(images, net);
            NeuroNet.randomize(net, rnd);
            return net;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return new NeuroNetStructure(new int[][]{
                    {image.numIn},
                    {(int) (StrictMath.log(numImages) * strength) + 1},
                    {image.numOut}});
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 200;//TODO !neuro!test
        }

        @Override
        public NeuroNet load(final DataInputStream dis) throws IOException {
            return NeuroNet2N.loadNewNet(dis);
        }

    },
    FourNodeLayers("FNL", true, 20151207, NeuroTrainerType.Standart) {
        @Override
        public String getDescription() {
            return "Четырехслойные нейросети с общей частью";
        }

        @Override
        public NeuroNet createNewRandomNet(final NeuroNetStructure structure,
                                           final int trainSize,
                                           final int testSize,
                                           final NetImage[] images,
                                           final Rnd rnd) {
            final NeuroNet net = new NeuroNet4N(structure);
            net.trainImages = new NetImage[trainSize];
            net.testImages = new NetImage[testSize];
            NeuroTrainer.selectTrainTestImages(images, net);
            NeuroNet.randomize(net, rnd);
            return net;
        }

        @Override
        public NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength) {
            return new NeuroNetStructure(new int[][]{
                    {70, image.numIn - 70},
                    {(int) (StrictMath.log(numImages) * strength) + 1},
                    {(int) (StrictMath.log(numImages) * strength) + 1},
                    {(int) (StrictMath.log(numImages) * strength) + 1},
                    {image.numOut}});
        }

        @Override
        public int getNumPretenders(final int numImages) {
            return 50;//TODO !neuro!test
//            return (int) (StrictMath.log(numOfImages) * 4) + 10;
        }

        @Override
        public NeuroNet load(final DataInputStream dis) throws IOException {
            return NeuroNet4N.loadNewNet(dis);
        }

    };

    public final boolean isNormalised;
    private final long formatCode;
    private final NeuroTrainerType defaultTrainer;
    private String shortDescription;

    NeuroMapType(final String shortDescription,
                 final boolean isNormalised,
                 final int code,
                 final NeuroTrainerType trainer) {
        this.shortDescription = shortDescription;
        this.isNormalised = isNormalised;
        this.formatCode = code;
        this.defaultTrainer = trainer;
    }

    public abstract String getDescription();

    public long getFormatCode() {
        return formatCode;
    }

    public NeuroTrainerType getDefaultTrainer() {
        return defaultTrainer;
    }

    public abstract NeuroNet createNewRandomNet(final NeuroNetStructure structure,
                                                final int trainSize,
                                                final int testSize,
                                                final NetImage[] images,
                                                final Rnd rnd);

    /**
     * @param numImages число образов
     * @param image     структура образа
     * @return структура нейросети
     */
    public abstract NeuroNetStructure makeStructure(final int numImages, final NetImage image, final double strength);

    /**
     * @param numImages число образов
     * @return число начальных нейросетей при обучении.
     */
    public abstract int getNumPretenders(final int numImages);


    public abstract NeuroMap load(DataInputStream dis) throws IOException;

    public NeuroMapping makeDefaultTrainer() {
        return defaultTrainer.makeTrainer();
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(final String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public static NeuroMapType getType(final String name) {
        for (final NeuroMapType t : NeuroMapType.values()) {
            if (name.equals(t.getShortDescription())) {
                return t;
            }
        }
        return null;
    }
}

